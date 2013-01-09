/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

public class IniFile {
    
    public static Map<String, Map<String, String>> read(File file) throws InvalidFileFormatException,
            IOException
    {
        Ini ini = new Ini();
        ini.load(file);
        return read(ini);
    }

    public static Map<String, Map<String, String>> read(InputStream is) throws InvalidFileFormatException,
            IOException
    {
        Ini ini = new Ini();
        ini.load(is);
        return read(ini);
    }

    public static Map<String, Map<String, String>> read(Reader r) throws InvalidFileFormatException,
            IOException
    {
        Ini ini = new Ini();
        ini.load(r);
        return read(ini);
    }

    private static Map<String, Map<String, String>> read(Ini ini) {
        Map<String, Map<String, String>> iniSections = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, Section> section : ini.entrySet()) {
            Map<String, String> sectionValues = new HashMap<String, String>();
            for (Map.Entry<String, String> val : section.getValue().entrySet()) {
                sectionValues.put(val.getKey(), val.getValue());
            }
            iniSections.put(section.getKey(), sectionValues);
        }
        return iniSections;
    }
}
