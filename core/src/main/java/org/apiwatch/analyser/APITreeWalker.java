/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser;

import java.util.Stack;

import org.apiwatch.models.APIElement;
import org.apiwatch.models.APIScope;
import org.apiwatch.util.antlr.IterableTree;


public abstract class APITreeWalker {
    
    /*-----------------------------------------------------------------------*/
    /* FIELDS */
    /*-----------------------------------------------------------------------*/
    public final String language;
    public final String sourceFile;
    public final Stack<APIElement> apiStack = new Stack<APIElement>();
    public final APIScope globalScope;

    /*-----------------------------------------------------------------------*/
    /* CONSTRUCTORS */
    /*-----------------------------------------------------------------------*/
    public APITreeWalker(String language, String sourceFile) {
        this.language = language;
        this.sourceFile = sourceFile;
        globalScope = new APIScope();
        globalScope.language = language;
        globalScope.name = language;
        apiStack.push(globalScope);
    }
    
    /*-----------------------------------------------------------------------*/
    /* MAIN PROCESS - TO BE IMPLEMENTED BY SUBCLASSES */
    /*-----------------------------------------------------------------------*/
    public abstract void walk(IterableTree ast);
    
    /*-----------------------------------------------------------------------*/
    /* UTIL METHODS */
    /*-----------------------------------------------------------------------*/
    protected void walkChildren(IterableTree ast) {
        for (IterableTree child : ast) {
            walk(child);
        }
    }
    
    protected void walkChildren(IterableTree ast, int tokenType) {
        for (IterableTree child : ast) {
            if (child.getType() == tokenType) {
                walk(child);
            }
        }
    }
    
}
