/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
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

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apiwatch.util.errors.Http404;

public abstract class View {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String HTML_CONTENT_TYPE = "text/html";
    public static final String TEXT_CONTENT_TYPE = "text/plain";
    public static final String JSON_CONTENT_TYPE = "application/json";

    protected final HttpServletRequest request;
    protected final HttpServletResponse response;
    protected final Context context;
    protected static final Logger LOGGER = Logger.getLogger(View.class);

    public View(HttpServletRequest req, HttpServletResponse resp) {
        this.request = req;
        this.response = resp;
        this.response.setCharacterEncoding(DEFAULT_ENCODING);
        this.context = new VelocityContext();
        this.context.put("apiwatch_version", getClass().getPackage().getImplementationVersion());
        this.context.put("page_title", "[no title]");
    }

    public void get() throws ServletException, IOException, Http404 {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void post() throws ServletException, IOException, Http404 {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void put() throws ServletException, IOException, Http404 {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void delete() throws ServletException, IOException, Http404 {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public boolean acceptsHTML() {
        String accept = request.getHeader("accept");
        return accept != null && accept.toLowerCase().contains("html");
    }
    
    public void renderToTemplate(Context ctx) throws IOException {
        response.setContentType(HTML_CONTENT_TYPE);
        Template template = Velocity.getTemplate(templatePath(), DEFAULT_ENCODING);
        template.merge(ctx, response.getWriter());
        response.getWriter().flush();
    }
    
    public void renderToJSON(String content) throws IOException {
        response.setContentType(JSON_CONTENT_TYPE);
        response.getWriter().write(content);
        response.getWriter().flush();
    }

    public String templatePath() {
        return "/templates/" + getClass().getSimpleName() + ".vm";
    }

}
