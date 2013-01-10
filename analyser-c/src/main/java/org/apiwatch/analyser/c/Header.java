/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser.c;

public class Header {

    public int line;
    public boolean isSystemHeader;
    public int sourceLine;
    public String sourceFile;

    public Header(int line, boolean isSystemHeader, int sourceLine, String sourceFile) {
        this.line = line;
        this.isSystemHeader = isSystemHeader;
        this.sourceLine = sourceLine;
        this.sourceFile = sourceFile;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Header [line=");
        builder.append(line);
        builder.append(", isSystemHeader=");
        builder.append(isSystemHeader);
        builder.append(", sourceLine=");
        builder.append(sourceLine);
        builder.append(", sourceFile=");
        builder.append(sourceFile);
        builder.append("]");
        return builder.toString();
    }
    
}
