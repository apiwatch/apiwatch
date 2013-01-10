/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import org.apache.commons.lang.StringEscapeUtils;

public class PrettyPrinter {

    public String prettyPrint(Object obj) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof Iterable) {
            @SuppressWarnings({ "rawtypes" })
            Iterable it = (Iterable) obj;
            boolean hasElements = it.iterator().hasNext();
            for (Object x : it) {
                sb.append(x.toString() + ", ");
            }
            if (hasElements) {
                sb.delete(sb.length() - ", ".length(), sb.length());
            }
        } else {
            sb.append(obj.toString());
        }
        return StringEscapeUtils.escapeHtml(sb.toString());
    }
    
}
