/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser;

public class Option {

    public String name;
    public String meta;
    public String description;
    public String nargs;

    public Option(String name, String meta, String description, String nargs) {
        this.name = name;
        this.meta = meta;
        this.description = description;
        this.nargs = nargs;
    }

    public Option(String name, String description) {
        this(name, null, description, "1");
    }
}
