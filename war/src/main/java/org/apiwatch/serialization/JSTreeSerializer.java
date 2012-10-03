package org.apiwatch.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static final List<Map<String, Object>> toJSTreeData(APIScope rootScope) {
        return toJSTreeData(rootScope, Visibility.SCOPE);
    }

    public static final List<Map<String, Object>> toJSTreeData(APIScope rootScope,
            Visibility threshold)
    {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

        if (rootScope.dependencies.size() > 0) {
            data.add(dependencyData(rootScope.dependencies));
        }
        for (APIScope subScope : rootScope.subScopes) {
            if (subScope.visibility.compareTo(threshold) >= 0) {
                data.add(scopeData(subScope, threshold));
            }
        }
        for (Symbol symbol : rootScope.symbols) {
            if (symbol.visibility.compareTo(threshold) >= 0) {
                data.add(symbolData(symbol, threshold));
            }
        }

        return data;
    }

    private static Map<String, Object> scopeData(APIScope scope, Visibility threshold) {
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("rel", "apiscope_" + scope.visibility);
        attr.put("visibility", scope.visibility);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", scope.name());
        result.put("attr", attr);

        if (scope.dependencies.size() > 0 || scope.subScopes.size() > 0 || scope.symbols.size() > 0) {
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();

            if (scope.dependencies.size() > 0) {
                children.add(dependencyData(scope.dependencies));
            }
            for (APIScope subScope : scope.subScopes) {
                if (subScope.visibility.compareTo(threshold) >= 0) {
                    children.add(scopeData(subScope, threshold));
                }
            }
            for (Symbol symbol : scope.symbols) {
                if (symbol.visibility.compareTo(threshold) >= 0) {
                    children.add(symbolData(symbol, threshold));
                }
            }
            result.put("state", "closed");
            result.put("children", children);
        }

        return result;
    }

    private static Map<String, Object> dependencyData(Set<String> dependencies) {
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("rel", "dependency");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", "dependencies");
        result.put("attr", attr);

        List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();

        for (String depName : dependencies) {
            Map<String, Object> depAttr = new HashMap<String, Object>();
            attr.put("rel", "dependency");
            Map<String, Object> dep = new HashMap<String, Object>();
            result.put("data", depName);
            result.put("attr", depAttr);
            children.add(dep);
        }

        result.put("state", "closed");
        result.put("children", children);
        return result;
    }

    private static Map<String, Object> symbolData(Symbol symbol, Visibility threshold) {
        if (symbol instanceof Type) {
            return typeData((Type) symbol, threshold);
        } else if (symbol instanceof Function) {
            return functionData((Function) symbol, threshold);
        } else if (symbol instanceof Variable) {
            return variableData((Variable) symbol, threshold);
        }
        return null;
    }

    private static Map<String, Object> typeData(Type type, Visibility threshold) {
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("rel", type.getClass().getSimpleName().toLowerCase() + "_" + type.visibility);
        attr.put("visibility", type.visibility);
        attr.put("modifiers", StringUtils.join(type.modifiers, ", "));
        if (type instanceof ComplexType) {
            attr.put("modifiers", StringUtils.join(((ComplexType) type).superTypes, ", "));
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", type.name());
        result.put("attr", attr);

        if (type instanceof ComplexType && ((ComplexType) type).symbols.size() > 0) {
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
            for (Symbol symbol : ((ComplexType) type).symbols) {
                if (symbol.visibility.compareTo(threshold) >= 0) {
                    children.add(symbolData(symbol, threshold));
                }
            }
            result.put("state", "closed");
            result.put("children", children);
        }

        return result;
    }

    private static Map<String, Object> functionData(Function func, Visibility threshold) {
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("rel", "function_" + func.visibility);
        attr.put("visibility", func.visibility);
        attr.put("modifiers", StringUtils.join(func.modifiers, ", "));
        attr.put("return_type", func.returnType);
        attr.put("exceptions", StringUtils.join(func.exceptions, ", "));
        attr.put("has_varargs", func.hasVarArgs);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", func.signature());
        result.put("attr", attr);
        return result;
    }

    private static Map<String, Object> variableData(Variable var, Visibility threshold) {
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("rel", "variable_" + var.visibility);
        attr.put("visibility", var.visibility);
        attr.put("modifiers", StringUtils.join(var.modifiers, ", "));
        attr.put("var_type", var.type);
        attr.put("constraints", var.constraints);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("data", var.name);
        result.put("attr", attr);
        return result;
    }

}
