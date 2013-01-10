/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apiwatch.analyser.APITreeWalker;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.ComplexType;
import org.apiwatch.models.Function;
import org.apiwatch.models.Symbol;
import org.apiwatch.models.SymbolContainer;
import org.apiwatch.models.Variable;
import org.apiwatch.models.Visibility;
import org.apiwatch.util.antlr.IterableTree;

public class CTreeWalker extends APITreeWalker {

    public static final String CALLBACK_MODIFIER = "callback";

    public CTreeWalker(String language, String sourceFile) {
        super(language, sourceFile);
    }

    @Override
    public void walk(IterableTree ast) {
        if (ast == null) {
            return;
        }
        switch (ast.getType()) {
        case CParser.C_SOURCE:
            walkChildren(ast);
            break;
        case CParser.TYPE_DECLARATION:
            typeDeclaration(ast);
            break;
        case CParser.VARIABLE_DECLARATION:
        case CParser.FIELD:
            variable(ast);
            break;
        case CParser.FUNCTION_DECLARATION:
        case CParser.FUNCTION_DEFINITION:
            function(ast);
            break;
        case CParser.PARAM:
            functionArg(ast);
            break;
        case CParser.ENUMERATOR:
            enumerator(ast);
            break;
        default:
            break;
        }
    }

    /**
     * Handles explicit type declarations like "typedef"s
     */
    private void typeDeclaration(IterableTree ast) {
        String sourceFile = ast.getText();
        ComplexType type = new ComplexType(null, language, sourceFile, Visibility.PUBLIC,
                apiStack.peek(), null, null, null);
        ((SymbolContainer) apiStack.peek()).symbols().add(type);
        apiStack.add(type);
        // we call declarator first to initialize the name of the type
        __declarator(ast.firstChildOfType(CParser.DECLARATOR));
        __declSpecifiers(ast.firstChildOfType(CParser.DECL_SPECIFIERS));
        apiStack.pop();
    }

    /**
     * Handles variables declarations and struct/union fields
     */
    private void variable(IterableTree ast) {

        String sourceFile;
        if (ast.getType() == CParser.VARIABLE_DECLARATION && ast.getText() != null) {
            // only get source file if we have a VARIABLE_DECLARATION node
            sourceFile = ast.getText();
        } else {
            sourceFile = apiStack.peek().sourceFile;
        }

        // in C, there can be multiple variables declared at the same time:
        //
        // static struct inet_packet_s pack1, **pack2, pack3[4];
        //
        // we need a temporary variable to record the type information before creating the actual
        // variables.
        Variable dummy;

        // in C, it is possible to declare a type and a variable of the same type all in one same
        // instruction. The only way to know if this is the case, is to check whether there is a
        // complex type definition in the DECL_SPECIFIERS node.
        if (__hasComplexTypeDefinition(ast)) {
            // if this is the case, we create a ComplexType and use this type for the following
            // declared variable(s)
            ComplexType type = new ComplexType(null, language, sourceFile, Visibility.PUBLIC,
                    apiStack.peek(), null, null, null);
            ((SymbolContainer) apiStack.peek()).symbols().add(type);

            apiStack.add(type);
            __declSpecifiers(ast.firstChildOfType(CParser.DECL_SPECIFIERS));
            apiStack.pop();

            // the dummy variable will be of the type just previously created.
            String variableType = type.name;
            for (String supType : type.superTypes) {
                // we add the 'struct' or 'enum' prefix here, if any
                variableType = supType + " " + variableType;
            }

            dummy = new Variable(null, language, sourceFile, Visibility.PUBLIC, apiStack.peek(),
                    new HashSet<String>(type.modifiers), variableType, null);
        } else {
            // The temp variable will be directly initialized with DECL_SPECIFIERS
            dummy = new Variable(null, language, sourceFile, Visibility.PUBLIC, apiStack.peek(),
                    null, null, null);
            // we put the variable on top of the stack
            apiStack.add(dummy);
            __declSpecifiers(ast.firstChildOfType(CParser.DECL_SPECIFIERS));
            // then remove it
            apiStack.pop();
        }

        IterableTree bitCount = ast.firstChildOfType(CParser.BIT_COUNT);
        if (bitCount != null) {
            // structure field size is specified here
            String bits = bitCount.getText();
            dummy.constraints = bits + " bit" + ("1".equals(bits) ? "" : "s");
        }

        for (IterableTree child : ast) {
            if (child.getType() == CParser.DECLARATOR) {
                // we clone the dummy variable here to record the actual one
                // (there can be multiple variables declared in the same instruction)
                Variable var = new Variable(dummy);

                apiStack.add(var);
                __declarator(child);
                apiStack.pop();

                // we add the variable as child of the parent symbol container
                // (root scope or structure)
                ((SymbolContainer) apiStack.peek()).symbols().add(var);
            }
        }
    }

    /**
     * This map allows to keep track of functions declarations and definitions in the AST
     */
    private Map<String, Function> functions = new HashMap<String, Function>();

    /**
     * Handles function declarations and definitions.
     * 
     * This method also keeps the "function" hash table up to date. The function definitions have
     * priority over the declarations.
     * <ul>
     * <li>When a definition is encountered after a previous declaration, the declaration must be
     * replaced by the definition in the API structure (apiStack).</li>
     * <li>When a declaration is encountered after a previous definition, the declaration should be
     * ignored.</li>
     * </ul>
     */
    private void function(IterableTree ast) {
        if (ast == null) {
            return;
        }

        String sourceFile = ast.getText() != null ? ast.getText() : apiStack.peek().sourceFile;

        Function func = new Function(null, language, sourceFile, Visibility.PUBLIC,
                apiStack.peek(), null, null, null, false, null);

        apiStack.add(func);
        __declSpecifiers(ast.firstChildOfType(CParser.DECL_SPECIFIERS));
        __declarator(ast.firstChildOfType(CParser.DECLARATOR));
        apiStack.pop();

        if (ast.getType() == CParser.FUNCTION_DEFINITION) {
            if (functions.containsKey(func.name)) {
                // we are in a FUNCTION_DEFINITION and we already parsed a FUNCTION_DECLARATION of
                // the same name, we need to remove completely the
                Iterator<Symbol> it = ((APIScope) apiStack.peek()).symbols().iterator();
                while (it.hasNext()) {
                    Symbol symbol = it.next();
                    if (symbol.name != null && symbol.name.equals(func.name)) {
                        it.remove();
                        break;
                    }
                }
            }
            ((APIScope) apiStack.peek()).symbols().add(func);
        } else {
            if (!functions.containsKey(func.name)) {
                ((APIScope) apiStack.peek()).symbols().add(func);
            }
        }
        functions.put(func.name, func);
    }

    /**
     * See {@link #functionArg(IterableTree)}. Shortcut for <tt>__functionArg(ast, false)</tt>.
     */
    private void functionArg(IterableTree ast) {
        functionArg(ast, false);
    }

    /**
     * Handles function arguments. When an argument type is a pointer to a callback function, this
     * method is called recursively to resolve the actual signature of the callback function. The
     * signature is used as the actual type of the argument.
     * 
     * @param nested
     *            <tt>true</tt> if the argument is a nested argument of a callback function.
     * 
     * @return the type of the function argument.
     */
    private String functionArg(IterableTree ast, boolean nested) {
        if (ast == null) {
            return null;
        }
        Variable arg = new Variable(null, language, apiStack.peek().sourceFile, Visibility.PUBLIC,
                apiStack.peek(), null, null, null);

        if (!nested && apiStack.peek() instanceof Function) {
            // Nested arguments can be found when a function argument is a pointer to a callback
            // function.
            // We only add the element to the API tree if we are not walking a nested argument.
            ((Function) apiStack.peek()).arguments.add(arg);
        }
        apiStack.add(arg);
        __declSpecifiers(ast.firstChildOfType(CParser.DECL_SPECIFIERS));
        IterableTree decl = ast.firstChildOfType(CParser.DECLARATOR);
        arg.type += __declaratorPointer(decl);

        if (!nested) {
            // we don't care about the name of the param if we are in a nested argument
            arg.name = __declaratorName(decl);
        }

        if (decl != null) {
            IterableTree nestedArgs = decl.firstChildOfType(CParser.FUNCTION_ARGS);
            if (nestedArgs != null) {
                // arg is a pointer to a function, we remove the extra '*' at the end of the return
                // type which is not a "real" pointer indicator
                arg.type = arg.type.replaceAll("\\*$", "");
                // we add a special modifier to identify a callback function
                arg.modifiers.add(CALLBACK_MODIFIER);
                arg.type += '(';
                for (IterableTree child : nestedArgs) {
                    arg.type += functionArg(child, true) + ", ";
                }
                if (nestedArgs.getChildCount() > 0) {
                    // remove the final comma
                    arg.type = arg.type.substring(0, arg.type.length() - 2);
                }
                arg.type += ')';
            }
        }
        apiStack.pop();

        return arg.type;
    }

    /**
     * Handles enum members.
     */
    private void enumerator(IterableTree ast) {
        ComplexType parentEnum = (ComplexType) apiStack.peek();
        Variable enumerator = new Variable(ast.getText(), language, parentEnum.sourceFile,
                Visibility.PUBLIC, parentEnum, null, "enum " + parentEnum.name, null);

        IterableTree assign = ast.firstChildOfType(CParser.ASSIGN);
        if (assign != null) {
            enumerator.constraints = "constant = " + assign.getText();
        }

        parentEnum.symbols().add(enumerator);
    }

    /**
     * Handles DECL_SPECIFIERS nodes. These nodes generaly carry the type information of variables,
     * functions and structs/unions
     */
    private void __declSpecifiers(IterableTree ast) {
        if (ast == null) {
            return;
        }
        for (IterableTree child : ast) {
            if (child.getType() == CParser.MODIFIER) {
                ((Symbol) apiStack.peek()).modifiers.add(child.getText());
            } else if (child.getType() == CParser.TYPE) {
                if (child.getChildCount() > 0) {
                    IterableTree subChild = child.getChild(0);
                    if (subChild.getType() == CParser.STRUCTURE
                            || subChild.getType() == CParser.ENUM) {
                        __complexTypeDefinition(subChild);
                    }
                } else {
                    __type(child);
                }
            }
        }
    }

    /**
     * Handles TYPE nodes.
     */
    private void __type(IterableTree ast) {
        if (apiStack.peek() instanceof Function) {
            if (((Function) apiStack.peek()).returnType == null) {
                ((Function) apiStack.peek()).returnType = ast.getText();
            } else {
                ((Function) apiStack.peek()).returnType += " " + ast.getText();
            }
        } else if (apiStack.peek() instanceof ComplexType) {
            ((ComplexType) apiStack.peek()).superTypes.add(ast.getText());
        } else if (apiStack.peek() instanceof Variable) {
            if (((Variable) apiStack.peek()).type == null) {
                ((Variable) apiStack.peek()).type = ast.getText();
            } else {
                ((Variable) apiStack.peek()).type += " " + ast.getText();
            }
        }
    }

    /**
     * Handles DECLARATOR nodes. In the AST, these nodes carry the name of variables, functions and
     * such. They also carry pointer/array symbols and complex type signatures in the case of
     * callback types.
     */
    private void __declarator(IterableTree ast) {
        if (ast == null) {
            return;
        }
        apiStack.peek().name = __declaratorName(ast);

        IterableTree params = ast.firstChildOfType(CParser.FUNCTION_ARGS);

        if (apiStack.peek() instanceof Function) {
            Function func = (Function) apiStack.peek();
            func.hasVarArgs = ast.firstChildOfType(CParser.ELLIPSIS) != null;
            func.returnType += __declaratorPointer(ast);
            if (params != null) {
                walkChildren(params);
            }
        } else if (apiStack.peek() instanceof Variable) {
            Variable var = (Variable) apiStack.peek();
            var.type += __declaratorPointer(ast);
            if (params != null) {
                // var is typed as a pointer to a function, we remove the extra '*' at the end of
                // the return type which is not a "real" pointer indicator
                var.type = var.type.replaceAll("\\*$", "");
                var.type += '(';
                for (IterableTree child : params) {
                    var.type += functionArg(child, true) + ", ";
                }
                if (params.getChildCount() > 0) {
                    // remove the final comma
                    var.type = var.type.substring(0, var.type.length() - 2);
                }
                var.type += ')';
            }
        }

    }

    /**
     * Handles struct/union/enum definitions.
     */
    private void __complexTypeDefinition(IterableTree ast) {
        if (ast == null) {
            return;
        }

        IterableTree structName = ast.firstChildOfType(CParser.IDENTIFIER);
        if (apiStack.peek() instanceof Variable) {
            // variable/field typed with "struct something"
            if (structName != null) {
                ((Variable) apiStack.peek()).type = ast.getText() + " " + structName.getText();
            }
        } else if (apiStack.peek() instanceof Function) {
            // function returning a "struct something"
            if (structName != null) {
                ((Function) apiStack.peek()).returnType = ast.getText() + " "
                        + structName.getText();
            }
        } else {
            ComplexType type = (ComplexType) apiStack.peek();

            // add 'struct', 'union' or 'enum' as super type
            type.superTypes.add(ast.getText());

            if (structName != null) {
                if (type.name == null) {
                    // set the structure name here if not already defined from typedef above
                    type.name = structName.getText();
                } else {
                    // struct name is defined in typedef, we record the actual struct name after it
                    // for tracking reasons
                    type.superTypes.add(structName.getText());
                }
            }

            // parse the struct/union/enum fields
            walkChildren(ast);

            if (type.name == null) {
                // if the type name is still 'null' (anonymous struct/union)
                // we forge a name from the types of the fields.
                type.name = __forgeTypeName(type);
            }
        }
    }

    /**
     * Checks if a variable type is a complex definition such as a struc/union or enum.
     */
    private boolean __hasComplexTypeDefinition(IterableTree ast) {
        if (ast == null) {
            return false;
        }
        IterableTree declSpecs = ast.firstChildOfType(CParser.DECL_SPECIFIERS);
        if (declSpecs != null) {
            for (IterableTree child : declSpecs) {
                if (child.getType() == CParser.TYPE) {
                    if (child.firstChildOfType(CParser.STRUCTURE) != null
                            || child.firstChildOfType(CParser.ENUM) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Extracts the IDENTIFIER information from a DECLARATOR node. In the case of complex pointer
     * specifications, DECLARATOR nodes can be nested like this:
     * 
     * (DECLARATOR POINTER (DECLARATOR POINTER IDENTIFIER))
     */
    private String __declaratorName(IterableTree ast) {
        if (ast == null || ast.getType() != CParser.DECLARATOR) {
            return null;
        }
        IterableTree ident = ast.firstChildOfType(CParser.IDENTIFIER);
        if (ident != null) {
            return ident.getText();
        } else {
            return __declaratorName(ast.firstChildOfType(CParser.DECLARATOR));
        }
    }

    /**
     * Extracts the POINTER information from a DECLARATOR node. In the case of complex pointer
     * specifications, DECLARATOR nodes can be nested like this:
     * 
     * (DECLARATOR POINTER (DECLARATOR POINTER IDENTIFIER))
     */
    private String __declaratorPointer(IterableTree ast) {
        String result = "";
        if (ast != null && ast.getType() == CParser.DECLARATOR) {
            for (IterableTree child : ast) {
                switch (child.getType()) {
                case CParser.POINTER:
                case CParser.ARRAY_DECL:
                    result += child.getText();
                    break;
                case CParser.DECLARATOR:
                    result += __declaratorPointer(child);
                    break;
                default:
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Forges a type from a ComplexType using the types of its contained symbols.
     */
    private String __forgeTypeName(ComplexType type) {
        String name = "$";
        for (Symbol s : type.symbols()) {
            if (s instanceof Variable) {
                name += "_" + ((Variable) s).type;
            } else if (s instanceof Function) {
                name += "_" + ((Function) s).returnType;
            }
        }
        return name;
    }
}
