/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.java;

import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.tree.Tree;
import org.apiwatch.analyser.APITreeWalker;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.ComplexType;
import org.apiwatch.models.Function;
import org.apiwatch.models.Variable;
import org.apiwatch.models.Visibility;
import org.apiwatch.util.antlr.IterableTree;

public class JavaTreeWalker extends APITreeWalker {

    public JavaTreeWalker(String language, String sourceFile) {
        super(language, sourceFile);
    }

    /**
     * Walk a Java Tree and fill "globalScope" with generic API information from
     * it.
     */
    @Override
    public void walk(IterableTree ast) {

        if (ast == null) {
            return;
        }

        switch (ast.getType()) {

        case JavaParser.JAVA_SOURCE:
            __root(ast);
            break;

        case JavaParser.IMPORT:
            __import(ast);
            break;

        case JavaParser.ENUM:
        case JavaParser.CLASS:
        case JavaParser.INTERFACE:
            __object(ast);
            break;

        case JavaParser.EXTENDS_CLAUSE:
        case JavaParser.IMPLEMENTS_CLAUSE:
            __extends(ast);
            break;

        case JavaParser.CLASS_TOP_LEVEL_SCOPE:
        case JavaParser.INTERFACE_TOP_LEVEL_SCOPE:
        case JavaParser.ENUM_TOP_LEVEL_SCOPE:
            walkChildren(ast);
            break;

        case JavaParser.VAR_DECLARATION:
            __variable(ast);
            break;

        case JavaParser.CONSTRUCTOR_DECL:
        case JavaParser.FUNCTION_METHOD_DECL:
        case JavaParser.VOID_METHOD_DECL:
            __function(ast);
            break;

        case JavaParser.FORMAL_PARAM_LIST:
            walkChildren(ast);
            break;

        case JavaParser.FORMAL_PARAM_VARARG_DECL:
            ((Function) apiStack.peek()).hasVarArgs = true;
            /* fallback to next case option */
        case JavaParser.FORMAL_PARAM_STD_DECL:
            __argument(ast);
            break;

        case JavaParser.THROWS_CLAUSE:
            __exceptions(ast);
            break;

        default:
            break;
        }
    }

    /**
     * Handle root node ^(JAVA_SOURCE ...)
     */
    private void __root(IterableTree ast) {
        IterableTree packageDecl = ast.firstChildOfType(JavaParser.PACKAGE);
        String packageName;
        if (packageDecl != null) {
            packageName = _dottedName(packageDecl.getChild(0));
        } else {
            packageName = "";
        }

        APIScope packageScope = new APIScope(packageName, language, _source(ast), Visibility.PUBLIC,
                apiStack.peek(), null, null, null);
        ((APIScope) apiStack.peek()).subScopes.add(packageScope);

        apiStack.push(packageScope);
        walkChildren(ast, JavaParser.IMPORT);

        /* only one of these three will actually be walked */
        walk(ast.firstChildOfType(JavaParser.CLASS));
        walk(ast.firstChildOfType(JavaParser.INTERFACE));
        walk(ast.firstChildOfType(JavaParser.ENUM));
        apiStack.pop();
    }

    /**
     * Handle import nodes ^(IMPORT ...)
     */
    private void __import(IterableTree ast) {
        for (IterableTree node : ast) {
            if (node.getType() != JavaParser.STATIC) {
                String importName = _dottedName(node);
                ((APIScope) apiStack.peek()).dependencies.add(importName);
                return;
            }
        }
    }

    /**
     * Handle typedef nodes ^(ENUM|CLASS|INTERFACE ...)
     */
    private void __object(IterableTree ast) {

        IterableTree modList = ast.firstChildOfType(JavaParser.MODIFIER_LIST);

        Set<String> modifiers = _javaModifiers(modList);
        Visibility visibility = _javaVisibility(modList);

        String name = ast.firstChildOfType(JavaParser.IDENT).getText();

        IterableTree genericTypeParams = ast.firstChildOfType(JavaParser.GENERIC_TYPE_PARAM_LIST);
        if (genericTypeParams != null) {
            name += "<" + _genericsTypeList(genericTypeParams) + ">";
        }

        ComplexType complexType = new ComplexType(name, language, _source(ast), visibility,
                apiStack.peek(), modifiers, null, null);

        if (apiStack.peek() instanceof ComplexType) {
            ((ComplexType) apiStack.peek()).symbols.add(complexType);
        } else if (apiStack.peek() instanceof APIScope) {
            ((APIScope) apiStack.peek()).symbols.add(complexType);
        }

        apiStack.push(complexType);
        walk(ast.firstChildOfType(JavaParser.EXTENDS_CLAUSE));
        walk(ast.firstChildOfType(JavaParser.IMPLEMENTS_CLAUSE));

        /* only one of these three will actually be walked */
        walk(ast.firstChildOfType(JavaParser.CLASS_TOP_LEVEL_SCOPE));
        walk(ast.firstChildOfType(JavaParser.INTERFACE_TOP_LEVEL_SCOPE));
        walk(ast.firstChildOfType(JavaParser.ENUM_TOP_LEVEL_SCOPE));
        apiStack.pop();
    }

    /**
     * Handle supertype nodes ^(EXTENDS_CLAUSE|IMPLEMENTS_CLAUSE ...)
     */
    private void __extends(IterableTree ast) {
        for (IterableTree child : ast) {
            ((ComplexType) apiStack.peek()).superTypes.add(_typeName(child));
        }
    }

    /**
     * Handle variable declaration nodes ^(VAR_DECLARATION ...)
     */
    private void __variable(IterableTree ast) {
        IterableTree modList = ast.firstChildOfType(JavaParser.MODIFIER_LIST);
        
        Set<String> modifiers = _javaModifiers(modList);
        Visibility visibility = _javaVisibility(modList);
        String type = _typeName(ast.firstChildOfType(JavaParser.TYPE));

        for (IterableTree varDecl : ast.firstChildOfType(JavaParser.VAR_DECLARATOR_LIST)) {
            String varName = varDecl.firstChildOfType(JavaParser.IDENT).getText();
            Variable var = new Variable(varName, language, _source(ast), visibility,
                    apiStack.peek(), modifiers, type, null);
            ((ComplexType) apiStack.peek()).symbols.add(var);
        }
    }

    /**
     * Handle function nodes ^(CONSTRUCTOR_DECL|FUNCTION_METHOD_DECL|VOID_METHOD_DECL ...)
     */
    private void __function(IterableTree ast) {
        IterableTree modList = ast.firstChildOfType(JavaParser.MODIFIER_LIST);
        
        Set<String> modifiers = _javaModifiers(modList);
        Visibility visibility = _javaVisibility(modList);
        Tree funcNameAst = ast.firstChildOfType(JavaParser.IDENT);
        String name; 
        if (funcNameAst != null) {
            name = funcNameAst.getText();
        } else {
            /* constructor nodes do not have a IDENT child, we take the name of the parent class */
            name = apiStack.peek().name;
        }
        String returnType = _typeName(ast.firstChildOfType(JavaParser.TYPE));

        Function func = new Function(name, language, _source(ast), visibility, apiStack.peek(),
                modifiers, returnType, null, false, null);
        ((ComplexType) apiStack.peek()).symbols.add(func);

        apiStack.push(func);
        walkChildren(ast, JavaParser.FORMAL_PARAM_LIST);
        walkChildren(ast, JavaParser.THROWS_CLAUSE);
        apiStack.pop();
    }

    /**
     * Handle function arg nodes ^(FORMAL_PARAM_STD_DECL ...)
     */
    private void __argument(IterableTree ast) {
        Set<String> modifiers = _javaModifiers(ast.firstChildOfType(JavaParser.LOCAL_MODIFIER_LIST));
        String type = _typeName(ast.firstChildOfType(JavaParser.TYPE));
        String name = ast.firstChildOfType(JavaParser.IDENT).getText();

        Variable arg = new Variable(name, language, _source(ast), Visibility.SCOPE,
                apiStack.peek(), modifiers, type, null);

        ((Function) apiStack.peek()).arguments.add(arg);
    }

    private void __exceptions(IterableTree ast) {
        for (IterableTree child : ast) {
            /* java forbids generics on exceptions, we can do this safely. */
            String exceptionClass = _dottedName(child);
            ((Function) apiStack.peek()).exceptions.add(exceptionClass);
        }
    }

    /* ======================================================================= */
    /* UTILITY METHODS */
    /* ======================================================================= */

    private static String _dottedName(IterableTree ast) {
        StringBuilder buffer = new StringBuilder();
        switch (ast.getType()) {
        case JavaParser.IDENT:
            buffer.append(ast.getText());
            break;
        case JavaParser.DOT:
            buffer.append(_dottedName(ast.getChild(0)));
            buffer.append(".");
            buffer.append(_dottedName(ast.getChild(1)));
            break;
        default:
            break;
        }
        return buffer.toString();
    }

    /**
     * Extract the "visibility" information from a MODIFIER_LIST node.
     */
    private static Visibility _javaVisibility(IterableTree ast) {
        if (ast != null) {
            for (IterableTree child : ast) {
                switch (child.getType()) {
                case JavaParser.PUBLIC:
                    return Visibility.PUBLIC;
                case JavaParser.PRIVATE:
                    return Visibility.PRIVATE;
                case JavaParser.PROTECTED:
                    return Visibility.PROTECTED;
                default:
                    continue;
                }
            }
        }
        return Visibility.SCOPE;
    }

    /**
     * Extract the modifiers from a MODIFIER_LIST node (without the "visibility"
     * information)
     */
    private static Set<String> _javaModifiers(IterableTree ast) {
        Set<String> modifiers = new HashSet<String>();
        if (ast != null) {
            for (IterableTree child : ast) {
                switch (child.getType()) {
                case JavaParser.PUBLIC:
                case JavaParser.PRIVATE:
                case JavaParser.PROTECTED:
                    break;
                case JavaParser.AT:
                    modifiers.add("@" + _dottedName(child.getChild(0)));
                    break;
                default:
                    modifiers.add(child.getText());
                }
            }
        }
        return modifiers;
    }

    /**
     * Extract the Type information from a TYPE node.
     */
    private static String _typeName(IterableTree ast) {
        if (ast == null) {
            return null;
        }
        StringBuilder typeName = new StringBuilder();

        if (ast.getType() == JavaParser.TYPE) {

            IterableTree qualifiedType = ast.firstChildOfType(JavaParser.QUALIFIED_TYPE_IDENT);

            if (qualifiedType != null) {
                StringBuilder qualifiedTypeName = new StringBuilder();
                for (int i = 0; i < qualifiedType.getChildCount(); i++) {
                    qualifiedTypeName.append("." + qualifiedType.getChild(i).getText());

                    IterableTree generics = qualifiedType.getChild(i).firstChildOfType(
                            JavaParser.GENERIC_TYPE_ARG_LIST);
                    if (generics != null) {
                        qualifiedTypeName.append('<');
                        qualifiedTypeName.append(_genericsTypeList(generics));
                        qualifiedTypeName.append('>');
                    }
                }
                /* to remove the leading dot */
                typeName.append(qualifiedTypeName.substring(1));
            } else {
                typeName.append(ast.getChild(0).getText());
            }

            Tree arrayDecl = ast.firstChildOfType(JavaParser.ARRAY_DECLARATOR_LIST);
            if (arrayDecl != null) {
                for (int i = 0; i < arrayDecl.getChildCount(); i++) {
                    typeName.append("[]");
                }
            }

        } else if (ast.getType() == JavaParser.QUESTION) {
            typeName.append("?");

            if (ast.getChildCount() > 0) {
                /* ^(QUESTION ^(EXTENDS|SUPER type)) */
                IterableTree extendsAst = ast.getChild(0);
                typeName.append(" ");
                typeName.append(extendsAst.getText());
                typeName.append(" ");
                typeName.append(_typeName(extendsAst.getChild(0)));
            }
        }

        return typeName.toString();
    }

    /**
     * Extract the generic type list from a type.
     * 
     * Such as Blah<GenericType1, GenericType2>
     */
    private static String _genericsTypeList(IterableTree ast) {

        StringBuilder generics = new StringBuilder();

        for (IterableTree child : ast) {
            generics.append(", ");
            switch (child.getType()) {
            case JavaParser.QUESTION:
                generics.append(child.getText());
                break;
            case JavaParser.TYPE:
                generics.append(_typeName(child));
                break;
            case JavaParser.IDENT:
                generics.append(child.getText());
                IterableTree bounds = child.firstChildOfType(JavaParser.EXTENDS_BOUND_LIST);
                if (bounds != null) {
                    generics.append(" extends ");
                    for (IterableTree bound : bounds) {
                        generics.append(_typeName(bound));
                        generics.append(" & ");
                    }
                    /* we remove the trailing " & " */
                    generics.delete(generics.length() - " & ".length(), generics.length());
                }
                break;
            default:
                break;
            }
        }

        return generics.substring(2); /* remove the leading ", " */
    }
    
    private static final String SOURCE_FILE_FORMAT = "%s:%d"; 
    private String _source(Tree ast) {
        return String.format(SOURCE_FILE_FORMAT, sourceFile, ast.getLine());
    }
}
