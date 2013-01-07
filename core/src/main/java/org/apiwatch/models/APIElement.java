/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.ArrayList;
import java.util.List;

public abstract class APIElement {

    public static final String PATH_SEPARATOR = "/";
    public static final String IDENT_SEPARATOR = ":";

    public String name;
    public String language;
    public String sourceFile;
    public Visibility visibility;
    public transient APIElement parent; /* not serialized */

    public APIElement(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent)
    {
        super();
        this.name = name;
        this.language = language;
        this.sourceFile = sourceFile;
        this.visibility = visibility != null ? visibility : Visibility.SCOPE;
        this.parent = parent;
    }

    public String name() {
        return name;
    }

    public String ident() {
        return getClass().getSimpleName() + IDENT_SEPARATOR + name();
    }

    public String path() {
        if (parent != null) {
            return parent.path() + PATH_SEPARATOR + ident();
        } else {
            return ident();
        }
    }

    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = new ArrayList<APIDifference>();

        if (name() != null && !name().equals(other.name()) || name() == null
                && other.name() != null) {
            diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "name", name(), other
                    .name()));
        }
        if (language != null && !language.equals(other.language) || language == null
                && other.language != null) {
            diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "language", language,
                    other.language));
        }
        if (sourceFile != null && !sourceFile.equals(other.sourceFile) || sourceFile == null
                && other.sourceFile != null) {
            diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "sourceFile", sourceFile,
                    other.sourceFile));
        }
        if (visibility != other.visibility) {
            diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "visibility", visibility,
                    other.visibility));
        }
        return diffs;
    }


    public String getSourceFile() {
        return sourceFile;
    }
    
    public Visibility getVisibility() {
        return visibility;
    }
    
    @Override
    public String toString() {
        return ident();
    }

    @Override
    public int hashCode() {
        return ident().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof APIElement) {
            APIElement other = (APIElement) obj;
            return ident().equals(other.ident());
        }
        return false;
    }

}
