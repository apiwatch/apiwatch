/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
    
    public static String VERSION = null;

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
            
            InputStream stream = getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
            if (stream != null) {
                Attributes attributes = new Manifest(stream).getMainAttributes();
                VERSION = attributes.getValue("Implementation-Version");
            }
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

    
    
    
    
    private View resolveView(HttpServletRequest req, HttpServletResponse resp) {
        
        HomeView homeView = new HomeView(req, resp);
        ComponentView componentView = new ComponentView(req, resp);
        VersionView versionView = new VersionView(req, resp);
        DiffView diffView = new DiffView(req, resp);
        
        if (homeView.isUrlMatches()) {
            return homeView;
        } else if (componentView.isUrlMatches()) {
            return componentView;
        } else if (versionView.isUrlMatches()) {
            return versionView;
        } else if (diffView.isUrlMatches()) {
            return diffView;
        } else {
            if (req.getRequestURI().endsWith("/") == false) {
                return new RedirectView(req, resp, req.getRequestURI() + "/");
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
