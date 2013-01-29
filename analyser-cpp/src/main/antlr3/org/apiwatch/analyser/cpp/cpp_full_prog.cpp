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





// You may adopt your own practices by all means, but in general it is best
 // to create a single include for your project, that will include the ANTLR3 C
 // runtime header files, the generated header files (all of which are safe to include
 // multiple times) and your own project related header files. Use <> to include and
 // -I on the compile line (which vs2005 now handles, where vs2003 did not).
 //


 #include    "./CPP_grammar_Lexer.h"
 #include    "./CPP_grammar_Parser.h"


#include <stack>
#include <string>

using namespace std;

//extern std::stack<std::string> paraphrase;
std::stack<std::string> paraphrase; // = new std::stack<std::string>();

void paraphrase_push(const char* _pp)
{
	paraphrase.push( _pp );
}

void paraphrase_pop()
{
	paraphrase.pop();
}

     // Now we declare the ANTLR related local variables we need.
     // Note that unless you are convinced you will never need thread safe
     // versions for your project, then you should always create such things
     // as instance variables for each invocation.
     // -------------------
 
     // Name of the input file. Note that we always use the abstract type pANTLR3_UINT8
     // for ASCII/8 bit strings - the runtime library guarantees that this will be
     // good on all platforms. This is a general rule - always use the ANTLR3 supplied
     // typedefs for pointers/types/etc.
     //
     pANTLR3_UINT8      fName;
 
     // The ANTLR3 character input stream, which abstracts the input source such that
     // it is easy to privide inpput from different sources such as files, or 
     // memory strings.
     //
     // For an ASCII/latin-1 memory string use:
     //     input = antlr3NewAsciiStringInPlaceStream (stringtouse, (ANTLR3_UINT32) length, NULL);
     //
     // For a UCS2 (16 bit) memory string use:
     //     input = antlr3NewUCS2StringInPlaceStream (stringtouse, (ANTLR3_UINT32) length, NULL);
     //
     // For input from a file, see code below
     //
     // Note that this is essentially a pointer to a structure containing pointers to functions.
     // You can create your own input stream type (copy one of the existing ones) and override any
     // individual function by installing your own pointer after you have created the standard 
     // version.
     //
     pANTLR3_INPUT_STREAM       input;
 
     // The lexer is of course generated by ANTLR, and so the lexer type is not upper case.
     // The lexer is supplied with a pANTLR3_INPUT_STREAM from whence it consumes its
     // input and generates a token stream as output. This is the ctx (CTX macro) pointer
        // for your lexer.
     //
     pCPP_grammar_Lexer             lxr;
 
     // The token stream is produced by the ANTLR3 generated lexer. Again it is a structure based
     // API/Object, which you can customise and override methods of as you wish. a Token stream is
     // supplied to the generated parser, and you can write your own token stream and pass this in
     // if you wish.
     //
     pANTLR3_COMMON_TOKEN_STREAM        tstream;
 
     // The Lang parser is also generated by ANTLR and accepts a token stream as explained
     // above. The token stream can be any source in fact, so long as it implements the 
     // ANTLR3_TOKEN_SOURCE interface. In this case the parser does not return anything
     // but it can of course specify any kind of return type from the rule you invoke
     // when calling it. This is the ctx (CTX macro) pointer for your parser.
     //
     pCPP_grammar_Parser                psr;
 
     // The parser produces an AST, which is returned as a member of the return type of
     // the starting rule (any rule can start first of course). This is a generated type
     // based upon the rule we start with.
     //
     //LangParser_decl_return     langAST;
 
 
     // The tree nodes are managed by a tree adaptor, which doles
     // out the nodes upon request. You can make your own tree types and adaptors
     // and override the built in versions. See runtime source for details and
     // eventually the wiki entry for the C target.
     //
     //pANTLR3_COMMON_TREE_NODE_STREAM    nodes;
 
     // Finally, when the parser runs, it will produce an AST that can be traversed by the 
     // the tree parser: c.f. LangDumpDecl.g3t This is the ctx (CTX macro) pointer for your
        // tree parser.
     //
     //pLangDumpDecl          treePsr;
 
// Main entry point for this example
 //
 int ANTLR3_CDECL
 main   (int argc, char *argv[])
 {

     // Create the input stream based upon the argument supplied to us on the command line
     // for this example, the input will always default to ./input if there is no explicit
     // argument.
     //
    if (argc < 2 || argv[1] == NULL)
    {
        fName   =(pANTLR3_UINT8)"./input"; // Note in VS2005 debug, working directory must be configured
    }
    else
    {
        fName   = (pANTLR3_UINT8)argv[1];
    }
 
     // Create the input stream using the supplied file name
     // (Use antlr3AsciiFileStreamNew for UCS2/16bit input).
     //
     input  = antlr3AsciiFileStreamNew(fName);
 
     // The input will be created successfully, providing that there is enough
     // memory and the file exists etc
     //
     if ( input == NULL )
     {
            ANTLR3_FPRINTF(stderr, "Unable to open file %s due to malloc() failure1\n", (char *)fName);
     }
 
     // Our input stream is now open and all set to go, so we can create a new instance of our
     // lexer and set the lexer input to our input stream:
     //  (file | memory | ?) --> inputstream -> lexer --> tokenstream --> parser ( --> treeparser )?
     //
     lxr        = CPP_grammar_LexerNew(input);      // CLexerNew is generated by ANTLR
 
     // Need to check for errors
     //
     if ( lxr == NULL )
     {
            ANTLR3_FPRINTF(stderr, "Unable to create the lexer due to malloc() failure1\n");
            exit(ANTLR3_ERR_NOMEM);
     }
 
     // Our lexer is in place, so we can create the token stream from it
     // NB: Nothing happens yet other than the file has been read. We are just 
     // connecting all these things together and they will be invoked when we
     // call the parser rule. ANTLR3_SIZE_HINT can be left at the default usually
     // unless you have a very large token stream/input. Each generated lexer
     // provides a token source interface, which is the second argument to the
     // token stream creator.
     // Note tha even if you implement your own token structure, it will always
     // contain a standard common token within it and this is the pointer that
     // you pass around to everything else. A common token as a pointer within
     // it that should point to your own outer token structure.
     //
     tstream = antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->rec->state->tokSource);
 
     if (tstream == NULL)
     {
        ANTLR3_FPRINTF(stderr, "Out of memory trying to allocate token stream\n");
        exit(ANTLR3_ERR_NOMEM);
     }
 
     // Finally, now that we have our lexer constructed, we can create the parser
     //
     psr        = CPP_grammar_ParserNew(tstream);  // CParserNew is generated by ANTLR3
 
     if (psr == NULL)
     {
        ANTLR3_FPRINTF(stderr, "Out of memory trying to allocate parser\n");
        exit(ANTLR3_ERR_NOMEM);
     }
 
     // We are all ready to go. Though that looked complicated at first glance,
     // I am sure, you will see that in fact most of the code above is dealing
     // with errors and there isn;t really that much to do (isn;t this always the
     // case in C? ;-).
     //
     // So, we now invoke the parser. All elements of ANTLR3 generated C components
     // as well as the ANTLR C runtime library itself are pseudo objects. This means
     // that they are represented as pointers to structures, which contain any
     // instance data they need, and a set of pointers to other interfaces or
     // 'methods'. Note that in general, these few pointers we have created here are
     // the only things you will ever explicitly free() as everything else is created
     // via factories, that allocate memory efficiently and free() everything they use
     // automatically when you close the parser/lexer/etc.
     //
     // Note that this means only that the methods are always called via the object
     // pointer and the first argument to any method, is a pointer to the structure itself.
     // It also has the side advantage, if you are using an IDE such as VS2005 that can do it
     // that when you type ->, you will see a list of all the methods the object supports.
     //
     //langAST = 
	 psr->translation_unit(psr);
 
     // If the parser ran correctly, we will have a tree to parse. In general I recommend
     // keeping your own flags as part of the error trapping, but here is how you can
     // work out if there were errors if you are using the generic error messages
     //
    if (psr->pParser->rec->state->errorCount > 0)
    {
        ANTLR3_FPRINTF(stderr, "The parser returned %d errors, tree walking aborted.\n", psr->pParser->rec->state->errorCount);
 
    }
    else
    {/*
        nodes   = antlr3CommonTreeNodeStreamNewTree(langAST.tree, ANTLR3_SIZE_HINT); // sIZE HINT WILL SOON BE DEPRECATED!!
 
        // Tree parsers are given a common tree node stream (or your override)
        //
        treePsr = LangDumpDeclNew(nodes);
 
        treePsr->decl(treePsr);
        nodes   ->free  (nodes);        nodes   = NULL;
        treePsr ->free  (treePsr);      treePsr = NULL;*/
    }
 
    // We did not return anything from this parser rule, so we can finish. It only remains
    // to close down our open objects, in the reverse order we created them
    //
    psr     ->free  (psr);      psr     = NULL;
    tstream ->free  (tstream);  tstream = NULL;
    lxr     ->free  (lxr);      lxr     = NULL;
    input   ->close (input);    input   = NULL;
 
     return 0;
 }


