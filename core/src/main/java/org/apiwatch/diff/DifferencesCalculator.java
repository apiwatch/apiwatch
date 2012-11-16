/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIElement;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Function;
import org.apiwatch.models.Symbol;
import org.apiwatch.models.SymbolContainer;
import org.apiwatch.models.Variable;

public class DifferencesCalculator {

    /**
     * <p>
     * Recursively calculate the differences between 2 {@link APIScope}s.
     * </p>
     * 
     * <p>
     * Here's how the algorithm looks like:
     * </p>
     * <ol>
     * <li>flatten both elements into 2 maps (hashed by their respective paths)</li>
     * <li>calculate differences between the 2 maps -> ADDED, REMOVED, COMMON_A,
     * COMMON_B</li>
     * <li>compute differences between COMMON_A & COMMON_B</li>
     * <li>store these differences plus ADDED & REMOVED into
     * {@link APIDifference} objects</li>
     * <li>return the list of differences</li>
     * </ol>
     */
    public static List<APIDifference> getDiffs(APIScope a, APIScope b) {

        // 1. flatten elements
        Map<String, APIElement> elementsA = flatten(a, new HashMap<String, APIElement>());
        Map<String, APIElement> elementsB = flatten(b, new HashMap<String, APIElement>());

        // 2. calculate basic differences (based on elements' paths)
        Map<String, APIElement> commonA = new HashMap<String, APIElement>();
        Map<String, APIElement> commonB = new HashMap<String, APIElement>();

        List<APIDifference> diffs = new ArrayList<APIDifference>();

        for (Map.Entry<String, APIElement> e : elementsA.entrySet()) {
            if (elementsB.containsKey(e.getKey())) {
                commonA.put(e.getKey(), e.getValue());
            } else if (!(e.getValue().parent instanceof Function)) {
                // ignore removed arguments, function removal is enough
                diffs.add(new APIDifference(ChangeType.REMOVED, e.getValue(), null));
            }
        }

        for (Map.Entry<String, APIElement> e : elementsB.entrySet()) {
            if (elementsA.containsKey(e.getKey())) {
                commonB.put(e.getKey(), e.getValue());
            } else if (!(e.getValue().parent instanceof Function)) {
                // ignore added arguments, function add is enough
                diffs.add(new APIDifference(ChangeType.ADDED, null, e.getValue()));
            }
        }

        // 3. compute differences between commonA & commonB (eliminate strictly identical elements)
        for (Map.Entry<String, APIElement> e : commonA.entrySet()) {
            APIElement eltB = commonB.get(e.getKey());
            diffs.addAll(e.getValue().getDiffs(eltB));
        }

        return diffs;
    }

    /**
     * Flatten the given APIElement into a hashmap.
     * 
     * This means recursively adding all contained APIElements to the hashmap
     * too.
     */
    static HashMap<String, APIElement> flatten(APIElement e, HashMap<String, APIElement> map) {

        map.put(e.path(), e);

        if (e instanceof APIScope) {
            for (APIScope scope : ((APIScope) e).subScopes) {
                flatten(scope, map);
            }
        }
        if (e instanceof SymbolContainer) {
            for (Symbol symbol : ((SymbolContainer) e).symbols()) {
                flatten(symbol, map);
            }
        }
        if (e instanceof Function) {
            for (Variable arg : ((Function) e).arguments) {
                map.put(arg.path(), arg);
            }
        }

        return map;
    }

}
