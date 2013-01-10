/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComplexType extends Type implements SymbolContainer {

    public Set<String> superTypes;
    public Set<Symbol> symbols;
    
    public ComplexType(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent, Set<String> modifiers, Set<String> superTypes, Set<Symbol> symbols)
    {
        super(name, language, sourceFile, visibility, parent, modifiers);
        this.superTypes = superTypes != null ? superTypes : new HashSet<String>();
        this.symbols = symbols != null ? symbols : new HashSet<Symbol>();
    }

    @Override
    public Collection<Symbol> symbols() {
        return symbols;
    }
    
    @Override
    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = super.getDiffs(other);

        if (other instanceof ComplexType) {
            ComplexType type = (ComplexType) other;
            if (superTypes != null && !superTypes.equals(type.superTypes)
                    || superTypes == null && type.superTypes != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "superTypes",
                        superTypes, type.superTypes));
            }
        }

        return diffs;
    }
    
}
