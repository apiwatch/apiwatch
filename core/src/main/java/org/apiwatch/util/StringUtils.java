package org.apiwatch.util;

import java.util.Collection;

public class StringUtils {
    
    public static String join(String separator, Collection<String> strings) {
        if (separator == null) {
            throw new IllegalArgumentException("separator cannot be null");
        }
        if (strings != null && strings.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : strings) {
                sb.append(separator);
                sb.append(s);
            }
            return sb.substring(separator.length());
        } else {
            return "";
        }
    }
    
}
