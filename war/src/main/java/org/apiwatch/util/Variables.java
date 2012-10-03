/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variables {

    // matches ${VAR_NAME} or $VAR_NAME
    public static final Pattern VAR_REX = Pattern.compile("\\$\\{([\\w\\.]+)\\}|\\$([\\w\\.]+)");

    public static String resolveAll(String input) {
        return resolveVariables(input, VariableType.ALL);
    }

    public static String resolveSystemProperties(String input) {
        return resolveVariables(input, VariableType.SYSTEM_PROPERTY);
    }

    public static String resolveEnvVars(String input) {
        return resolveVariables(input, VariableType.ENV_VAR);
    }

    public static String resolveVariables(String input, VariableType type) {
        if (null == input) {
            return null;
        }
        Matcher m = VAR_REX.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String name = m.group(1) != null ? m.group(1) : m.group(2);
            String value = getValue(name, type);
            m.appendReplacement(sb, value);
        }
        m.appendTail(sb);
        return sb.toString();

    }

    private static String getValue(String name, VariableType type) {
        String value = null;
        switch (type) {
        case SYSTEM_PROPERTY:
            value = System.getProperty(name);
            break;
        case ENV_VAR:
            value = System.getenv(name);
            break;
        default:
            value = System.getenv(name);
            if (value == null) {
                value = System.getProperty(name);
            }
        }
        return value != null ? value : "";
    }

    private static enum VariableType {
        SYSTEM_PROPERTY, ENV_VAR, ALL;
    }

}
