/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.serialization;

import org.apiwatch.models.APIElement;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.Function;
import org.apiwatch.models.Symbol;
import org.apiwatch.models.SymbolContainer;
import org.apiwatch.models.Variable;

public class SerializationUtils {

    public static void restoreParents(APIElement elt) {
        if (elt instanceof APIScope) {
            for (APIScope scope : ((APIScope) elt).subScopes) {
                scope.parent = elt;
                restoreParents(scope);
            }
        }
        if (elt instanceof SymbolContainer) {
            for (Symbol symbol : ((SymbolContainer) elt).symbols()) {
                symbol.parent = elt;
                restoreParents(symbol);
            }
        }
        if (elt instanceof Function) {
            for (Variable var : ((Function) elt).arguments) {
                var.parent = elt;
            }
        }
    }
    
}
