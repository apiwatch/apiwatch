/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Symbol extends APIElement {

    public Set<String> modifiers;

    public Symbol(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent, Set<String> modifiers)
    {
        super(name, language, sourceFile, visibility, parent);
        this.modifiers = modifiers != null ? modifiers : new HashSet<String>();
    }

    @Override
    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = super.getDiffs(other);

        if (other instanceof Symbol) {
            Symbol symbol = (Symbol) other;
            if (modifiers != null && !modifiers.equals(symbol.modifiers) || modifiers == null
                    && symbol.modifiers != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "modifiers",
                        modifiers, symbol.modifiers));
            }
        }

        return diffs;
    }

}
