/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.List;
import java.util.Set;

public class ArrayType extends Type {

    public String elementType;

    public ArrayType(String name, String language, String sourceFile, Visibility visibility,
            APIElement parent, Set<String> modifiers, String elementType)
    {
        super(name, language, sourceFile, visibility, parent, modifiers);
        this.elementType = elementType;
    }

    @Override
    public String name() {
        return elementType + name;
    }
    
    @Override
    public List<APIDifference> getDiffs(APIElement other) {
        List<APIDifference> diffs = super.getDiffs(other);

        if (other instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) other;
            if (elementType != null && !elementType.equals(arrayType.elementType)
                    || elementType == null && arrayType.elementType != null) {
                diffs.add(new APIDifference(ChangeType.CHANGED, this, other, "elementType",
                        elementType, arrayType.elementType));
            }
        }

        return diffs;
    }
    
}
