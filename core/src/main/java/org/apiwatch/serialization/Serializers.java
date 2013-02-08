/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.serialization;

import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiwatch.analyser.Analyser;
import org.apiwatch.models.APIElement;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.util.errors.SerializationError;
import org.apiwatch.util.errors.UnknownSerializationFormat;

public class Serializers {

    static Logger LOGGER = Logger.getLogger(Analyser.class.getName());
    private static Map<String, APIScopeSerializer> APISCOPE_BY_FORMAT = null;
    private static Map<String, APIStabilityViolationSerializer> VIOLATION_BY_FORMAT = null;

    private static void discoverSerializers() {
        if (APISCOPE_BY_FORMAT == null) {
            APISCOPE_BY_FORMAT = new HashMap<String, APIScopeSerializer>();
            ServiceLoader<APIScopeSerializer> scopeLoader = ServiceLoader
                    .load(APIScopeSerializer.class);
            LOGGER.trace("Discovering APIScopeSerializer implementations in class path...");
            for (APIScopeSerializer impl : scopeLoader) {
                LOGGER.debug(String.format("Found %s. format=%s", impl.getClass(), impl.format()));
                APISCOPE_BY_FORMAT.put(impl.format(), impl);
            }
        }

        if (VIOLATION_BY_FORMAT == null) {
            VIOLATION_BY_FORMAT = new HashMap<String, APIStabilityViolationSerializer>();
            ServiceLoader<APIStabilityViolationSerializer> violationLoader = ServiceLoader
                    .load(APIStabilityViolationSerializer.class);
            LOGGER.trace("Discovering APIScopeSerializer implementations in class path...");
            for (APIStabilityViolationSerializer impl : violationLoader) {
                LOGGER.debug(String.format("Found %s. format=%s", impl.getClass(), impl.format()));
                VIOLATION_BY_FORMAT.put(impl.format(), impl);
            }
        }
    }

    public static Set<String> availableFormats(Class<?> dataType) {
        discoverSerializers();
        if (APIElement.class.isAssignableFrom(dataType)) {
            return APISCOPE_BY_FORMAT.keySet();
        } else if (APIStabilityViolation.class.isAssignableFrom(dataType)) {
            return VIOLATION_BY_FORMAT.keySet();
        } else {
            return new HashSet<String>();
        }
    }

    public static void dumpAPIScope(APIScope s, Writer w, String format) throws SerializationError
    {
        discoverSerializers();
        LOGGER.trace("Serializing to '" + format + "'...");
        APIScopeSerializer serializer = APISCOPE_BY_FORMAT.get(format);
        if (serializer != null) {
            serializer.dump(s, w);
        } else {
            throw new UnknownSerializationFormat(format);
        }
    }

    public static APIScope loadAPIScope(Reader r, String format) throws SerializationError
    {
        discoverSerializers();
        LOGGER.trace("Deserializing from '" + format + "'...");
        APIScopeSerializer serializer = APISCOPE_BY_FORMAT.get(format);
        if (serializer != null) {
            return serializer.load(r);
        } else {
            throw new UnknownSerializationFormat(format);
        }
    }

    public static void dumpViolations(List<APIStabilityViolation> s, Writer w, String format)
            throws SerializationError
    {
        discoverSerializers();
        LOGGER.trace("Serializing to '" + format + "'...");
        APIStabilityViolationSerializer serializer = VIOLATION_BY_FORMAT.get(format);
        if (serializer != null) {
            serializer.dump(s, w);
        } else {
            throw new UnknownSerializationFormat(format);
        }
    }

    public static List<APIStabilityViolation> loadViolations(Reader r, String format)
            throws SerializationError
    {
        discoverSerializers();
        LOGGER.trace("Deserializing from '" + format + "'...");
        APIStabilityViolationSerializer serializer = VIOLATION_BY_FORMAT.get(format);
        if (serializer != null) {
            return serializer.load(r);
        } else {
            throw new UnknownSerializationFormat(format);
        }
    }

}
