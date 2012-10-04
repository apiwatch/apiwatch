/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import java.util.HashMap;
import java.util.Map.Entry;

public class DiffMaps<K, V> {
    
    public HashMap<K, V> commonA;
    public HashMap<K, V> commonB;
    public HashMap<K, V> added;
    public HashMap<K, V> removed;
    
    public DiffMaps(HashMap<K, V> a, HashMap<K, V> b) {
        commonA = new HashMap<K, V>();
        commonB = new HashMap<K, V>();
        added = new HashMap<K, V>();
        removed = new HashMap<K, V>();
        
        for (Entry<K, V> e : a.entrySet()) {
            if (b.containsKey(e.getKey())) {
                commonA.put(e.getKey(), e.getValue());
            } else {
                removed.put(e.getKey(), e.getValue());
            }
        }
        
        for (Entry<K, V> e : b.entrySet()) {
            if (a.containsKey(e.getKey())) {
                commonB.put(e.getKey(), e.getValue());
            } else {
                added.put(e.getKey(), e.getValue());
            }
        }
    }
    
}
