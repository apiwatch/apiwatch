/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.models;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Date;

import org.apiwatch.models.APIScope;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.errors.SerializationError;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.spring.DaoFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "component_versions")
public class Version implements Comparable<Version> {

    public static final String COMPONENT_COLUMN = "component_id";
    public static final String NAME_COLUMN = "name";
    public static final String DATE_COLUMN = "date";
    public static final String APIBLOB_COLUMN = "api_blob";
    public static final String APIBLOBFORMAT_COLUMN = "api_blob_format";
    public static final String ALIASOF_COLUMN = "alias_of_id";
    public static final String LATEST = "LATEST";

    private static Dao<Version, String> DAO = null;

    @DatabaseField(generatedId = true, canBeNull = false)
    private int id;

    @DatabaseField(foreign = true, uniqueCombo = true, canBeNull = false,
            columnName = COMPONENT_COLUMN, foreignAutoRefresh = true)
    private Component component;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = NAME_COLUMN)
    private String name;
    
    @DatabaseField(canBeNull = true, columnName = DATE_COLUMN)
    private Date date;

    @DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING, columnName = APIBLOB_COLUMN)
    private String apiBlob;

    @DatabaseField(canBeNull = true, columnName = APIBLOBFORMAT_COLUMN)
    private String apiBlobFormat;

    @DatabaseField(foreign = true, canBeNull = true, columnName = ALIASOF_COLUMN,
            foreignAutoRefresh = true)
    private Version aliasOf;

    @ForeignCollectionField
    private transient ForeignCollection<Version> aliases;

    Version() {
        /* constructor used by ORM */
    }

    public Version(Component component, String name, String apiBlob, String apiBlobFormat) {
        this.component = component;
        this.name = name;
        this.date = new Date();
        this.apiBlob = apiBlob;
        this.apiBlobFormat = apiBlobFormat;
    }

    public Version(Component component, String name, Version aliasOf) {
        this.component = component;
        this.name = name;
        this.aliasOf = aliasOf;
        this.date = null;
        this.apiBlob = null;
        this.apiBlobFormat = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiBlob() {
        return apiBlob;
    }

    public void setApiBlob(String apiBlob) {
        this.apiBlob = apiBlob;
    }

    public String getApiBlobFormat() {
        return apiBlobFormat;
    }

    public void setApiBlobFormat(String apiBlobFormat) {
        this.apiBlobFormat = apiBlobFormat;
    }

    public Version getAliasOf() {
        return aliasOf;
    }

    public void setAliasOf(Version aliasOf) {
        this.aliasOf = aliasOf;
    }

    public ForeignCollection<Version> getAliases() {
        return aliases;
    }

    public void setAliases(ForeignCollection<Version> aliases) {
        this.aliases = aliases;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return component.getName() + "-" + name;
    }

    public APIScope getAPIScope() throws SerializationError {
        return Serializers.loadAPIScope(new StringReader(apiBlob), apiBlobFormat);
    }

    public void setAPIScope(APIScope scope, String format) throws SerializationError {
        Writer w = new StringWriter();
        Serializers.dumpAPIScope(scope, w, format);
        apiBlob = w.toString();
        apiBlobFormat = format;
    }

    public static Dao<Version, String> dao() {
        if (DAO == null) {
            throw new RuntimeException(
                    "you must initialize the DAO with initDAO() before accessing it.");
        } else {
            return DAO;
        }
    }
    
    public static synchronized void initDAO(ConnectionSource dbConnection) throws SQLException {
        if (dbConnection != null) {
            DAO = DaoFactory.createDao(dbConnection, Version.class);
        } else {
            DAO = null;
        }
    }

    @Override
    public int compareTo(Version o) {
        Version realThis = this;
        Version realOther = o;
        while (realThis.aliasOf != null) {
            realThis = realThis.aliasOf;
        }
        while (realOther.aliasOf != null) {
            realOther = realOther.aliasOf;
        }
        int cmp = realThis.date.compareTo(realOther.date);
        if (cmp == 0) {
            cmp = this.name.compareTo(o.name);
        }
        return cmp;
    }

}
