package org.apiwatch.analyser.cpp;

public class ParserConstants {
    
    static final int MaxTemplateTokenScan = 200;
    
    public static class StorageClass { 
        static final int  Invalid = 0x0;
        static final int     Auto = 0x1;
        static final int Register = 0x2;
        static final int   Static = 0x4;
        static final int   Extern = 0x8;
        static final int  Mutable = 0x10;
    }
    
    public static class TypeQualifier {
        static final int  Invalid = 0x0;
        static final int    Const = 0x1;
        static final int Volatile = 0x2;
    }
    
    public static class TypeSpecifier {
        static final int  Invalid = 0x0;
        static final int     Void = 0x1;
        static final int     Char = 0x2;
        static final int    Short = 0x4;
        static final int      Int = 0x8;
        static final int     Long = 0x10;
        static final int    Float = 0x20;
        static final int   Double = 0x40;
        static final int   Signed = 0x80;
        static final int Unsigned = 0x100;
        static final int   TypeID = 0x200;
        static final int   Struct = 0x400;
        static final int     Enum = 0x800;
        static final int    Union = 0x1000;
        static final int    Class = 0x2000;
        static final int  WChar_T = 0x4000;
        static final int     Bool = 0x8000;
    }
    
    public static class FunctionSpecifier {
        static final int  Invalid = 0x0;
        static final int  Virtual = 0x1;
        static final int   Inline = 0x2;
        static final int Explicit = 0x4;
        static final int   Friend = 0x8;
    }
    
    public static enum FunctionParserState {
        None, Name, Params, Block;
    }
    
    public static class QualifiedItem {
        static final int   Invalid = 0x0;
        static final int      Type = 0x1; // includes enum, class, typedefs, namespace
        static final int      Dtor = 0x2;
        static final int      Ctor = 0x4;
        static final int  Operator = 0x8;
        static final int PtrMember = 0x10;
        static final int       Var = 0x20;
        static final int       Fun = 0x40;
    }
}
