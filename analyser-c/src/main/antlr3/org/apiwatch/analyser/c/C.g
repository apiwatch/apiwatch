/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
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
    TYPE_DECLARATION;
    FUNCTION_DEFINITION;
    DECLARATOR;
    ARRAY_DECL;
    FUNCTION_ARGS;
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
    BIT_COUNT;
}

@header {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
 
import org.apiwatch.analyser.c.Header;
import org.apiwatch.util.antlr.IterableTree;
}

@lexer::header {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
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
private Stack<HashSet<String>> scopeStack = new Stack<HashSet<String>>();
public List<Header> headers;

// special constructor
public CParser(TokenStream input, List<Header> headers) {
    this(input, new RecognizerSharedState());
    this.headers = headers;
}

private boolean isInSystemHeader(int line) {
    if (headers == null || headers.isEmpty()) {
        return false;
    }
    for (int i = 0; i < headers.size(); i++) {
        if (headers.get(i).line > line) {
            return headers.get(i - 1).isSystemHeader;
        }
    }
    return false;
}

private String getSourceFile(int line) {
    if (headers == null || headers.isEmpty()) {
        return "";
    }
    
    Header header = null;
    int realLine = 0;
    String sourceFile = null;
    
    for (int i = 0; i < headers.size(); i++) {
        header = headers.get(i);
        if (header.line > line) {
            header = headers.get(i - 1);
            break;
        }
    }
    
    realLine = header.sourceLine + (line - header.line - 1);
    
    return header.sourceFile + ":" + realLine;
}


private boolean isTypeName(String name) {
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

public List<String> systemPaths;
public List<Header> headers; 
private static final Pattern PREPROC_LINE = Pattern.compile("#\\s*(\\d+)\\s*\"(.+?)\"");

// special constructor
public CLexer(CharStream input, List<String> systemPaths) {
    this(input, new RecognizerSharedState());
    this.systemPaths = systemPaths != null ? systemPaths : new ArrayList<String>();
    this.headers = new ArrayList<Header>();
}

private void recordHeader(int line, String text) {
    text = text.replace("\\\\", "\\"); // for windows paths
    Matcher match = PREPROC_LINE.matcher(text);
    if (match.find()) {
		    int sourceLine = Integer.valueOf(match.group(1));
		    String sourceFile = match.group(2);
		    boolean isSystemHeader = false;
        // ignore case and file path separator.
		    String source = sourceFile.toLowerCase().replace("\\", "/");
        for (String path : systemPaths) {
            if (source.startsWith(path.replace("\\", "/").toLowerCase())) {
                isSystemHeader = true;
                break;
            }
        }
        headers.add(new Header(line, isSystemHeader, sourceLine, sourceFile));
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
    String sourceFile = getSourceFile(line);
}                                                                  // special case, looking for typedef
  : TYPEDEF declaration_specifiers? {isTypeDef = true;} init_declarator_list compiler_directive* SEMI
                          -> {isInSystemHeader(line)}? // remove from AST
                          -> ^(TYPE_DECLARATION[sourceFile] declaration_specifiers? init_declarator_list)
  | d=declaration_specifiers compiler_directive* SEMI
        -> {isInSystemHeader(line)}? // remove from AST
        -> ^(TYPE_DECLARATION[sourceFile] declaration_specifiers)
  | d=declaration_specifiers i=init_declarator_list compiler_directive* SEMI
        -> {isInSystemHeader(line)}? // remove from AST
        -> {$i.isFunction}? ^(FUNCTION_DECLARATION[sourceFile] declaration_specifiers init_declarator_list)
        ->                  ^(VARIABLE_DECLARATION[sourceFile] declaration_specifiers init_declarator_list)
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
  : compiler_directive       ->
  | storage_class_specifier  -> MODIFIER[$storage_class_specifier.text]
  | type_qualifier           -> MODIFIER[$type_qualifier.text]
  | simple_type_specifier    -> TYPE[$simple_type_specifier.text]
  | complex_type_specifier   -> ^(TYPE complex_type_specifier)
  ;

storage_class_specifier
  : EXTERN
  | STATIC
  | AUTO
  | REGISTER
  ;

simple_type_specifier
  : VOID
  | CHAR
  | SHORT
  | INT
  | LONG
  | FLOAT
  | DOUBLE
  | SIGNED
  | UNSIGNED
  | {isTypeName(input.LT(1).getText())}? IDENTIFIER
  ;
  catch [FailedPredicateException fpe] {
    throw new NoViableAltException("", 0, 0, input);
  }

type_qualifier
  : CONST
  | INLINE
  | VOLATILE
  ;

complex_type_specifier
  : struct_or_union_specifier
  | enum_specifier
  ;

struct_or_union_specifier
options {k=3;}
@init { 
    scopeStack.add(new HashSet<String>());
}
@after {
    scopeStack.pop();
}
  : struct_or_union LCURLY struct_declaration_list RCURLY
                     -> ^(STRUCTURE[$struct_or_union.text] struct_declaration_list)
  | struct_or_union IDENTIFIER (LCURLY struct_declaration_list RCURLY)?
                     -> ^(STRUCTURE[$struct_or_union.text] IDENTIFIER struct_declaration_list?)
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
  : specifier_qualifer+     -> ^(DECL_SPECIFIERS specifier_qualifer+)
  ;

specifier_qualifer
  :	type_qualifier          -> MODIFIER[$type_qualifier.text]
  | simple_type_specifier   -> TYPE[$simple_type_specifier.text]
  | complex_type_specifier  -> ^(TYPE complex_type_specifier)
  ;

struct_declarator_list
  : struct_declarator (COMMA! struct_declarator)*
  ;

struct_declarator
  : declarator COLON constant_expression   -> declarator BIT_COUNT[$constant_expression.text]
  | COLON constant_expression              -> BIT_COUNT[$constant_expression.text]
  | declarator
  ;

enum_specifier
options {k=3;}
  : ENUM LCURLY enumerator_list RCURLY                  -> ^(ENUM enumerator_list)
  | ENUM IDENTIFIER LCURLY enumerator_list RCURLY       -> ^(ENUM IDENTIFIER enumerator_list)
  | ENUM IDENTIFIER                                     -> ^(ENUM IDENTIFIER)
  ;

enumerator_list
  : enumerator (COMMA! enumerator)* 
  ;

enumerator
  : IDENTIFIER ASSIGN constant_expression
                        -> ^(ENUMERATOR[$IDENTIFIER.text] ASSIGN[$constant_expression.text])
  | IDENTIFIER          ->   ENUMERATOR[$IDENTIFIER.text]
  ;



declarator returns [boolean isFunction]
  : compiler_directive* pointer? compiler_directive* direct_declarator {$isFunction=$direct_declarator.isFunction;}
                        -> ^(DECLARATOR pointer? direct_declarator)
  | compiler_directive* pointer compiler_directive*                   {$isFunction=false;}
                        -> ^(DECLARATOR pointer)
  ;

direct_declarator returns [boolean isFunction]
  : IDENTIFIER {
	    /* record this identifier as a new typename for the current scope */
	    if (isTypeDef && !(scopeStack == null || scopeStack.isEmpty())) {
	        scopeStack.peek().add($IDENTIFIER.text);
	    }
    } s=declarator_suffix*   {$isFunction=$s.isFunction;}  
  | LPAREN! declarator RPAREN! s=declarator_suffix* {$isFunction=$s.isFunction;}
  ;


declarator_suffix returns [boolean isFunction]
  : array_suffix      {$isFunction=false;}
  | function_suffix   {$isFunction=true;}
  ;

array_suffix
  : LBRACK RBRACK                        -> ^(ARRAY_DECL[$LBRACK.text + $RBRACK.text])
  | LBRACK e=constant_expression RBRACK  -> ^(ARRAY_DECL[$LBRACK.text + $e.text + $RBRACK.text])
  ;

function_suffix
  : LPAREN VOID? RPAREN compiler_directive*               -> ^(FUNCTION_ARGS)
  | LPAREN parameter_type_list RPAREN compiler_directive* -> ^(FUNCTION_ARGS parameter_type_list)
  | LPAREN identifier_list RPAREN compiler_directive*     -> ^(FUNCTION_ARGS identifier_list)
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
  : STAR type_qualifier* pointer? -> ^(POINTER[$text])
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
  | LCURLY initializer_list COMMA? RCURLY
  ;

initializer_list
  : initializer (COMMA initializer)*
  ;

// E x p r e s s i o n s

expression
  : assignment_expression (COMMA assignment_expression)*
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

argument_expression_list
  : assignment_expression (COMMA assignment_expression)*
  ;

additive_expression
  : multiplicative_expression ((PLUS|MINUS) multiplicative_expression)*
  ;

multiplicative_expression
  : cast_expression ((STAR|DIV|MOD) cast_expression)*
  ;

cast_expression
  : LPAREN type_name RPAREN cast_expression
  | unary_expression
  ;

unary_expression
  : postfix_expression
  | INCR unary_expression
  | DECR unary_expression
  | unary_operator cast_expression
  | SIZEOF unary_expression
  | SIZEOF LPAREN type_name RPAREN
  ;

unary_operator
  : AND
  | STAR
  | PLUS
  | MINUS
  | NOT
  | LOGICAL_NOT
  ;

postfix_expression
  : primary_expression 
      ( LBRACK expression RBRACK
      | LPAREN RPAREN
      | LPAREN argument_expression_list RPAREN
      | DOT IDENTIFIER
      | ARROW IDENTIFIER           
      | INCR
      | DECR
      )*
  ;

primary_expression
  : IDENTIFIER
  | constant
  | LPAREN expression RPAREN
  ;

constant
  : HEX_LITERAL
  | OCTAL_LITERAL
  | DECIMAL_LITERAL
  | CHARACTER_LITERAL
  | FLOATING_POINT_LITERAL
  | STRING_LITERAL+
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
  : SEMI!
  | expression SEMI!
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
EXTENSION          : '__extension__' {$channel=HIDDEN;}; // GNU specific
FLOAT              : 'float'           ;
FOR                : 'for'             ;
ATTRIBUTE          : '__attribute__'   ; // GNU specific
GOTO               : 'goto'            ;
IF                 : 'if'              ;
INT                : 'int'             ;
INLINE             : 'inline'|'__inline__'|'__inline'; // c99
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
  | ('l'|'L') ('l'|'L')?
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

PREPROCESSOR_COMMAND 
  : '#' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN; recordHeader($line, $text);}
  ;
