/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apiwatch.server.models.Component;
import org.apiwatch.util.errors.Http404;

public class ComponentView extends View {

    private String name;

    public ComponentView(HttpServletRequest req, HttpServletResponse resp) {
        super(req, resp);
    }
    
    @Override
    public void get() throws ServletException, IOException, Http404 {
        try {

            Component comp = Component.dao().queryForId(name);
            if (comp == null) {
                throw new Http404();
            }

            context.put("page_title", "Component: " + name);
            context.put("component", comp);
            
            renderToTemplate(context);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static final Pattern URL_REGEXP = Pattern.compile("^/([^/]+?)/$");
    @Override
    protected void parseUrl() {
        Matcher matcher = URL_REGEXP.matcher(url);
        urlMatches = matcher.matches();
        if (urlMatches) {
            name= matcher.group(1);
        }
    }
    
}
