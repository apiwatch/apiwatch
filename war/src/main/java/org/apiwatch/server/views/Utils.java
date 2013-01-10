/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import java.sql.SQLException;

import org.apiwatch.server.models.Component;
import org.apiwatch.server.models.Version;
import org.apiwatch.util.errors.Http404;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

public class Utils {

    public static Version getVersion(String componentName, String versionName) throws SQLException,
            Http404
    {
        Component comp = Component.dao().queryForId(componentName);
        if (comp == null) {
            throw new Http404();
        }

        QueryBuilder<Version, String> builder = Version.dao().queryBuilder();
        PreparedQuery<Version> query = builder.where().eq(Version.COMPONENT_COLUMN, comp).and()
                .eq(Version.NAME_COLUMN, versionName).prepare();
        Version ver = Version.dao().queryForFirst(query);
        if (ver == null) {
            throw new Http404();
        }

        return ver;
    }
    
    public static Version resolveRealVersion(Version ver) {
        Version realVersion = ver;
        while (realVersion.getAliasOf() != null) {
            realVersion = realVersion.getAliasOf();
        }
        return realVersion;
    }

}
