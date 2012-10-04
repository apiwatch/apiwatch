/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util.errors;

public class ParseError extends Exception {

    private static final long serialVersionUID = 1L;

    public ParseError() {
        super();
    }

    public ParseError(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseError(String message) {
        super(message);
    }

    public ParseError(Throwable cause) {
        super(cause);
    }

}
