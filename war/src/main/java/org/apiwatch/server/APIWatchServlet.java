/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apiwatch.server.models.DBService;
import org.apiwatch.server.views.ComponentView;
import org.apiwatch.server.views.DiffView;
import org.apiwatch.server.views.HomeView;
import org.apiwatch.server.views.NotFoundView;
import org.apiwatch.server.views.RedirectView;
import org.apiwatch.server.views.VersionView;
import org.apiwatch.server.views.View;
import org.apiwatch.util.Variables;
import org.apiwatch.util.errors.Http404;

@SuppressWarnings("serial")
public class APIWatchServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(APIWatchServlet.class);
    private static final String DEFAULT_VELOCITY_PROP_FILE = "/velocity.properties";
    private static final String DEFAULT_APIWATCH_PROP_FILE = "/apiwatch.properties";
    private static final String LOG4J_LOGGER_PROPERTY = "runtime.log.logsystem.log4j.logger";

    private static final String APIWATCH_JDBC_URL = "apiwatch.jdbc.url";
    private static final String APIWATCH_JDBC_USERNAME = "apiwatch.jdbc.username";
    private static final String APIWATCH_JDBC_PASSWORD = "apiwatch.jdbc.password";
    
    

    @Override
    public void init() throws ServletException {
        try {
            Properties jdbcProps = new Properties();
            String userProperties = System.getProperty("apiwatch.properties");
            if (userProperties != null) {
                try {
                    LOGGER.debug("Loading user APIWATCH properties from " + userProperties + "...");
                    jdbcProps.load(new FileInputStream(userProperties));
                } catch (IOException e) {
                    LOGGER.error("Failed to load user APIWATCH properties, "
                            + "falling back to default configuration", e);
                    jdbcProps.load(getClass().getResourceAsStream(DEFAULT_APIWATCH_PROP_FILE));
                }
            } else {
                LOGGER.debug("Loading default APIWATCH properties...");
                jdbcProps.load(getClass().getResourceAsStream(DEFAULT_APIWATCH_PROP_FILE));
            }

            String jdbcUrl = Variables.resolveAll(jdbcProps.getProperty(APIWATCH_JDBC_URL));
            String jdbcUsername = jdbcProps.getProperty(APIWATCH_JDBC_USERNAME);
            String jdbcPassword = jdbcProps.getProperty(APIWATCH_JDBC_PASSWORD);
            DBService.init(jdbcUrl, jdbcUsername, jdbcPassword);

            Properties velocityProps = new Properties();
            velocityProps.load(getClass().getResourceAsStream(DEFAULT_VELOCITY_PROP_FILE));
            // force logging with log4j to avoid creation of velocity.log file...
            velocityProps.setProperty(LOG4J_LOGGER_PROPERTY, LOGGER.getName());
            Velocity.init(velocityProps);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            DBService.tearDown();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Pattern COMPONENT_REX = Pattern.compile("^/([^/]+?)/$");
    private static final Pattern VERSION_REX = Pattern.compile("^/([^/]+?)/([^/]+?)/$");
    private static final Pattern DIFF_REX = Pattern.compile("^/([^/]+?)/([^/]+?)/diff/([^/]+?)/$");
    
    private View resolveView(HttpServletRequest req, HttpServletResponse resp) {
        String url = req.getRequestURI();
        
        Matcher componentMatcher = COMPONENT_REX.matcher(url);
        Matcher versionMatcher = VERSION_REX.matcher(url);
        Matcher diffMatcher = DIFF_REX.matcher(url);
        
        if ("/".equals(url)) {
            return new HomeView(req, resp);
        } else if (componentMatcher.matches()) {
            String name = componentMatcher.group(1);
            return new ComponentView(req, resp, name);
        } else if (versionMatcher.matches()) {
            String component = versionMatcher.group(1);
            String version = versionMatcher.group(2);
            return new VersionView(req, resp, component, version);
        } else if (diffMatcher.matches()) {
            String component = diffMatcher.group(1);
            String versionA = diffMatcher.group(2);
            String versionB = diffMatcher.group(3);
            return new DiffView(req, resp, component, versionA, versionB);
        } else {
            if (url.endsWith("/") == false) {
                return new RedirectView(req, resp, url + "/");
            } else {
                return new NotFoundView(req, resp);
            }
            
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        try {
            resolveView(req, resp).get();
        } catch (Http404 e) {
            new NotFoundView(req, resp).render();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        try {
            resolveView(req, resp).post();
        } catch (Http404 e) {
            new NotFoundView(req, resp).render();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        try {
            resolveView(req, resp).put();
        } catch (Http404 e) {
            new NotFoundView(req, resp).render();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        try {
            resolveView(req, resp).delete();
        } catch (Http404 e) {
            new NotFoundView(req, resp).render();
        }
    }

}
