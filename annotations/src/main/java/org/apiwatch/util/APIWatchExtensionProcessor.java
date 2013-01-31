package org.apiwatch.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes("org.apiwatch.util.APIWatchExtension")
public class APIWatchExtensionProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        try {

            Map<String, List<String>> extensions = new HashMap<String, List<String>>();

            for (TypeElement annotation : annotations) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                    TypeElement implementation = (TypeElement) element;
                    String implName = implementation.getQualifiedName().toString();

                    for (TypeMirror mirror : implementation.getInterfaces()) {

                        TypeElement iface = (TypeElement) ((DeclaredType) mirror).asElement();
                        String ifaceName = iface.getQualifiedName().toString();

                        if (ifaceName.startsWith("org.apiwatch.")) {
                            if (!extensions.containsKey(ifaceName)) {
                                extensions.put(ifaceName, new ArrayList<String>());
                            }
                            extensions.get(ifaceName).add(implName);
                            messager.printMessage(Kind.NOTE, String.format(
                                    "Found extension %s implements %s", implName, ifaceName));
                        }
                    }
                }
            }

            for (Map.Entry<String, List<String>> e : extensions.entrySet()) {
                String filePath = "META-INF/services/" + e.getKey();
                FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", filePath);
                Writer writer = file.openWriter();
                for (String implName : e.getValue()) {
                    writer.write(implName + "\n");
                }
                writer.close();
                messager.printMessage(Kind.NOTE, "Generated file " + filePath);
            }
        } catch (IOException e) {
            messager.printMessage(Kind.ERROR, e.getMessage());
            return false;
        }

        return true;
    }

}
