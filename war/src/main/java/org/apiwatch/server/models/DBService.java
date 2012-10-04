/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.models;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBService {

    private static Logger LOGGER = Logger.getLogger(DBService.class);
    private static DBService INSTANCE = null;

    private ConnectionSource dbConnection;

    public ConnectionSource getDbConnection() {
        return dbConnection;
    }

    private DBService(String url, String username, String password) throws SQLException,
            IOException
    {
        LOGGER.info("Opening JDBC connection with " + url);
        dbConnection = new JdbcConnectionSource(url.replace("\\", "\\\\"), username, password);
        Component.initDAO(dbConnection);
        if (Component.dao().isTableExists() == false) {
            TableUtils.createTable(dbConnection, Component.class);
        }
        Version.initDAO(dbConnection);
        if (Version.dao().isTableExists() == false) {
            TableUtils.createTableIfNotExists(dbConnection, Version.class);
        }
    }

    public static void init(String url, String username, String password) throws SQLException,
            IOException
    {
        INSTANCE = new DBService(url, username, password);
    }

    public static void tearDown() throws SQLException {
        if (INSTANCE != null && INSTANCE.dbConnection != null) {
            LOGGER.info("Closing database connections...");
            Component.dao().commit(INSTANCE.dbConnection.getReadWriteConnection());
            Version.dao().commit(INSTANCE.dbConnection.getReadWriteConnection());
            INSTANCE.dbConnection.close();
            Component.initDAO(null);
            Version.initDAO(null);
            INSTANCE.dbConnection = null;
        }
    }

    public static DBService instance() {
        if (INSTANCE == null) {
            throw new RuntimeException("You must call init() before accessing the instance.");
        }
        return INSTANCE;
    }

}
