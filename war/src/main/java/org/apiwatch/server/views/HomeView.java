/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apiwatch.server.models.Component;
import org.apiwatch.server.models.Version;
import org.apiwatch.util.errors.Http404;

public class HomeView extends View {

    public HomeView(HttpServletRequest req, HttpServletResponse resp) {
        super(req, resp);
        context.put("page_title", "Registered Components");
    }

    @Override
    public void get() throws ServletException, IOException, Http404 {
        try {
            Map<Component, Set<Version>> components = new TreeMap<Component, Set<Version>>();
            for (Component c : Component.dao()) {
                Set<Version> versions = new TreeSet<Version>();
                for (Version v : c.getVersions()) {
                    versions.add(v);
                }
                components.put(c, versions);
            }
            context.put("components", components);
            renderToTemplate(context);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void parseUrl() {
        urlMatches = "/".equals(url);
    }

}

