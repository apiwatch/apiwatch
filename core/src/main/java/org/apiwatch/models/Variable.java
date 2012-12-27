/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.List;
import java.util.Set;

public class Variable extends Symbol {

    public String type;
    public String constraints;
    
    public Variable(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent, Set<String> modifiers, String type, String constraints)
    {
        super(name, language, sourceFile, visibility, parent, modifiers);
        this.type = type;
        this.constraints = constraints;
    }
    
    public Variable(Variable other) {
        super(other.name, other.language, other.sourceFile, other.visibility, other.parent, other.modifiers);
        this.type = other.type;
        this.constraints = other.constraints;
    }

    @Override
    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = super.getDiffs(other);

        if (other instanceof Variable) {
            Variable var = (Variable) other;
            if (type != null && !type.equals(var.type)
                    || type == null && var.type != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "type",
                        type, var.type));
            }
            if (constraints != null && !constraints.equals(var.constraints)
                    || constraints == null && var.constraints != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "constraints",
                        constraints, var.constraints));
            }
        }

        return diffs;
    }
    
    

}
