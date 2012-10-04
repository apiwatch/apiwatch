/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util.errors;

public class UnknownSerializationFormat extends SerializationError {

    private static final long serialVersionUID = 1L;

    public UnknownSerializationFormat() {
        super();
    }

    public UnknownSerializationFormat(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownSerializationFormat(String message) {
        super(message);
    }

    public UnknownSerializationFormat(Throwable cause) {
        super(cause);
    }

}
