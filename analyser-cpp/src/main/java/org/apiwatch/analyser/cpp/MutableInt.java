/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.cpp;

public class MutableInt {

    public int val;

    public MutableInt(int val) {
        this.val = val;
    }

    @Override
    public int hashCode() {
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            Integer i = (Integer) obj;
            return val == i.intValue();
        } else if (obj instanceof MutableInt) {
            MutableInt i = (MutableInt) obj;
            return val == i.val;
        } else {
            return false;
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new MutableInt(val);
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }

}