/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.cpp;

import org.apiwatch.analyser.APITreeWalker;
import org.apiwatch.util.antlr.IterableTree;

public class CPPTreeWalker extends APITreeWalker {

    public static final String CALLBACK_MODIFIER = "callback";

    public CPPTreeWalker(String language, String sourceFile) {
        super(language, sourceFile);
    }

    @Override
    public void walk(IterableTree ast) {
        if (ast == null) {
            return;
        }
        switch (ast.getType()) {
        default:
            break;
        }
    }


}
