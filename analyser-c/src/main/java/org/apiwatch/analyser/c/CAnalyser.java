/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;

import java.io.IOException;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apiwatch.analyser.LanguageAnalyser;
import org.apiwatch.models.APIScope;
import org.apiwatch.util.antlr.IterableTree;
import org.apiwatch.util.errors.ParseError;

public class CAnalyser implements LanguageAnalyser {

    private static final String[] FILE_EXTENSIONS = { "c", "h", "i" };
    
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

    @Override
    public APIScope analyse(String sourceFile, Map<String, Object> options) throws IOException,
            ParseError
    {
        String encoding = null;
        String[] systemPaths = null;
        
        if (options != null) {
            encoding = (String) options.get("encoding");
            systemPaths = (String[]) options.get("systemPaths");
        }
        if (encoding == null) {
            encoding = "UTF8";
        }

        CLexer lexer = new CLexer(new ANTLRFileStream(sourceFile, encoding));
        lexer.systemPaths = systemPaths;
        CParser parser = new CParser(new CommonTokenStream(lexer));
        parser.setTreeAdaptor(new IterableTree.Adaptor());
        CTreeWalker walker = new CTreeWalker(language(), sourceFile);
        
        try {
            Object ast = parser.c_source().getTree();
            walker.walk((IterableTree) ast);
        } catch (RecognitionException e) {
            throw new ParseError(e);
        }

        return walker.globalScope;
    }

}
