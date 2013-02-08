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

public class NotFoundView extends View {

    public NotFoundView(HttpServletRequest req, HttpServletResponse resp) {
        super(req, resp);
        context.put("page_title", "404 Not found");
    }

    public void render() throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        if (acceptsHTML()) {
            context.put("request_path", request.getRequestURI());
            renderToTemplate(context);
        } else {
            response.setContentType(TEXT_CONTENT_TYPE);
            response.getWriter().write("Resource not found: " + request.getRequestURI() + "\r\n");
            response.getWriter().flush();
        }
    }

    @Override
    public void get() throws ServletException, IOException, Http404 {
        render();
    }

    @Override
    public void post() throws ServletException, IOException, Http404 {
        render();
    }

    @Override
    public void put() throws ServletException, IOException, Http404 {
        render();
    }

    @Override
    public void delete() throws ServletException, IOException, Http404 {
        render();
    }

    @Override
    protected void parseUrl() {
        urlMatches = true;
    }

}
