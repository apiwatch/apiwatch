/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.models;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.spring.DaoFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "components")
public class Component implements Comparable<Component> {

    @DatabaseField(id = true, canBeNull = false)
    private String name;

    @ForeignCollectionField(eager = true)
    private transient ForeignCollection<Version> versions;
    
    private static Dao<Component, String> DAO = null;

    Component() {
        /* constructor used by ORM */
    }

    public Component(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ForeignCollection<Version> getVersions() {
        return versions;
    }

    public void setVersions(ForeignCollection<Version> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Dao<Component, String> dao() {
        if (DAO == null) {
            throw new RuntimeException(
                    "you must initialize the DAO with initDAO() before accessing it.");
        } else {
            return DAO;
        }
    }

    public static synchronized void initDAO(ConnectionSource dbConnection) throws SQLException {
        if (dbConnection != null) {
            DAO = DaoFactory.createDao(dbConnection, Component.class);
        } else {
            DAO = null;
        }
    }

    @Override
    public int compareTo(Component o) {
        return name.compareTo(o.name);
    }

}
