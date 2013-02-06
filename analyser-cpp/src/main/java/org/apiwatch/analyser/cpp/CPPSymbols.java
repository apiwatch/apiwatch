package org.apiwatch.analyser.cpp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CPPSymbols {
    
    private List<Set<CPPSymbol>> scopes;
    private int currentIndex;
    
    public CPPSymbols() {
        scopes = new ArrayList<Set<CPPSymbol>>();
        scopes.add(new HashSet<CPPSymbol>());
        currentIndex = 0;
    }
    
    public boolean isDefined(String name, int... objectTypes) {
        CPPSymbol[] symbols = new CPPSymbol[objectTypes.length];
        for (int i = 0; i < objectTypes.length; i++) {
            symbols[i] = new CPPSymbol(name, objectTypes[i]);
        }
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Set<CPPSymbol> scope = scopes.get(i);
            for (CPPSymbol symbol : symbols) {
                if (scope.contains(symbol)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public CPPSymbol lookup(String name, int... objectTypes) {
        CPPSymbol[] symbols = new CPPSymbol[objectTypes.length];
        for (int i = 0; i < objectTypes.length; i++) {
            symbols[i] = new CPPSymbol(name, objectTypes[i]);
        }
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Set<CPPSymbol> scope = scopes.get(i);
            for (CPPSymbol symbol : symbols) {
                if (scope.contains(symbol)) {
                    for (CPPSymbol s : scope) {
                        if (s.equals(symbol)) {
                            return s;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void define(String name, int objectType) {
        defineInScope(name, objectType, currentScope());
    }
    
    public void defineInScope(String name, int objectType, Set<CPPSymbol> scope) {
        scope.add(new CPPSymbol(name, objectType));
    }
    
    public void enterScope() {
        scopes.add(new HashSet<CPPSymbols.CPPSymbol>());
        currentIndex++;
    }
    
    public void exitScope() {
        scopes.remove(currentIndex);
        currentIndex--;
    }
    
    public Set<CPPSymbol> currentScope() {
        return scopes.get(currentIndex);
    }
    
    public static class CPPSymbol {
        
        public static final int otInvalid = 0;
        public static final int otFunction = 1;
        public static final int otVariable = 2;
        public static final int otTypedef = 3;
        public static final int otStruct = 4;
        public static final int otUnion = 5;
        public static final int otEnum = 6;
        public static final int otClass = 7;
        public static final int otTypename = 8;
        public static final int otNonTypename = 9;
        
        public String name;
        public int objectType;
        
        public CPPSymbol(String name, int objectType) {
            this.name = name;
            this.objectType = objectType;
        }
        
        public CPPSymbol(String name) {
            this(name, otInvalid);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CPPSymbol) {
                CPPSymbol that = (CPPSymbol) obj;
                return objectType == that.objectType && name.equals(that.name);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            int hash = name.hashCode() * 10;
            return hash + (hash > 0 ? objectType : -objectType);
        }
    }
}
