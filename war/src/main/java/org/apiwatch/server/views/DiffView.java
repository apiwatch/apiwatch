/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DiffView extends View {

    private String component;
    private String versionA;
    private String versionB;

    public DiffView(HttpServletRequest req, HttpServletResponse resp, String component,
            String versionA, String versionB)
    {
        super(req, resp);
        this.component = component;
        this.versionA = versionA;
        this.versionB = versionB;
    }

}
