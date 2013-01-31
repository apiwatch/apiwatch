/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.serialization;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apiwatch.models.APIScope;
import org.apiwatch.util.APIWatchExtension;
import org.apiwatch.util.errors.SerializationError;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

@APIWatchExtension
public class APIScopeJSONSerializer implements APIScopeSerializer {

    @Override
    public String format() {
        return JSONSerializer.FORMAT;
    }
    
    @Override
    public void dump(APIScope s, Writer w) throws SerializationError {
        XStream xstream = initializeXStream();
        try {
            w.write(xstream.toXML(s));
        } catch (IOException e) {
            throw new SerializationError(e);
        }
    }

    @Override
    public APIScope load(Reader r) {
        XStream xstream = initializeXStream();
        APIScope scope = (APIScope) xstream.fromXML(r);
        SerializationUtils.restoreParents(scope);
        return scope;
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
        return x;
    }
    
}
