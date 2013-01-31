/*
 * PUBLIC DOMAIN PCCTS-BASED C++ GRAMMAR (cplusplus.g, stat.g, expr.g)
 *
 * Authors: Sumana Srinivasan, NeXT Inc.;            sumana_srinivasan@next.com
 *          Terence Parr, Parr Research Corporation; parrt@parr-research.com
 *          Russell Quong, Purdue University;        quong@ecn.purdue.edu
 *
 * VERSION 1.2
 *
 * SOFTWARE RIGHTS
 *
 * This file is a part of the ANTLR-based C++ grammar and is free
 * software.  We do not reserve any LEGAL rights to its use or
 * distribution, but you may NOT claim ownership or authorship of this
 * grammar or support code.  An individual or company may otherwise do
 * whatever they wish with the grammar distributed herewith including the
 * incorporation of the grammar or the output generated by ANTLR into
 * commerical software.  You may redistribute in source or binary form
 * without payment of royalties to us as long as this header remains
 * in all source distributions.
 *
 * We encourage users to develop parsers/tools using this grammar.
 * In return, we ask that credit is given to us for developing this
 * grammar.  By "credit", we mean that if you incorporate our grammar or
 * the generated code into one of your programs (commercial product,
 * research project, or otherwise) that you acknowledge this fact in the
 * documentation, research report, etc....  In addition, you should say nice
 * things about us at every opportunity.
 *
 * As long as these guidelines are kept, we expect to continue enhancing
 * this grammar.  Feel free to send us enhancements, fixes, bug reports,
 * suggestions, or general words of encouragement at parrt@parr-research.com.
 * 
 * NeXT Computer Inc.
 * 900 Chesapeake Dr.
 * Redwood City, CA 94555
 * 12/02/1994
 * 
 * Restructured for public consumption by Terence Parr late February, 1995.
 *
 * DISCLAIMER: we make no guarantees that this grammar works, makes sense,
 *             or can be used to do anything useful.
 */
/* 2001-2002
 * Version 1.0
 * This C++ grammar file has been converted from PCCTS to run under 
 *  ANTLR to generate lexer and parser in C++ code by
 *  Jianguo Zuo and David Wigg at
 *  The Centre for Systems and Software Engineering
 *  London South Bank University
 *  London, UK.
 *
 */
/* 2003
 * Version 2.0 was published by David Wigg in September 2003
 */
/* 2004
 * Version 3.0 July 2004
 * This is version 3.0 of the C++ grammar definition for ANTLR to 
 *  generate lexer and parser in C++ code updated by
 *  David Wigg at
 *  The Centre for Systems and Software Engineering
 *  London South Bank University
 *  London, UK.
 */
/* 2005
 * Version 3.1 November 2005
 * Updated by David Wigg at London South Bank University
 *
 */
/* 2007
 * Version 3.2 November 2007
 * Updated by David Wigg at London South Bank University
 *
 * wiggjd@bcs.ac.uk
 * blackse@lsbu.ac.uk
 *
 * See MyReadMe.txt for further information
 *
 * This file is best viewed in courier font with tabs set to 4 spaces
 */
 


/* -- 2010 --
 * Version 4.0.1 August 2010
 * Modified and reworked to compile with ANTLR 3.2
 *  by Ramin Zaghi
 * 
 * Please note that this is the first public release
 *  for ANTLR 3.2; This does not comiple with any older
 *  versions of ANTLR. This may also have some missing 
 *  features compared to the 2007 update by David Wigg.
 *  I am publishing this work only to make this first
 *  ANTLR 3.2 update  available  to  the  community
 *  however if you are interested in a more complete 
 *  work please take a look at David's 2007 update.
 * 
 *  I emphasize that this new update needs more work
 *  and can be restructured to make it more developer
 *  friendly. The project file is a MSVS2008 project
 *  file and it only includes a "Debug" configuration.
 *  
 *  You may send your comments to < antlr3_cpp_parser@taggedprogramming.com >
 *  
 */





#pragma once
#ifndef _HEADER_FILE_
#define _HEADER_FILE_

#include    <antlr3.h>

#define FALSE 0
#define TRUE 1

typedef int boolean;

extern boolean dummyBool;
extern boolean dummyVar;
extern boolean wasInTemplate;
extern 	boolean antlrTracing;

extern int deferredLineCount;	// used to accumulate line numbers in comments etc.

#define CPPParser_MaxQualifiedItemSize 500


/******************************************************************/
	// File generated from CPP_parser.g
	// Version 3.2 November 2007
	// This file is best viewed in courier font with tabs set to 4 spaces
	//
	// The statements in this block appear in both CPPLexer.hpp and CPPParser.hpp
	//#include "antlr/CharScanner.hpp"
	#include "Helper/CPPDictionary.hpp"

	// Following externs declared here to be available for users

	// Declared and set in CPPParser.cpp
	extern int lineNo; // current line
	extern bool in_user_file;	// true = in principal file, false = in an include file

	// Declared and set in CPPLexer.cpp
	extern boolean in_user_file_deferred;

	extern int deferredLineCount;	// used to accumulate line numbers in comments etc.

	extern char principal_file[128];	// Name of user file
	extern int principal_line;		// Principal file's line number
	extern int principal_last_set;	// Where principal file's line number was last set
									//   in preprocessed *.i file

	extern char current_included_file[128];	// Name of current include file
	extern int include_line;		// Included file's line number
	extern int include_last_set;	// Where included file's line number was last set
									//   in preprocessed *.i file

	// The statements in this block appear in both CPPLexer.hpp and CPPParser.hpp

/******************************************************************/

typedef unsigned long TypeSpecifier;   // note: must be at least 16 bits
#define tsInvalid   0x0
#define tsVOID      0x1
#define tsCHAR      0x2
#define tsSHORT     0x4
#define tsINT       0x8
#define tsLONG      0x10
#define tsFLOAT     0x20
#define tsDOUBLE    0x40
#define tsSIGNED    0x80
#define tsUNSIGNED  0x100
#define tsTYPEID    0x200
#define tsSTRUCT    0x400
#define tsENUM      0x800
#define tsUNION     0x1000
#define tsCLASS     0x2000
#define tsWCHAR_T   0x4000
#define tsBOOL      0x8000

typedef unsigned int TypeQualifier;
#define tqInvalid 0
#define tqCONST 1
#define tqVOLATILE 2


typedef unsigned int StorageClass ;
#define scInvalid 0 
#define scAUTO 1
#define scREGISTER 2
#define scSTATIC 3 
#define scEXTERN 4
#define scMUTABLE 5

typedef unsigned int FunctionSpecifier;
#define fsInvalid 0
#define fsVIRTUAL 1 
#define fsINLINE 2
#define fsEXPLICIT 3  
#define fsFRIEND 4

typedef int QualifiedItem;
#define qiInvalid     0x0
#define qiType        0x1	// includes enum, class, typedefs, namespace
#define qiDtor        0x2
#define qiCtor        0x4
#define qiOperator    0x8
#define qiPtrMember   0x10
#define qiVar         0x20
#define qiFun         0x40


extern int templateParameterScope;
extern int externalScope;
extern int anyType;
extern int anyNonType;

extern boolean _td;			// For typedef
extern boolean _fd;			// For friend
//extern StorageClass _sc;	// For storage class
//extern TypeQualifier _tq;	// For type qualifier
//extern TypeSpecifier _ts;	// For type specifier
//extern FunctionSpecifier _fs;	// For declaration specifier

extern int functionDefinition;
					// 0 = Function definition not being parsed
					// 1 = Parsing function name
					// 2 = Parsing function parameter list
					// 3 = Parsing function block

extern char qualifierPrefix[1024+1];//CPPParser_MaxQualifiedItemSize+1];
extern char *enclosingClass;
extern int assign_stmt_RHS_found;
extern boolean in_parameter_list;
extern boolean K_and_R;	// used to distinguish old K & R parameter definitions
extern boolean in_return;
extern boolean is_address;
extern boolean is_pointer;

// Limit lookahead for qualifiedItemIs()
enum _DUMMY_ENUM_001
{ 
	MaxTemplateTokenScan = 200 
};






void CPPParser__init();

void paraphrase_push(const char* _pp);
void paraphrase_pop();

void deferredNewline();
void process_line_directive(const char *includedFile, const char *includedLineNo);
void tab();
void antlrTrace(boolean traceFlag);

void enterExternalScope();
void exitExternalScope();
void end_of_stmt();
boolean qualifiedItemIsOneOf(QualifiedItem qiFlags, int lookahead_offset);

void enterNewLocalScope();
void exitLocalScope();
void exceptionBeginHandler();
void exceptionEndHandler();

void endTemplateDeclaration();
void beginTemplateDefinition();

void beginParameterDeclaration();
void declaratorArray();
void beginTemplateParameterList();
void endTemplateParameterList();
void templateTypeParameter(const char *);
void endConstructorDefinition();
void endDestructorDefinition();

void unctionParameterList();
void functionEndParameterList(const int def);
int isTypeName(const char *s);

void functionParameterList();

void declaratorID(pANTLR3_UINT8 _s, QualifiedItem qi);	// This stores new symbol with its type.
void declaratorArray();
void declaratorParameterList(const int def);
void declaratorEndParameterList(const int def);

void classForwardDeclaration(const char *, TypeSpecifier, FunctionSpecifier);
void beginClassDefinition(const char *, TypeSpecifier);
void endClassDefinition();
void beginEnumDefinition(const char *);
void endEnumDefinition();
void enumElement(const char *);

void beginTemplateDeclaration();
void endFunctionDefinition();
void declarationSpecifier(boolean,boolean,StorageClass,TypeQualifier,TypeSpecifier,FunctionSpecifier);










////////////////////////////
////////////////////////////


int /*CPPParser__*/qualifiedItemIsOneOf(QualifiedItem qiFlags,   // Ored combination of flags
					int lookahead_offset);

/*CPPParser__*/QualifiedItem 
/*CPPParser__*/qualifiedItemIs(int lookahead_offset);
int /*CPPParser__*/skipTemplateQualifiers(int& kInOut);
int /*CPPParser__*/skipNestedParens(int& kInOut);

int /*CPPParser__*/scopedItem(int k);

int /*CPPParser__*/finalQualifier(int k);

int /*CPPParser__*/isTypeName(const char *s);

int /*CPPParser__*/firstIsTypeName(const char *s);
int /*CPPParser__*/isClassName(const char *s);

void /*CPPParser__*/beginDeclaration();
void /*CPPParser__*/endDeclaration();
void /*CPPParser__*/beginFunctionDefinition();
void /*CPPParser__*/endFunctionDefinition();

void /*CPPParser__*/beginConstructorDefinition();
void /*CPPParser__*/endConstructorDefinition();


void /*CPPParser__*/beginConstructorDeclaration(const char *ctor);

void /*CPPParser__*/endConstructorDeclaration();

void /*CPPParser__*/beginDestructorDefinition();

void /*CPPParser__*/endDestructorDefinition();




void /*CPPParser__*/beginDestructorDeclaration(const char *dtor);

void /*CPPParser__*/endDestructorDeclaration();

void /*CPPParser__*/beginParameterDeclaration();

void /*CPPParser__*/beginFieldDeclaration();

void /*CPPParser__*/declarationSpecifier(bool td, bool fd, StorageClass sc, TypeQualifier tq,
			 TypeSpecifier ts, FunctionSpecifier fs);

void /*CPPParser__*/declaratorID(const char *id,QualifiedItem qi);



void /*CPPParser__*/declaratorArray();
void /*CPPParser__*/declaratorParameterList(int def);
void /*CPPParser__*/declaratorEndParameterList(int def);


void /*CPPParser__*/functionParameterList();


void /*CPPParser__*/functionEndParameterList(int def);




void /*CPPParser__*/enterNewLocalScope();
void /*CPPParser__*/exitLocalScope();

void /*CPPParser__*/enterExternalScope();

void /*CPPParser__*/exitExternalScope();

void /*CPPParser__*/classForwardDeclaration(const char *tag, TypeSpecifier ts, FunctionSpecifier fs);


void /*CPPParser__*/beginClassDefinition(const char *tag, TypeSpecifier ts);


void /*CPPParser__*/endClassDefinition();




void /*CPPParser__*/enumElement(const char *e);

void /*CPPParser__*/beginEnumDefinition(const char *e);
void /*CPPParser__*/endEnumDefinition();

void /*CPPParser__*/templateTypeParameter(const char *t);


void /*CPPParser__*/beginTemplateDeclaration();

void /*CPPParser__*/endTemplateDeclaration();

void /*CPPParser__*/beginTemplateDefinition();
void /*CPPParser__*/endTemplateDefinition();

void /*CPPParser__*/beginTemplateParameterList();





void /*CPPParser__*/endTemplateParameterList();
void /*CPPParser__*/exceptionBeginHandler();

void /*CPPParser__*/exceptionEndHandler();


void /*CPPParser__*/end_of_stmt();

void /*CPPParser__*/panic(const char *err);


void /*CPPParser__*/myCode_pre_processing(int argc,char *argv[]);
void /*CPPParser__*/myCode_post_processing();
void /*CPPParser__*/myCode_end_of_stmt();

void /*CPPParser__*/myCode_function_direct_declarator(const char *id);



#endif
