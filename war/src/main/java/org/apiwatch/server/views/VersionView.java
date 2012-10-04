/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apiwatch.models.Visibility;
import org.apiwatch.serialization.JSTreeSerializer;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.server.models.Component;
import org.apiwatch.server.models.DBService;
import org.apiwatch.server.models.Version;
import org.apiwatch.util.IO;
import org.apiwatch.util.errors.Http404;
import org.apiwatch.util.errors.SerializationError;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public class VersionView extends View {

    private final String component;
    private final String version;

    public VersionView(HttpServletRequest req, HttpServletResponse resp, String component,
            String version)
    {
        super(req, resp);
        this.component = component;
        this.version = version;
    }

    @Override
    public void get() throws ServletException, IOException, Http404 {
        try {

            Component comp = Component.dao().queryForId(component);
            if (comp == null) {
                throw new Http404();
            }

            QueryBuilder<Version, String> builder = Version.dao().queryBuilder();
            PreparedQuery<Version> query = builder.where().eq(Version.COMPONENT_COLUMN, comp).and()
                    .eq(Version.NAME_COLUMN, version).prepare();
            Version ver = Version.dao().queryForFirst(query);
            if (ver == null) {
                throw new Http404();
            }

            Version realVersion = ver;
            while (realVersion.getAliasOf() != null) {
                // resolve the real version
                realVersion = realVersion.getAliasOf();
            }

            if (acceptsHTML()) {
                String vis = request.getParameter("visibility");
                Visibility threshold;
                if (vis != null) {
                    threshold = Visibility.valueOf(vis);
                } else {
                    threshold = Visibility.SCOPE;
                }
                context.put("page_title", ver.toString());
                context.put("version", ver);
                context.put("jsTreeData",
                        JSTreeSerializer.toJSTreeData(realVersion.getAPIScope(), threshold));
                context.put("threshold", threshold);
                context.put("visibilities", Visibility.values());
                renderToTemplate(context);
            } else {
                response.setContentType(realVersion.getApiBlobFormat());
                response.getWriter().write(realVersion.getApiBlob());
                response.getWriter().flush();
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (SerializationError e) {
            throw new ServletException(e);
        }
    }

    private static final String VERSION_ALREADY_EXISTS = "version or alias '%s' already exists for component '%s'";

    @Override
    public void post() throws ServletException, IOException, Http404 {
        try {

            Version ver = TransactionManager.callInTransaction(DBService.instance()
                    .getDbConnection(), new Callable<Version>() {
                @Override
                public Version call() throws Exception {
                    Component comp = Component.dao().createIfNotExists(new Component(component));

                    // check if version already exists
                    QueryBuilder<Version, String> builder = Version.dao().queryBuilder();
                    PreparedQuery<Version> nameQuery = builder.where()
                            .eq(Version.COMPONENT_COLUMN, comp).and()
                            .eq(Version.NAME_COLUMN, version).prepare();
                    Version ver = Version.dao().queryForFirst(nameQuery);
                    if (ver != null) {
                        // a version cannot be registered twice!
                        throw new IllegalArgumentException();
                    }

                    // check if the body contains a well formed serialized APIScope
                    String body = IO.readToString(request.getReader());
                    Serializers.loadAPIScope(new StringReader(body), request.getContentType());

                    // create the new version
                    ver = Version.dao().createIfNotExists(
                            new Version(comp, version, body, request.getContentType()));

                    // move the LATEST alias to the new version
                    PreparedQuery<Version> latestQuery = Version.dao().queryBuilder().where()
                            .eq(Version.COMPONENT_COLUMN, comp).and()
                            .eq(Version.NAME_COLUMN, Version.LATEST).prepare();
                    Version latest = Version.dao().queryForFirst(latestQuery);
                    if (latest != null) {
                        latest.setAliasOf(ver);
                        Version.dao().update(latest);
                    } else {
                        Version.dao().createIfNotExists(new Version(comp, Version.LATEST, ver));
                    }
                    return ver;
                }
            });

            response.setStatus(HttpServletResponse.SC_CREATED);
            LOGGER.info("New version registered " + ver);
            LOGGER.info(component + "-LATEST alias now references " + ver);
        } catch (SQLException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        String.format(VERSION_ALREADY_EXISTS, version, component));
            } else if (e.getCause() instanceof SerializationError) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getCause().toString());
            } else {
                throw new ServletException(e);
            }
        }
    }

}
