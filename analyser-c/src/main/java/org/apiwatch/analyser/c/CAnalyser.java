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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.log4j.Logger;
import org.apiwatch.analyser.Analyser;
import org.apiwatch.analyser.LanguageAnalyser;
import org.apiwatch.analyser.Option;
import org.apiwatch.models.APIScope;
import org.apiwatch.util.StringUtils;
import org.apiwatch.util.antlr.IterableTree;
import org.apiwatch.util.antlr.IterableTreeAdaptor;
import org.apiwatch.util.errors.ParseError;

public class CAnalyser implements LanguageAnalyser {

    private static final String PREPROCESSOR_CMD_OPTION = "c_preprocessor_cmd";
    private static final String SYSTEM_INCLUDE_PATHS_OPTION = "c_system_include_paths";
    private static final String[] FILE_EXTENSIONS = { "c" };
    private static final String LANGUAGE = "C";
    private static final String DEFAULT_PREPROCESSOR_CMD = "cc -E";
    private static final Option[] OPTIONS = {
            new Option(
                    PREPROCESSOR_CMD_OPTION,
                    "CMD",
                    "The command that will be used to pre-process C source files. The default is `cc -E`.",
                    null),
            new Option(
                    SYSTEM_INCLUDE_PATHS_OPTION,
                    "PATH",
                    "The API elements defined source files located under these paths will be excluded.",
                    "+") };

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
        return OPTIONS;
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
            encoding = (String) options.get(Analyser.ENCODING_OPTION);
            systemPaths = (List<String>) options.get(SYSTEM_INCLUDE_PATHS_OPTION);
            preprocessorCmd = (String) options.get(PREPROCESSOR_CMD_OPTION);
        }
        if (systemPaths == null) {
            systemPaths = new ArrayList<String>();
        }
        if (encoding == null) {
            encoding = Analyser.DEFAULT_ENCODING;
        }
        if (preprocessorCmd == null) {
            preprocessorCmd = DEFAULT_PREPROCESSOR_CMD;
        }

        CharStream input;

        if (sourceFile.endsWith(".i")) {
            // this is an already pre-processed file, we can analyse it "as is".
            input = new ANTLRFileStream(sourceFile, encoding);
        } else {
            input = preprocessFile(preprocessorCmd, sourceFile, encoding);
        }

        try {
            CLexer lexer = new CLexer(input, systemPaths);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // here we force the lexer to process all tokens before giving them to the parser
            // this is mandatory so the 'headers' list is completely filled when parsing
            tokens.fill(); 
            CParser parser = new CParser(tokens, lexer.headers);
            parser.setTreeAdaptor(new IterableTreeAdaptor());
            Object ast = parser.c_source().getTree();

            CTreeWalker walker = new CTreeWalker(language(), sourceFile);
            walker.walk((IterableTree) ast);

            return walker.globalScope;
        } catch (RecognitionException e) {
            throw new ParseError(e);
        }
    }

    private static final int BUFFER_SIZE = 32 * 1024;

     CharStream preprocessFile(String preprocessorCmd, String sourceFile, String encoding)
            throws IOException
    {
        try {
            String cmdLine = String.format("%s \"%s\"", preprocessorCmd, sourceFile);
            Process process = Runtime.getRuntime().exec(StringUtils.translateCommandline(cmdLine));
            ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes;
            while ((bytes = process.getInputStream().read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
            }

            ByteArrayOutputStream err = new ByteArrayOutputStream(BUFFER_SIZE);
            while ((bytes = process.getErrorStream().read(buffer)) != -1) {
                err.write(buffer, 0, bytes);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                Logger log = Logger.getLogger(getClass());
                log.error("Preprocessor command `" + cmdLine + "` ended with exit code " + exitCode);
                if (err.size() > 0) {
                    log.error(err);
                }
            }
            ANTLRInputStream input;
            input = new ANTLRInputStream(new ByteArrayInputStream(out.toByteArray()), encoding);
            input.name = sourceFile;
            return input;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

}
