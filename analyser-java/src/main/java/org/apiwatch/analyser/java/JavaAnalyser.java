/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.java;

import java.io.IOException;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apiwatch.analyser.Analyser;
import org.apiwatch.analyser.LanguageAnalyser;
import org.apiwatch.analyser.Option;
import org.apiwatch.models.APIScope;
import org.apiwatch.util.antlr.IterableTree;
import org.apiwatch.util.antlr.IterableTreeAdaptor;
import org.apiwatch.util.errors.ParseError;

public class JavaAnalyser implements LanguageAnalyser {

    private static final String LANGUAGE = "Java";
    private static final String[] FILE_EXTENSIONS = { "java" };

    @Override
    public String[] fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String language() {
        return LANGUAGE;
    }

    @Override
    public Option[] options() {
        return null;
    }

    @Override
    public APIScope analyse(String sourceFile, Map<String, Object> options) throws IOException,
            ParseError
    {
        String encoding = null;
        if (options != null) {
            encoding = (String) options.get(Analyser.ENCODING_OPTION);
        }
        if (encoding == null) {
            encoding = Analyser.DEFAULT_ENCODING;
        }

        JavaLexer lexer = new JavaLexer(new ANTLRFileStream(sourceFile, encoding));
        JavaParser parser = new JavaParser(new CommonTokenStream(lexer));
        parser.setTreeAdaptor(new IterableTreeAdaptor());
        JavaTreeWalker walker = new JavaTreeWalker(language(), sourceFile);

        try {
            Object ast = parser.javaSource().getTree();
            walker.walk((IterableTree) ast);
        } catch (RecognitionException e) {
            throw new ParseError(e);
        }

        return walker.globalScope;
    }

}
