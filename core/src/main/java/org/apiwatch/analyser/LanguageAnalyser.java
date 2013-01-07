/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.analyser;

import java.io.IOException;
import java.util.Map;

import org.apiwatch.models.APIScope;
import org.apiwatch.util.errors.ParseError;


public interface LanguageAnalyser {
    
    String[] fileExtensions();
    String language();
    Option[] options();
    
    APIScope analyse(String sourceFile, Map<String, Object> options) throws IOException, ParseError;
    
}
