/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.serialization;

import java.io.Reader;
import java.io.Writer;

import org.apiwatch.util.errors.SerializationError;

public interface Serializer<T> {
    
    String format();
    void dump(T obj, Writer o) throws SerializationError;
    T load(Reader r) throws SerializationError;
    
}
