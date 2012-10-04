/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class APIStabilityViolationJSONSerializer implements APIStabilityViolationSerializer {

    @Override
    public String format() {
        return JSONSerializer.FORMAT;
    }

    @Override
    public void dump(List<APIStabilityViolation> v, Writer o) throws SerializationError {
        XStream xstream = initializeXStream();
        try {
            o.write(xstream.toXML(v));
        } catch (IOException e) {
            throw new SerializationError(e);
        }
    }

    @Override
    public List<APIStabilityViolation> load(Reader i) {
        XStream xstream = initializeXStream();
        @SuppressWarnings("unchecked")
        List<APIStabilityViolation> violation = (List<APIStabilityViolation>) xstream.fromXML(i);
        return violation;
    }
    
    private XStream initializeXStream() {
        XStream x = new XStream(new JettisonMappedXmlDriver());
        x.alias("APIScope", org.apiwatch.models.APIScope.class);
        x.alias("ArrayType", org.apiwatch.models.ArrayType.class);
        x.alias("ChangeType", org.apiwatch.models.ChangeType.class);
        x.alias("ComplexType", org.apiwatch.models.ComplexType.class);
        x.alias("Function", org.apiwatch.models.Function.class);
        x.alias("Severity", org.apiwatch.models.Severity.class);
        x.alias("SimpleType", org.apiwatch.models.SimpleType.class);
        x.alias("Variable", org.apiwatch.models.Variable.class);
        x.alias("Visibility", org.apiwatch.models.Visibility.class);
        x.alias("APIDifference", org.apiwatch.models.APIDifference.class);
        x.omitField(org.apiwatch.models.APIScope.class, "subScopes");
        x.omitField(org.apiwatch.models.APIScope.class, "symbols");
        x.omitField(org.apiwatch.models.ComplexType.class, "symbols");
        x.omitField(org.apiwatch.models.Function.class, "arguments");
        return x;
    }

}
