/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Function extends Symbol {

    public String returnType;
    public List<Variable> arguments;
    public boolean hasVarArgs;
    public Set<String> exceptions;

    public Function(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent, Set<String> modifiers, String returnType, List<Variable> arguments,
            boolean hasVarArgs, Set<String> exceptions)
    {
        super(name, language, sourceFile, visibility, parent, modifiers);
        this.returnType = returnType;
        this.arguments = arguments != null ? arguments : new ArrayList<Variable>();
        this.hasVarArgs = hasVarArgs;
        this.exceptions = exceptions != null ? exceptions : new HashSet<String>();
    }

    @Override
    public String ident() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(IDENT_SEPARATOR);
        sb.append(signature());
        return sb.toString();
    }
    
    private static final String ARGS_SEPARATOR = ", ";
    public String signature() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append('(');
        if (arguments.size() > 0) {
            boolean first = true;
            for (Variable var : arguments) {
                if (first) {
                    first = false;
                } else {
                    sb.append(ARGS_SEPARATOR);
                }
                sb.append(var.type);
            }
            if (hasVarArgs) {
                sb.append("...");
            }
        }
        sb.append(')');
        return sb.toString();
    }
    
    @Override
    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = super.getDiffs(other);

        if (other instanceof Function) {
            Function func = (Function) other;
            if (returnType != null && !returnType.equals(func.returnType)
                    || returnType == null && func.returnType != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "returnType",
                        returnType, func.returnType));
            }
            if (hasVarArgs != func.hasVarArgs) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "hasVarArgs",
                        hasVarArgs, func.hasVarArgs));
            }
            if (exceptions != null && !exceptions.equals(func.exceptions)
                    || exceptions == null && func.exceptions != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "exceptions",
                        exceptions, func.exceptions));
            }
        }

        return diffs;
    }
    
    
    
}
