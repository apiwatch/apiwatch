/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apiwatch.analyser.LanguageAnalyser;
import org.apiwatch.models.APIScope;
import org.apiwatch.util.StringUtils;
import org.apiwatch.util.antlr.IterableTree;
import org.apiwatch.util.antlr.IterableTreeAdaptor;
import org.apiwatch.util.errors.ParseError;

public class CAnalyser implements LanguageAnalyser {

    private static final String[] FILE_EXTENSIONS = { "c", "i" };
    public static final String DEFAULT_ENCODING = "UTF8";
    public static final String DEFAULT_PREPROCESSOR_CMD = "cc -E";

    @Override
    public String[] fileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String language() {
        return "C";
    }

    @Override
    public APIScope analyse(String sourceFile) throws IOException, ParseError {
        return this.analyse(sourceFile, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public APIScope analyse(String sourceFile, Map<String, Object> options) throws IOException,
            ParseError
    {
        String encoding = null;
        List<String> systemPaths = null;
        String preprocessorCmd = null;

        if (options != null) {
            encoding = (String) options.get("encoding");
            systemPaths = (List<String>) options.get("excludes");
            preprocessorCmd = (String) options.get("preprocessor_cmd");
        }
        if (systemPaths == null) {
            systemPaths = new ArrayList<String>();
        }
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        if (preprocessorCmd == null) {
            preprocessorCmd = DEFAULT_PREPROCESSOR_CMD;
        }

        CharStream input = null;

        if (sourceFile.endsWith(".i")) {
            // this is an already pre-processed file, we can analyse it "as is".
            input = new ANTLRFileStream(sourceFile, encoding);
        } else {
            input = preprocessFile(preprocessorCmd, sourceFile, encoding);
        }

        CLexer lexer = new CLexer(input);
        lexer.systemPaths = systemPaths.toArray(new String[systemPaths.size()]);
        CParser parser = new CParser(new CommonTokenStream(lexer));
        parser.setTreeAdaptor(new IterableTreeAdaptor());
        CTreeWalker walker = new CTreeWalker(language(), sourceFile);

        try {
            Object ast = parser.c_source().getTree();
            walker.walk((IterableTree) ast);
        } catch (RecognitionException e) {
            throw new ParseError(e);
        }

        return walker.globalScope;
    }

    private static final int BUFFER_SIZE = 32 * 1024;

    private CharStream preprocessFile(String preprocessorCmd, String sourceFile, String encoding)
            throws IOException
    {
        CharStream input = null;
        try {
            String cmdLine = String.format("%s '%s'", preprocessorCmd, sourceFile);
            Process process = Runtime.getRuntime().exec(StringUtils.translateCommandline(cmdLine));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
            	throw new IOException("Preprocessor command ended with exit code " + exitCode);
            }
            InputStream is = process.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;
            while ((bytes = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
            }
            input = new ANTLRInputStream(new ByteArrayInputStream(out.toByteArray()), encoding);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        return input;
    }

}
