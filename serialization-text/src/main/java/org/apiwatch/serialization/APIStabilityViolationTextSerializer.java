/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.serialization;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.util.errors.SerializationError;

public class APIStabilityViolationTextSerializer implements APIStabilityViolationSerializer {

    @Override
    public String format() {
        return TextSerializer.FORMAT;
    }

    @Override
    public void dump(List<APIStabilityViolation> v, Writer w) throws SerializationError {
        try {
            for (APIStabilityViolation violation : v) {
                w.write((violation.toString() + "\n"));
            }
        } catch (IOException e) {
            throw new SerializationError(e);
        }
    }

    @Override
    public List<APIStabilityViolation> load(Reader i) {
        throw new RuntimeException("Cannot deserialize 'text' format");
    }

}
