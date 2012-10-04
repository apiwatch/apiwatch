/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
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

public class APIScope extends APIElement implements SymbolContainer {

    public static final String ROOT_PATH = new APIScope().path();

    public Set<String> dependencies;
    public Set<APIScope> subScopes;
    public Set<Symbol> symbols;

    public APIScope() {
        this(null, null, null, null, null, null, null, null);
    }

    public APIScope(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent, Set<String> dependencies, Set<APIScope> subScopes,
            Set<Symbol> symbols)
    {
        super(name, language, sourceFile, visibility, parent);
        this.dependencies = dependencies != null ? dependencies : new HashSet<String>();
        this.subScopes = subScopes != null ? subScopes : new HashSet<APIScope>();
        this.symbols = symbols != null ? symbols : new HashSet<Symbol>();
    }

    public void update(APIScope other) {
        name = name != null ? name : other.name;
        language = language != null ? language : other.language;
        sourceFile = sourceFile != null ? sourceFile : other.sourceFile;
        visibility = visibility != null ? visibility : other.visibility;
        dependencies.addAll(other.dependencies);
        symbols.addAll(other.symbols);
        for (APIScope otherSubScope : other.subScopes) {
            if (subScopes.contains(otherSubScope)) {
                for (APIScope subScope : subScopes) {
                    if (subScope.equals(otherSubScope)) {
                        subScope.update(otherSubScope);
                    }
                }
            } else {
                subScopes.add(otherSubScope);
            }
        }
    }

    @Override
    public Collection<Symbol> symbols() {
        return symbols;
    }

    @Override
    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = super.getDiffs(other);

        if (other instanceof APIScope) {
            APIScope scope = (APIScope) other;
            if (dependencies != null && !dependencies.equals(scope.dependencies)
                    || dependencies == null && scope.dependencies != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "dependencies",
                        dependencies, scope.dependencies));
            }
        }

        return diffs;
    }

}
