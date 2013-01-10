/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util.errors;

public class SerializationError extends Exception {

    private static final long serialVersionUID = -8372835960489319517L;

    public SerializationError() {
    }

    public SerializationError(String message) {
        super(message);
    }

    public SerializationError(Throwable cause) {
        super(cause);
    }

    public SerializationError(String message, Throwable cause) {
        super(message, cause);
    }

}
