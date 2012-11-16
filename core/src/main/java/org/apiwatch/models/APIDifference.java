/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.HashSet;
import java.util.Set;

public class APIDifference {

    public ChangeType changeType;
    public APIElement elementA;
    public APIElement elementB;
    public String attribute;
    public Object valueA;
    public Object valueB;
    public transient APIDifference parent; /* not serialized */
    public transient Set<APIDifference> subDiffs; /* not serialized */

    public APIDifference(ChangeType changeType, APIElement elementA, APIElement elementB) {
        this(changeType, elementA, elementB, null, null, null, null, null);
    }

    public APIDifference(ChangeType changeType, APIElement elementA, APIElement elementB,
            String attribute, Object valueA, Object valueB)
    {
        this(changeType, elementA, elementB, attribute, valueA, valueB, null, null);
    }

    public APIDifference(ChangeType changeType, APIElement elementA, APIElement elementB,
            String attribute, Object valueA, Object valueB, APIDifference parent,
            Set<APIDifference> subDiffs)
    {
        super();
        this.changeType = changeType;
        this.elementA = elementA;
        this.elementB = elementB;
        this.attribute = attribute;
        this.valueA = valueA;
        this.valueB = valueB;
        this.parent = parent;
        this.subDiffs = subDiffs != null ? subDiffs : new HashSet<APIDifference>();
    }

    public APIElement element() {
        return elementB != null ? elementB : elementA;
    }

    public String name() {
        APIElement element = element();
        return element != null ? element.name() : null;
    }

    public String path() {
        APIElement element = element();
        return element != null ? element.path() : APIScope.ROOT_PATH;
    }

    public Visibility visibility() {
        APIElement element = element();
        return element != null ? element.visibility : null;
    }

    @Override
    public String toString() {
        return changeType.toString() + '(' + path() + ')';
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public APIElement getElementA() {
        return elementA;
    }

    public APIElement getElementB() {
        return elementB;
    }

    public String getAttribute() {
        return attribute;
    }

    public Object getValueA() {
        return valueA;
    }

    public Object getValueB() {
        return valueB;
    }

}
