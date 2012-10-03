/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;

import org.apiwatch.analyser.APITreeWalker;
import org.apiwatch.util.antlr.IterableTree;

public class CTreeWalker extends APITreeWalker {

    public CTreeWalker(String language, String sourceFile) {
        super(language, sourceFile);
    }



    @Override
    public void walk(IterableTree ast) {
        // TODO Auto-generated method stub
        
    }

}
