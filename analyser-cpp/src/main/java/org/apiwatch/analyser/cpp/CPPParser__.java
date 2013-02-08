/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.cpp;

import java.util.Set;

import org.antlr.runtime.TokenStream;
import org.apiwatch.analyser.cpp.CPPSymbols.CPPSymbol;
import org.apiwatch.analyser.cpp.ParserConstants.FunctionParserState;
import org.apiwatch.analyser.cpp.ParserConstants.FunctionSpecifier;
import org.apiwatch.analyser.cpp.ParserConstants.QualifiedItem;
import org.apiwatch.analyser.cpp.ParserConstants.StorageClass;
import org.apiwatch.analyser.cpp.ParserConstants.TypeQualifier;
import org.apiwatch.analyser.cpp.ParserConstants.TypeSpecifier;

public class CPPParser__ {

    TokenStream tokens;
    
    CPPSymbols symbols;
    Set<CPPSymbol> templateParameterScope;
    Set<CPPSymbol> externalScope;

    int anyType;
    int anyNonType;
    int lineNo;

    boolean typeDef = false; // For typedef
    boolean friend = false; // For friend
    int storageClass; // For storage class
    int typeQualifier; // For type qualifier
    int typeSpecifier; // For type specifier
    int functionSpecifier; // For declaration specifier
    FunctionParserState functionParserState;

    StringBuilder qualifierPrefix;
    String enclosingClass;

    int assignStmtRHSFound;
    boolean inParameterList;
    boolean KandR; // used to distinguish old K & R parameter definitions
    boolean inReturn;
    boolean isAddress;
    boolean isPointer;
    
    int statementTrace;

    public CPPParser__(TokenStream tokens, int statementTrace) {
        this.tokens = tokens;
        this.statementTrace = statementTrace;
        symbols = new CPPSymbols();
        functionParserState = FunctionParserState.None;
        
        typeDef = false;
        friend = false;
        storageClass = StorageClass.Invalid;
        typeQualifier = TypeQualifier.Invalid;
        typeSpecifier = TypeSpecifier.Invalid;
        functionSpecifier = FunctionSpecifier.Invalid;
        qualifierPrefix = new StringBuilder();
        enclosingClass = "";
        
        assignStmtRHSFound = 0;
        inParameterList = false;
        KandR = false;
        inReturn = false;
        isAddress = false;
        isPointer = false;
        lineNo = 0;
    }

    boolean itemIs(int qiFlags /* ORed combination of flags */, int lookahead) {
        int qi = qualifiedItemType(lookahead);
        return (qi & qiFlags) != 0;
    }
    
    boolean itemIs(int qiFlags) {
        return itemIs(qiFlags, 0);
    }

    /**
     * This is an important function, but will be replaced with an enhanced predicate in the future,
     * once predicates and/or predicate guards can contain loops.
     * 
     * Scan past the ::T::B:: to see what lies beyond. Return QualifiedItem.Type if the qualified
     * item can pose as type name. Note that T::T is NOT a type; it is a constructor. Also, class T
     * { ... T...} yields the enclosed T as a ctor. This is probably a type as I separate out the
     * constructor defs/decls, I want it consistent with T::T.
     * 
     * In the below examples, I use A,B,T and example types, and a,b as example ids. In the below
     * examples, any A or B may be a qualified template, i.e., A<...>
     * 
     * T::T outside of class T yields QualifiedItem.Ctor
     * 
     * T<...>::T outside of class T yields QualifiedItem.Ctor
     * 
     * T inside of class T {...} yields QualifiedItem.Ctor
     * 
     * T, ::T, A::T outside of class T yield QualifiedItem.Type
     * 
     * a, ::a, A::B::a yield QualifiedItem.Id
     * 
     * a::b yields QualifiedItem.Invalid
     * 
     * ::operator, operator, A::B::operator yield QualifiedItem.Operator
     * 
     * A::*, A::B::* yield QualifiedItem.PtrMember
     * 
     * ::*, * yield QualifiedItem.Invalid
     * 
     * ::~T, ~T, A::~T yield QualifiedItem.Dtor
     * 
     * ~a, ~A::a, A::~T::, ~T:: yield QualifiedItem.Invalid
     */
    int qualifiedItemType(int lookahead) {
        MutableInt k = new MutableInt(lookahead + 1);
        int finalTypeIdx = 0;
        // Skip leading "::"
        while (tokens.LT(k.val).getType() == CPPLexer.Scope) {
            k.val++;
        }
        // Skip sequences of T:: or T<...>::
        // DW 11/02/05 Note that tokens.LT(k).getType() is not a "type" but a type of token, eg. ID
        while (tokens.LT(k.val).getType() == CPPLexer.ID && isTypeName(tokens.LT(k.val).getText())) {

            // If this type is the same as the last type, then ctor
            if (finalTypeIdx != 0 && tokens.LT(k.val).getText().equals(tokens.LT(finalTypeIdx).getText())) {
                // Like T::T
                // As an extra check, do not allow T::T::
                if (tokens.LT(k.val + 1).getType() == CPPLexer.Scope) {
                    return QualifiedItem.Invalid;
                } else {
                    return QualifiedItem.Ctor;
                }
            }

            // Record this as the most recent type seen in the series
            finalTypeIdx = k.val;

            // Skip this token
            k.val++;

            // Skip over any template qualifiers <...>
            // I believe that "T<..." cannot be anything valid but a template
            if (tokens.LT(k.val).getType() == CPPLexer.LessThan) {
                if (!skipTemplateQualifiers(k)) {
                    return QualifiedItem.Invalid;
                }
                // k has been updated to token following <...>
            }

            // Skip any "::" and keep going
            if (tokens.LT(k.val).getType() == CPPLexer.Scope) {
                k.val++;
            } else {
                // Otherwise series terminated -- last ID in the sequence was a type
                // Return ctor if last type is in containing class
                // We already checked for T::T inside loop
                if (enclosingClass.equals(tokens.LT(finalTypeIdx).getText())) {
                    return QualifiedItem.Ctor;
                } else {
                    return QualifiedItem.Type;
                }
            }
        }

        // tokens.LT(k) is not an ID, or it is an ID but not a typename.
        switch (tokens.LT(k.val).getType()) {
        case CPPLexer.ID:
            // ID but not a typename
            if (tokens.LT(k.val + 1).getType() == CPPLexer.Scope) {
                // Do not allow id::
                return QualifiedItem.Invalid;
            } else if (enclosingClass.equals(tokens.LT(k.val).getText())) {
                // Like class T T()
                return QualifiedItem.Ctor;
            } else {
                if (isTypeName(tokens.LT(k.val).getText())) {
                    return QualifiedItem.Type;
                } else {
                    return QualifiedItem.Var; // DW 19/03/04 was qiVar Could be function, qiFun?
                }
            }
        case CPPLexer.Tilde:
            // check for dtor
            if (tokens.LT(k.val + 1).getType() == CPPLexer.ID && isTypeName(tokens.LT(k.val + 1).getText())
                    && tokens.LT(k.val + 2).getType() != CPPLexer.Scope) {
                // Like ~B or A::B::~B
                // Also (incorrectly?) matches ::~A.
                return QualifiedItem.Dtor;
            } else {
                // ~a or ~A::a is QualifiedItem.Invalid
                return QualifiedItem.Invalid;
            }
        case CPPLexer.Star:
            // Like A::*
            if (finalTypeIdx == 0) {
                // Do not allow * or ::*
                // Haven't seen a type yet
                return QualifiedItem.Invalid;
            } else {
                return QualifiedItem.PtrMember;
            }
        case CPPLexer.Operator:
            // Like A::operator, ::operator, or operator
            return QualifiedItem.Operator;
        default:
            // Something that neither starts with :: or ID, or
            // a :: not followed by ID, operator, ~, or *
            return QualifiedItem.Invalid;
        }
    }

    /**
     * Skip over <...>.  This correctly handles nested <> and (), e.g:
     *    <T>
     *    < (i>3) >
     *    < T2<...> >
     * but not
     *    < i>3 >
     *  
     * On input, kInOut is the index of the "<"
     * On output, 
     *     if the return is true, then 
     *        kInOut is the index of the token after ">"
     *     else
     *        kInOut is unchanged
     */
    boolean skipTemplateQualifiers(MutableInt kInOut) {
        // Start after "<"
        MutableInt k = new MutableInt(kInOut.val + 1);
        // scan to end of <...>
        while (tokens.LT(k.val).getType() != CPPLexer.GreaterThan) {
            switch (tokens.LT(k.val).getType()) {
            case CPPLexer.EOF:
                return false;
            case CPPLexer.LessThan:
                if (!skipTemplateQualifiers(k)) {
                    return false;
                }
                break;
            case CPPLexer.LParen:
                if (!skipNestedParens(k)) {
                    return false;
                }
                break;
            default:
                k.val++; // skip everything else
                break;
            }
            if (k.val > ParserConstants.MaxTemplateTokenScan) {
                return false;
            }
        }
        // Update output argument to point past ">"
        kInOut.val = k.val + 1;
        return true;
    }

    /**
     * Skip over (...). This correctly handles nested (), e.g:
     * 
     * (i>3, (i>5))
     * 
     * On input, kInOut is the index of the "(" On output, if the return is true, then kInOut is the
     * index of the token after ")" else kInOut is unchanged
     */
    boolean skipNestedParens(MutableInt kInOut) {
        // Start after "("
        MutableInt k = new MutableInt(kInOut.val + 1);

        while (tokens.LT(k.val).getType() != CPPLexer.RParen) {
            // scan to end of (...)
            switch (tokens.LT(k.val).getType()) {
            case CPPLexer.EOF:
                return false;
            case CPPLexer.LParen:
                if (!skipNestedParens(k)) {
                    return false;
                }
                break;
            default:
                k.val++; // skip everything else
                break;
            }
            if (k.val > ParserConstants.MaxTemplateTokenScan) {
                return false;
            }
        }

        // Update output argument to point past ")"
        kInOut.val = k.val + 1;
        return true;
    }

    /**
     * Return true if 's' can pose as a type name
     */
    boolean isTypeName(String s) {
        // To look for any type name only
        return symbols.isDefined(s, CPPSymbol.otTypedef, CPPSymbol.otEnum, CPPSymbol.otClass,
                CPPSymbol.otStruct, CPPSymbol.otUnion);
    }

    /**
     * Return true if the most recently added matching entry 's' can pose as a type name
     */
    boolean firstIsTypeName(String s) {
        // To look for any type name only
        return symbols.isDefined(s, CPPSymbol.otTypedef, CPPSymbol.otEnum, CPPSymbol.otClass,
                CPPSymbol.otStruct, CPPSymbol.otUnion);
    }

    /**
     * Return true if 's' is a class name (or a struct which is a class with all members public).
     */
    boolean isClassName(String s) {
        // To look for any type name only
        return symbols.isDefined(s, CPPSymbol.otClass, CPPSymbol.otStruct, CPPSymbol.otUnion);
    }


    void beginDeclaration() {
    }

    void endDeclaration() {
    }

    void beginFunctionDefinition() {
        functionParserState = FunctionParserState.Name;
    }

    void endFunctionDefinition() {
        // Remove parameter scope
        symbols.exitScope();
        functionParserState = FunctionParserState.None;
    }

    void beginConstructorDefinition() {
        functionParserState = FunctionParserState.Name;
    }

    void endConstructorDefinition() {
        symbols.exitScope();
        functionParserState = FunctionParserState.None;
    }

    void beginConstructorDeclaration(String ctor) {
    }

    void endConstructorDeclaration() {
    }

    void beginDestructorDefinition() {
        functionParserState = FunctionParserState.Name;
    }

    void endDestructorDefinition() {
        symbols.exitScope();
        functionParserState = FunctionParserState.None;
    }

    void beginDestructorDeclaration(String dtor) {
    }

    void endDestructorDeclaration() {
    }

    void beginParameterDeclaration() {
    }

    void beginFieldDeclaration() {
    }

    void declarationSpecifier(boolean td, boolean fd, int sc, int tq, int ts, int fs) {
        typeDef = td; // For typedef
        friend = fd; // For friend
        storageClass = sc;
        typeQualifier = tq;
        typeSpecifier = ts;
        functionSpecifier = fs;
    }

    /**
     * Symbols from declarators are added to the symbol table here. The symbol is also added to
     * external scope or whatever the current scope is, in the symbol table. See list of object
     * types below.
     */
    void declaratorID(String id, int qi) {
        // if already in symbol table as a class, don't add
        // of course, this is incorrect as you can rename
        // classes by entering a new scope, but our limited
        // example basically keeps all types globally visible.
        if (isTypeName(id)) {
            return;
        } else if (qi == QualifiedItem.Type) {
            // DW 04/08/03 Scoping not fully implemented
            // Typedefs all recorded in 'external' scope and therefor never removed
            symbols.defineInScope(id, CPPSymbol.otTypedef, externalScope);
        } else if (qi == QualifiedItem.Fun) {
            symbols.define(id, CPPSymbol.otFunction); // Add to current scope
        } else {
            symbols.define(id, CPPSymbol.otVariable); // Add to current scope
        }
    }

    void declaratorArray() {
    }

    void declaratorParameterList() {
        symbols.enterScope();
    }

    void declaratorEndParameterList(boolean definition) {
        if (!definition) {
            symbols.exitScope();
        }
    }

    void functionParameterList() {
        symbols.enterScope();
        // DW 25/3/97 change flag from function to parameter list
        // DW 07/03/07 Taken out because it caused a problem when function declared within function
        // and
        // it was not actually used anywhere. (See in_parameter_list)
        // functionParserState = FunctionParserState.Params;
    }

    void functionEndParameterList(boolean definition) {
        if (!definition) {
            // If this parameter list is not in a definition then Reduce currentScope (higher level)
            symbols.exitScope();
        } else {
            // Change flag from parameter list to body of definition
            functionParserState = FunctionParserState.Block;
            // endFunctionDefinition will remove the parameters from scope
        }
    }

    void enterNewLocalScope() {
        symbols.enterScope();
    }

    void exitLocalScope() {
        symbols.exitScope();
    }

    void enterExternalScope() {
        // Set template parameter scope - Not used for now
        templateParameterScope = symbols.currentScope();
        symbols.enterScope();

        // define builtin "std" namespace in external scope
        externalScope = symbols.currentScope();
        externalScope.add(new CPPSymbol("std", CPPSymbol.otTypedef));

        // enter new scope again, all following symbols will be treated as "local"
        symbols.enterScope();
    }

    void exitExternalScope() {
        symbols.exitScope(); // Exit externalScope
        symbols.exitScope(); // Exit templateParameterScope
    }

    void classForwardDeclaration(String tag, int ts) {
        if (isTypeName(tag)) {
            // if already in symbol table as a class, don't add
            // of course, this is incorrect as you can rename
            // classes by entering a new scope, but our limited
            // example basically keeps all types globally visible.
            return;
        }
        switch (ts) {
        case TypeSpecifier.Struct:
            symbols.defineInScope(tag, CPPSymbol.otStruct, externalScope);
            break;
        case TypeSpecifier.Union:
            symbols.defineInScope(tag, CPPSymbol.otUnion, externalScope);
            break;
        case TypeSpecifier.Class:
            symbols.defineInScope(tag, CPPSymbol.otClass, externalScope);
            break;
        }
    }

    void beginClassDefinition(String tag, int typeSpecifier) {
        if (isTypeName(tag)) {
            // if already in symbol table as a class, don't add
            // of course, this is incorrect as you can rename
            // classes by entering a new scope, but our limited
            // example basically keeps all types globally visible.
            return;
        }

        switch (typeSpecifier) {
        case TypeSpecifier.Struct:
            symbols.defineInScope(tag, CPPSymbol.otStruct, externalScope);
            break;
        case TypeSpecifier.Union:
            symbols.defineInScope(tag, CPPSymbol.otUnion, externalScope);
            break;
        case TypeSpecifier.Class:
            symbols.defineInScope(tag, CPPSymbol.otClass, externalScope);
            break;
        }

        qualifierPrefix.append(tag);
        qualifierPrefix.append("::");

        // add all member type symbols into the global scope (not correct, but
        // will work for most code).
        // This symbol lives until the end of the file
        symbols.enterScope();
    }

    void endClassDefinition() {
        symbols.enterScope();
        // remove final T:: from A::B::C::T::
        // upon backing out of last class, qualifierPrefix is set to ""
        int lastIndex = qualifierPrefix.length() - 1;
        while (lastIndex > 0 && qualifierPrefix.charAt(lastIndex) == ':') {
            // trim all ':' characters at the end
            lastIndex--;
        }
        while (lastIndex > 0 && qualifierPrefix.charAt(lastIndex) != ':') {
            // decrement until begining or ':' character
            lastIndex--;
        }
        if (lastIndex > 0) {
            // correction when not at beginning of string to keep the last 2 ':' chars
            lastIndex += 1;
        }
        qualifierPrefix.delete(lastIndex, qualifierPrefix.length());
    }

    void beginEnumDefinition(String name) {
        // DW 26/3/97 Set flag for new class
        // add all enum tags into the global scope (not correct, but will work for most code).
        // This symbol lives until the end of the file
        symbols.defineInScope(name, CPPSymbol.otEnum, externalScope);
    }

    void endEnumDefinition() {
    }

    void enumElement(String name) {
        symbols.define(name, CPPSymbol.otVariable); // Add to current scope
    }

    void templateTypeParameter(String t) {
        // DW 11/06/03 Symbol saved in templateParameterScope (0)
        // as a temporary measure until scope is implemented fully
        // This symbol lives until the end of the file
        // DW 10/08/05 Replaced to make template parameters local
        symbols.defineInScope(t, CPPSymbol.otTypedef, externalScope);
    }

    void beginTemplateDeclaration() {
        symbols.enterScope();
    }

    void endTemplateDeclaration() {
        symbols.exitScope();
    }

    void beginTemplateDefinition() {
    }

    void endTemplateDefinition() {
    }

    void beginTemplateParameterList() {
        // DW 26/05/03 To scope template parameters
        symbols.enterScope();
    }

    void endTemplateParameterList() {
        // DW 26/05/03 To end scope template parameters
        symbols.exitScope();
    }

    void exceptionBeginHandler() {
    }

    void exceptionEndHandler() {
        // remove parm elements from the handler scope
        symbols.exitScope();
    }

    void end_of_stmt() {
    }

}
