/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

grammar C;

options {
    backtrack = true;
    memoize = true;
    language = Java;
    k = 2;
    output = AST;
    ASTLabelType = IterableTree;
}

tokens {
    // imaginary tokens for AST
    C_SOURCE;
    VARIABLE_DECLARATION;
    FUNCTION_DECLARATION;
    FUNCTION_DEFINITION;
    DECLARATOR;
    ARRAY_DECL;
    FUNCTION_PARAMS;
    PARAM;
    DECL_SPECIFIERS;
    MODIFIER;
    TYPE;
    STRUCTURE;
    FIELD;
    CAST;
    CALL;
    ARRAY_ACCESS;
    IDENTIFIER_LIST;
    POINTER;
    ENUMERATOR;
	PRE_INCR;
	PRE_DECR;
	POST_INCR;
	POST_DECR;
}

@header {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
 
import org.apiwatch.analyser.c.Header;
import org.apiwatch.util.antlr.IterableTree;
}

@lexer::header {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;
 
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apiwatch.analyser.c.Header;
}

@members {
    
private boolean isTypeDef = false;
/* to track typedefs and recognize type names from identifiers */
private Stack<HashSet<String>> scopeStack;

public List<Header> headers;

boolean isInSystemHeader(int line) {
    if (this.headers == null || this.headers.isEmpty()) {
        return false;
    }
    for (int i = 0; i < this.headers.size(); i++) {
        if (this.headers.get(i).line > line) {
            return this.headers.get(i - 1).isSystemHeader;
        }
    }
    return false;
}

String getSourceFile(int line) {
    if (this.headers == null || this.headers.isEmpty()) {
        return "";
    }
    
    Header header = null;
    int realLine = 0;
    String sourceFile = null;
    
    for (int i = 0; i < this.headers.size(); i++) {
        header = this.headers.get(i);
        if (header.line > line) {
            header = this.headers.get(i - 1);
            break;
        }
    }
    
    realLine = header.sourceLine + (line - header.line - 1);
    
    return header.sourceFile + ":" + realLine;
}


boolean isTypeName(String name) {
    if (name.startsWith("__builtin")) {
        // GCC builtins
        return true;
    }
    for (HashSet<String> scope : scopeStack) {
        if (scope.contains(name)) {
            return true;
        }
    }
    return false;
}
    
    

}

@lexer::members {

public String[] systemPaths;
public List<Header> headers = new ArrayList<Header>(); 
static Pattern PREPROC_LINE = Pattern.compile("#\\s*(\\d+)\\s*\"(.+?)\"");

private void recordHeader(int line, String text) {
    boolean isSystemHeader = false;
    text = text.replace("\\\\", "\\");
    int sourceLine = 0;
    String sourceFile = null;
    
    Matcher match = PREPROC_LINE.matcher(text);
    if (match.find()) {
        sourceLine = Integer.valueOf(match.group(1));
        sourceFile = match.group(2);
        for (String path : this.systemPaths) {
            if (text.contains(path)) {
                isSystemHeader = true;
                break;
            }
        }
        this.headers.add(new Header(line, isSystemHeader, sourceLine, sourceFile));
    }
}
    
}

//////////////////////////////
// P A R S E R    R U L E S //
//////////////////////////////

c_source
@init { 
    scopeStack.add(new HashSet<String>());
}
@after {
    scopeStack.pop();
}
  : external_declaration* -> ^(C_SOURCE external_declaration*)
  ;

/** Either a function definition or any other kind of C decl/def.
 *  The LL(*) analysis algorithm fails to deal with this due to
 *  recursion in the declarator rules.  I'm putting in a
 *  manual predicate here so that we don't backtrack over
 *  the entire function.  Further, you get a better error
 *  as errors within the function itself don't make it fail
 *  to predict that it's a function.  Weird errors previously.
 *  Remember: the goal is to avoid backtrack like the plague
 *  because it makes debugging, actions, and errors harder.
 *
 *  Note that k=1 results in a much smaller predictor for the 
 *  fixed lookahead; k=2 made a few extra thousand lines. ;)
 *  I'll have to optimize that in the future.
 */
external_declaration
options {k=1;}
  : (declaration_specifiers? declarator declaration* LCURLY )=> function_definition
  | declaration
  ;

function_definition
@init {
    scopeStack.add(new HashSet<String>());
    int line = $function_definition.start.getLine();
    String sourceFile = getSourceFile(line);
}
@after {
    scopeStack.pop();
}                                      //K&R style                  // ANSI style
  : declaration_specifiers? declarator (declaration+ block_statement|block_statement)
        -> {isInSystemHeader(line)}? // remove from AST
        -> ^(FUNCTION_DEFINITION[sourceFile] declaration_specifiers? declarator declaration*)
  ;

declaration
@init {
    isTypeDef = false;
    int line = $declaration.start.getLine();
    String sourceFile = this.getSourceFile(line);
}                                                                  // special case, looking for typedef
  : TYPEDEF declaration_specifiers? {isTypeDef = true;} init_declarator_list compiler_directive* SEMI
                          -> {isInSystemHeader(line)}? // remove from AST
                          -> ^(TYPEDEF[sourceFile] declaration_specifiers? init_declarator_list)
  | declaration_specifiers i=init_declarator_list? compiler_directive* SEMI
        -> {isInSystemHeader(line)}? // remove from AST
        -> {$i.isFunction}? ^(FUNCTION_DECLARATION[sourceFile] declaration_specifiers init_declarator_list?)
        ->                  ^(VARIABLE_DECLARATION[sourceFile] declaration_specifiers init_declarator_list?)
  ;

declaration_specifiers
  : compiler_directive* decl_specifier+  -> ^(DECL_SPECIFIERS decl_specifier+)
  ;

init_declarator_list returns [boolean isFunction]
  : i=init_declarator {$isFunction=$i.isFunction;} (COMMA! init_declarator)*
  ;

init_declarator returns [boolean isFunction]
  : d=declarator {$isFunction=$d.isFunction;} (ASSIGN! initializer!)?
  ;

decl_specifier
  : storage_class_specifier -> ^(MODIFIER storage_class_specifier)
  | type_specifier          -> ^(TYPE type_specifier)
  | type_qualifier          -> ^(MODIFIER type_qualifier)
  ;

storage_class_specifier
  : EXTERN
  | STATIC
  | AUTO
  | REGISTER
  ;

type_specifier
  : VOID
  | CHAR
  | SHORT
  | INT
  | LONG
  | FLOAT
  | DOUBLE
  | SIGNED
  | UNSIGNED
  | struct_or_union_specifier
  | enum_specifier
  | type_id
  ;

type_id
    : {isTypeName(input.LT(1).getText())}? IDENTIFIER
    ;

struct_or_union_specifier
options {k=3;}
@init { 
    scopeStack.add(new HashSet<String>());
}
@after {
    scopeStack.pop();
}
  : struct_or_union IDENTIFIER? LCURLY struct_declaration_list RCURLY
                     -> ^(STRUCTURE[$struct_or_union.text] IDENTIFIER? struct_declaration_list)
  | struct_or_union IDENTIFIER
                     -> ^(STRUCTURE[$struct_or_union.text] IDENTIFIER)
  ;

struct_or_union
  : STRUCT
  | UNION
  ;

struct_declaration_list
  : struct_declaration+
  ;

struct_declaration
  : specifier_qualifier_list struct_declarator_list SEMI
  		-> ^(FIELD specifier_qualifier_list struct_declarator_list)
  ;

specifier_qualifier_list
  : specifier_qualifer+     -> ^(TYPE specifier_qualifer+)
  ;

specifier_qualifer
  :	type_qualifier 
  | type_specifier
  ;

struct_declarator_list
  : struct_declarator (COMMA! struct_declarator)*
  ;

struct_declarator
  : declarator (COLON constant_expression)?
  | COLON constant_expression
  ;

enum_specifier
options {k=3;}
  : ENUM LCURLY enumerator_list RCURLY             -> ^(ENUM enumerator_list)
  | ENUM IDENTIFIER LCURLY enumerator_list RCURLY  -> ^(ENUM IDENTIFIER enumerator_list)
  | ENUM IDENTIFIER                                -> ^(ENUM IDENTIFIER)
  ;

enumerator_list
  : enumerator (COMMA! enumerator)* 
  ;

enumerator
  : IDENTIFIER (ASSIGN constant_expression)?  -> ^(ENUMERATOR IDENTIFIER)
  ;

type_qualifier
  : CONST
  | INLINE
  | VOLATILE
  ;

declarator returns [boolean isFunction]
  : compiler_directive* pointer? d=direct_declarator {$isFunction=$d.isFunction;}
  | compiler_directive* pointer {$isFunction=false;}
  ;

direct_declarator returns [boolean isFunction]
  : IDENTIFIER {
	    /* record this identifier as a typename for the current scope */
	    if (isTypeDef && !(scopeStack == null || scopeStack.isEmpty())) {
	        scopeStack.peek().add($IDENTIFIER.text);
	    }
    } s=declarator_suffix*   {$isFunction=$s.isFunction;}  
                     -> ^(DECLARATOR IDENTIFIER declarator_suffix*)
  | LPAREN declarator RPAREN s=declarator_suffix* {$isFunction=$s.isFunction;}
                     -> ^(DECLARATOR declarator declarator_suffix*)
  ;

declarator_suffix returns [boolean isFunction]
  : LBRACK constant_expression RBRACK                {$isFunction=false;} -> ^(ARRAY_DECL constant_expression)
  | LBRACK RBRACK                                    {$isFunction=false;} -> ^(ARRAY_DECL)
  | LPAREN parameter_type_list RPAREN compiler_directive* {$isFunction=true;}  -> ^(FUNCTION_PARAMS parameter_type_list)
  | LPAREN identifier_list RPAREN compiler_directive*     {$isFunction=true;}  -> ^(FUNCTION_PARAMS identifier_list)
  | LPAREN RPAREN compiler_directive*                     {$isFunction=true;}  -> ^(FUNCTION_PARAMS)
  ;

compiler_directive
  : attribute_specifier
  | inline_assembly
  ;

attribute_specifier
  :  ATTRIBUTE LPAREN LPAREN attribute_list? RPAREN RPAREN
  ;

attribute_list
  : attribute (COMMA attribute)*
  ;

attribute
  : IDENTIFIER LPAREN IDENTIFIER RPAREN
  | IDENTIFIER LPAREN IDENTIFIER COMMA attribute_parameter_list RPAREN
  | IDENTIFIER LPAREN attribute_parameter_list? RPAREN
  | IDENTIFIER
  ;

inline_assembly
  : ASM VOLATILE? 
           LPAREN 
              STRING_LITERAL+ 
              (COLON (STRING_LITERAL (LPAREN IDENTIFIER RPAREN)? COMMA?)* )* 
           RPAREN
  ;

attribute_parameter_list
  : expression (COMMA! expression)*
  ;

pointer
  : STAR type_qualifier* pointer? -> ^(POINTER type_qualifier* pointer?)
  ;

parameter_type_list
  : parameter_list (COMMA! ELLIPSIS)?
  ;

parameter_list
  : parameter_declaration (COMMA! parameter_declaration)*
  ;

parameter_declaration
  : declaration_specifiers concrete_or_abstract_declarator* 
          -> ^(PARAM declaration_specifiers concrete_or_abstract_declarator*)
  ;

identifier_list
  : IDENTIFIER (COMMA! IDENTIFIER)*
  ;

type_name
  : specifier_qualifier_list abstract_declarator?
  ;

concrete_or_abstract_declarator
  : declarator
  | abstract_declarator
  ;

abstract_declarator
  : pointer direct_abstract_declarator?
  | direct_abstract_declarator
  ;

direct_abstract_declarator
  : (LPAREN abstract_declarator RPAREN | abstract_declarator_suffix) abstract_declarator_suffix*
  ;

abstract_declarator_suffix
  : LBRACK RBRACK
  | LBRACK constant_expression RBRACK
  | LPAREN RPAREN
  | LPAREN parameter_type_list RPAREN
  ;
  
initializer
  : assignment_expression
  | LCURLY initializer_list COMMA? RCURLY -> initializer_list
  ;

initializer_list
  : initializer (COMMA! initializer)*
  ;

// E x p r e s s i o n s

argument_expression_list
  : assignment_expression (COMMA! assignment_expression)*
  ;

additive_expression
  : multiplicative_expression ((PLUS^|MINUS^) multiplicative_expression)*
  ;

multiplicative_expression
  : cast_expression ((STAR^|DIV^|MOD^) cast_expression)*
  ;

cast_expression
  : LPAREN type_name RPAREN cast_expression -> ^(CAST type_name cast_expression)
  | unary_expression
  ;

unary_expression
  : postfix_expression
  | INCR unary_expression -> ^(PRE_INCR unary_expression)
  | DECR unary_expression -> ^(PRE_DECR unary_expression)
  | AND cast_expression
  | STAR^ cast_expression
  | PLUS^ cast_expression
  | MINUS^ cast_expression
  | NOT^ cast_expression
  | LOGICAL_NOT^ cast_expression
  | SIZEOF^ unary_expression
  | SIZEOF^ LPAREN! type_name RPAREN!
  ;

postfix_expression
  : primary_expression (LBRACK expression RBRACK)+               -> ^(ARRAY_ACCESS primary_expression expression+)
  | primary_expression (LPAREN RPAREN)+                          -> ^(CALL primary_expression+)
  | primary_expression (LPAREN argument_expression_list RPAREN)+ -> ^(CALL primary_expression argument_expression_list+)
  | primary_expression (DOT IDENTIFIER)+                         -> ^(DOT primary_expression IDENTIFIER+)
  | primary_expression (ARROW IDENTIFIER)+                       -> ^(ARROW primary_expression IDENTIFIER+)
  | primary_expression INCR+                                     -> ^(POST_INCR primary_expression INCR+)
  | primary_expression DECR+                                     -> ^(POST_DECR primary_expression DECR+)
  | primary_expression
  ;

primary_expression
  : IDENTIFIER
  | constant
  | LPAREN! expression RPAREN!
  ;

constant
  : HEX_LITERAL
  | OCTAL_LITERAL
  | DECIMAL_LITERAL
  | CHARACTER_LITERAL
  | STRING_LITERAL+
  | FLOATING_POINT_LITERAL
  ;

/////

expression
  : assignment_expression (COMMA! assignment_expression)*
  ;

constant_expression
  : conditional_expression
  ;

assignment_expression
  : lvalue assignment_operator assignment_expression
  | conditional_expression
  ;
  
lvalue
  : unary_expression
  ;

assignment_operator
  : ASSIGN
  | STAR_ASSIGN
  | DIV_ASSIGN
  | MOD_ASSIGN
  | PLUS_ASSIGN
  | MINUS_ASSIGN
  | SHIFT_LEFT_ASSIGN
  | SHIFT_RIGHT_ASSIGN
  | AND_ASSIGN
  | XOR_ASSIGN
  | OR_ASSIGN
  ;

conditional_expression
  : logical_or_expression (QUESTION expression COLON conditional_expression)?
  ;

logical_or_expression
  : logical_and_expression (LOGICAL_OR logical_and_expression)*
  ;

logical_and_expression
  : inclusive_or_expression (LOGICAL_AND inclusive_or_expression)*
  ;

inclusive_or_expression
  : exclusive_or_expression (OR exclusive_or_expression)*
  ;

exclusive_or_expression
  : and_expression (XOR and_expression)*
  ;

and_expression
  : equality_expression (AND equality_expression)*
  ;
equality_expression
  : relational_expression ((EQUAL|NOT_EQUAL) relational_expression)*
  ;

relational_expression
  : shift_expression ((LESS_THAN|GREATER_THAN|LESS_OR_EQUAL|GREATER_OR_EQUAL) shift_expression)*
  ;

shift_expression
  : additive_expression ((SHIFT_LEFT|SHIFT_RIGHT) additive_expression)*
  ;

// S t a t e m e n t s

statement
  : labeled_statement
  | block_statement
  | expression_statement
  | selection_statement
  | iteration_statement
  | jump_statement
  | inline_assembly_statement
  ;

inline_assembly_statement
  : inline_assembly SEMI
  ;

labeled_statement
  : IDENTIFIER COLON statement
  | CASE constant_expression COLON statement
  | DEFAULT COLON statement
  ;

block_statement
@init { 
    scopeStack.add(new HashSet<String>());
}
@after {
    scopeStack.pop();
}
  : LCURLY declaration* statement_list? RCURLY
  ;

statement_list
  : statement+
  ;

expression_statement
  : SEMI
  | expression SEMI
  ;

selection_statement
  : IF LPAREN expression RPAREN statement (options {k=1; backtrack=false;}:ELSE statement)?
  | SWITCH LPAREN expression RPAREN statement
  ;

iteration_statement
  : WHILE LPAREN expression RPAREN statement
  | DO statement WHILE LPAREN expression RPAREN SEMI
  | FOR LPAREN expression_statement expression_statement expression? RPAREN statement
  ;

jump_statement
  : GOTO IDENTIFIER SEMI
  | CONTINUE SEMI
  | BREAK SEMI
  | RETURN SEMI
  | RETURN expression SEMI
  ;

////////////////////////////
// L E X E R    R U L E S //
////////////////////////////

// operators and other special chars
AND                : '&'               ;
AND_ASSIGN         : '&='              ;
ASM                : 'asm'|'__asm__'|'__asm';
ASSIGN             : '='               ;
ARROW              : '->'              ;
COLON              : ':'               ;
COMMA              : ','               ;
DECR               : '--'              ;
DIV                : '/'               ;
DIV_ASSIGN         : '/='              ;
DOT                : '.'               ;
ELLIPSIS           : '...'             ;
EQUAL              : '=='              ;
GREATER_OR_EQUAL   : '>='              ;
GREATER_THAN       : '>'               ;
INCR               : '++'              ;
LBRACK             : '['               ;
LCURLY             : '{'               ;
LESS_OR_EQUAL      : '<='              ;
LESS_THAN          : '<'               ;
LOGICAL_AND        : '&&'              ;
LOGICAL_NOT        : '!'               ;
LOGICAL_OR         : '||'              ;
LPAREN             : '('               ;
MINUS              : '-'               ;
MINUS_ASSIGN       : '-='              ;
MOD                : '%'               ;
MOD_ASSIGN         : '%='              ;
NOT                : '~'               ;
NOT_EQUAL          : '!='              ;
OR                 : '|'               ;
OR_ASSIGN          : '|='              ;
PLUS               : '+'               ;
PLUS_ASSIGN        : '+='              ;
QUESTION           : '?'               ;
RBRACK             : ']'               ;
RCURLY             : '}'               ;
RPAREN             : ')'               ;
SEMI               : ';'               ;
SHIFT_LEFT         : '<<'              ;
SHIFT_LEFT_ASSIGN  : '<<='             ;
SHIFT_RIGHT        : '>>'              ;
SHIFT_RIGHT_ASSIGN : '>>='             ;
STAR               : '*'               ;
STAR_ASSIGN        : '*='              ;
XOR                : '^'               ;
XOR_ASSIGN         : '^='              ;

// keywords
AUTO               : 'auto'            ;
BREAK              : 'break'           ;
CASE               : 'case'            ;
CHAR               : 'char'            ;
CONST              : 'const'|'__const' ;
CONTINUE           : 'continue'        ;
DEFAULT            : 'default'         ;
DECLSPEC           : '__declspec'      ; // microsoft specific
DO                 : 'do'              ;
DOUBLE             : 'double'          ;
ELSE               : 'else'            ;
ENUM               : 'enum'            ;
EXTERN             : 'extern'          ;
EXTENSION          : '__extension__' {$channel=HIDDEN;}; // gcc specific
FLOAT              : 'float'           ;
FOR                : 'for'             ;
ATTRIBUTE          : '__attribute__'   ; // gcc specific
GOTO               : 'goto'            ;
IF                 : 'if'              ;
INT                : 'int'             ;
INLINE             : 'inline'|'__inline__'; // c99
LONG               : 'long'            ;
REGISTER           : 'register'        ;
RESTRICT           : ('restrict'|'__restrict'|'__restrict__') {$channel=HIDDEN;};
RETURN             : 'return'          ;
SHORT              : 'short'           ;
SIGNED             : 'signed'          ;
SIZEOF             : 'sizeof'          ;
STATIC             : 'static'          ;
STRUCT             : 'struct'          ;
SWITCH             : 'switch'          ;
TYPEDEF            : 'typedef'         ;
UNION              : 'union'           ;
UNSIGNED           : 'unsigned'        ;
VOID               : 'void'            ;
VOLATILE           : 'volatile'|'__volatile__';
WHILE              : 'while'           ;

// complex tokens
IDENTIFIER
  : LETTER (LETTER|'0'..'9')*
  ;
  
fragment
LETTER
  : '$'
  | 'A'..'Z'
  | 'a'..'z'
  | '_'
  ;

CHARACTER_LITERAL
  : '\'' ( EscapeSequence | ~('\''|'\\') ) '\''
  ;

STRING_LITERAL
  : '"' ( EscapeSequence | ~('\\'|'"') )* '"'
  ;

HEX_LITERAL 
  : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? 
  ;

DECIMAL_LITERAL 
  : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? 
  ;

OCTAL_LITERAL 
  : '0' ('0'..'7')+ IntegerTypeSuffix? 
  ;

fragment
HexDigit 
  : ('0'..'9'|'a'..'f'|'A'..'F') 
  ;

fragment
IntegerTypeSuffix
  : ('u'|'U')  ('l'|'L')
  | ('u'|'U')
  | ('l'|'L')
  ;

FLOATING_POINT_LITERAL
  : ('0'..'9')+ DOT ('0'..'9')* Exponent? FloatTypeSuffix?
  | DOT ('0'..'9')+ Exponent? FloatTypeSuffix?
  | ('0'..'9')+ Exponent FloatTypeSuffix
  | ('0'..'9')+ Exponent
  | ('0'..'9')+ FloatTypeSuffix
  ;

fragment
Exponent 
  : ('e'|'E') ('+'|'-')? ('0'..'9')+ 
  ;

fragment
FloatTypeSuffix 
  : ('f'|'F'|'d'|'D') 
  ;

fragment
EscapeSequence
  : '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
  | OctalEscape
  ;

fragment
OctalEscape
  : '\\' ('0'..'3') ('0'..'7') ('0'..'7')
  | '\\' ('0'..'7') ('0'..'7')
  | '\\' ('0'..'7')
  ;

fragment
UnicodeEscape
  : '\\' 'u' HexDigit HexDigit HexDigit HexDigit
  ;
  
WS: (' '|'\r'|'\t'|'\u000C'|'\n')+ {$channel=HIDDEN;}
  ;

COMMENT
  : '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
  ;

LINE_COMMENT
  : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
  ;

LINE_COMMAND 
  : '#' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN; this.recordHeader($line, $text);}
  ;
