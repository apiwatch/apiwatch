/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.serialization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.ComplexType;
import org.apiwatch.models.Function;
import org.apiwatch.models.Symbol;
import org.apiwatch.models.Type;
import org.apiwatch.models.Variable;
import org.apiwatch.models.Visibility;

public class JSTreeSerializer {

    public static final String toJSTreeData(APIScope rootScope) {
        return toJSTreeData(rootScope, Visibility.SCOPE);
    }

    public static final String toJSTreeData(APIScope rootScope, Visibility threshold) {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        if (rootScope.dependencies.size() > 0) {
            sb.append(dependencyData(rootScope.dependencies) + ",");
        }
        for (APIScope subScope : rootScope.subScopes) {
            if (subScope.visibility.compareTo(threshold) >= 0) {
                sb.append(scopeData(subScope, threshold) + ",");
            }
        }
        for (Symbol symbol : rootScope.symbols) {
            if (symbol.visibility.compareTo(threshold) >= 0) {
                sb.append(symbolData(symbol, threshold) + ",");
            }
        }
        sb.append(']');

        return sb.toString();
    }

    private static String scopeData(APIScope scope, Visibility threshold) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        result.append("data:\"" + scope.name() + "\",");
        result.append("attr:");
        result.append('{');
        result.append("rel:\"apiscope_" + scope.visibility + "\",");
        result.append("visibility:\"" + scope.visibility + "\",");
        result.append("},");
        if (scope.dependencies.size() > 0 || scope.subScopes.size() > 0 || scope.symbols.size() > 0) {
            result.append("state:\"closed\",");
            result.append("children:");
            result.append('[');
            if (scope.dependencies.size() > 0) {
                result.append(dependencyData(scope.dependencies) + ",");
            }
            for (APIScope subScope : scope.subScopes) {
                if (subScope.visibility.compareTo(threshold) >= 0) {
                    result.append(scopeData(subScope, threshold) + ",");
                }
            }
            for (Symbol symbol : scope.symbols) {
                if (symbol.visibility.compareTo(threshold) >= 0) {
                    result.append(symbolData(symbol, threshold) + ",");
                }
            }
            result.append("],");
        }
        result.append('}');
        return result.toString();
    }

    private static String dependencyData(Set<String> dependencies) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        result.append("data:\"dependencies\",");
        result.append("attr:{rel:\"dependency\"},");
        result.append("state:\"closed\",");
        result.append("children:[");
        for (String depName : dependencies) {
            result.append('{');
            result.append("data:\"" + depName + "\",");
            result.append("attr:{rel:\"dependency\"},");
            result.append("},");
        }
        result.append("],}");
        return result.toString();
    }

    private static String symbolData(Symbol symbol, Visibility threshold) {
        if (symbol instanceof Type) {
            return typeData((Type) symbol, threshold);
        } else if (symbol instanceof Function) {
            return functionData((Function) symbol, threshold);
        } else if (symbol instanceof Variable) {
            return variableData((Variable) symbol, threshold);
        }
        return null;
    }

    private static String typeData(Type type, Visibility threshold) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        result.append("data:\"" + type.name() + "\",");
        result.append("attr:{");
        result.append("rel:\"" + type.getClass().getSimpleName().toLowerCase() + "_"
                + type.visibility + "\",");
        result.append("visibility:\"" + type.visibility + "\",");
        result.append("modifiers:\"" + StringUtils.join(type.modifiers, ", ") + "\",");
        if (type instanceof ComplexType) {
            result.append("super_types:\""
                    + StringUtils.join(((ComplexType) type).superTypes, ", ") + "\",");
        }
        result.append("},"); // end attr
        if (type instanceof ComplexType && ((ComplexType) type).symbols.size() > 0) {
            result.append("state:\"closed\",");
            result.append("children:[");
            for (Symbol symbol : ((ComplexType) type).symbols) {
                if (symbol.visibility.compareTo(threshold) >= 0) {
                    result.append(symbolData(symbol, threshold) + ",");
                }
            }
            result.append("],"); // end children
        }
        result.append('}'); // end root
        return result.toString();
    }

    private static String functionData(Function func, Visibility threshold) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        result.append("data:\"" + func.signature() + "\",");
        result.append("attr:{");
        result.append("rel:\"function_" + func.visibility + "\",");
        result.append("visibility:\"" + func.visibility + "\",");
        result.append("modifiers:\"" + StringUtils.join(func.modifiers, ", ") + "\",");
        result.append("exceptions:\"" + StringUtils.join(func.exceptions, ", ") + "\",");
        result.append("return_type:\"" + func.returnType + "\",");
        result.append("has_varargs:" + func.hasVarArgs + ",");
        result.append("},"); // end attr
        result.append('}'); // end root
        return result.toString();
    }

    private static String variableData(Variable var, Visibility threshold) {
        StringBuilder result = new StringBuilder();
        result.append('{');
        result.append("data:\"" + var.name + "\",");
        result.append("attr:{");
        result.append("rel:\"variable_" + var.visibility + "\",");
        result.append("visibility:\"" + var.visibility + "\",");
        result.append("modifiers:\"" + StringUtils.join(var.modifiers, ", ") + "\",");
        result.append("type:\"" + var.type + "\",");
        result.append("data:\"" + var.constraints + "\",");
        result.append("},"); // end attr
        result.append('}'); // end root
        return result.toString();
    }

}
