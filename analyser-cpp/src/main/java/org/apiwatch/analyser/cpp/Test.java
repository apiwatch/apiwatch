/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.cpp;

public class Test {

    StringBuilder qualifierPrefix;
    
    public Test(String string) {
        qualifierPrefix = new StringBuilder(string);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {

        
        Test t = new Test("Cuir::SUper::Pede::");
        
        System.out.println("BEFORE=" + t.qualifierPrefix);
        
        t.trimOneLevel();
        
        System.out.println("BEFORE=" + t.qualifierPrefix);
        
        
        debug("Enchante %s %s\n", "madame", "la marquise");
        
        
    }
    
    public void trimOneLevel() {
        int lastIndex = qualifierPrefix .length() - 1;
        while (lastIndex > 0 && qualifierPrefix.charAt(lastIndex) == ':') {
            // trim all ':' characters at the end
            lastIndex--;
        }
        while (lastIndex > 0 && qualifierPrefix.charAt(lastIndex) != ':') {
            // decrement until begining or ':' character
            lastIndex--;
        }
        if (lastIndex > 0) {
            // correction when not at beginning of string
            lastIndex += 1;
        }
        qualifierPrefix.delete(lastIndex, qualifierPrefix.length());
    }
    
    
    public static void debug(String msg, Object... params) {
        System.err.printf(msg, params, "cuir");
    }
    
}
