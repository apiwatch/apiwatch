/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apiwatch.util.errors.Http404;

public class RedirectView extends View {

    private String redirectUrl;

    public RedirectView(HttpServletRequest req, HttpServletResponse resp, String redirectUrl) {
        super(req, resp);
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void get() throws ServletException, IOException, Http404 {
        response.sendRedirect(redirectUrl);
    }

    @Override
    public void post() throws ServletException, IOException, Http404 {
        get();
    }

    @Override
    public void put() throws ServletException, IOException, Http404 {
        get();
    }

    @Override
    public void delete() throws ServletException, IOException, Http404 {
        get();
    }

    @Override
    protected void parseUrl() {
        urlMatches = true;
    }

}
