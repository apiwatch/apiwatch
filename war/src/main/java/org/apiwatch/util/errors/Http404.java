/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util.errors;

@SuppressWarnings("serial")
public class Http404 extends Exception {

    public Http404() {
        super();
    }

    public Http404(String message, Throwable cause) {
        super(message, cause);
    }

    public Http404(String message) {
        super(message);
    }

    public Http404(Throwable cause) {
        super(cause);
    }

}
