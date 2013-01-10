/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apiwatch.models.Severity;
import org.ini4j.InvalidFileFormatException;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

public class ArgTypes {

    public static class IntegerArgument implements ArgumentType<Integer> {

        @Override
        public Integer convert(ArgumentParser parser, Argument arg, String value)
                throws ArgumentParserException
        {
            int val;
            try {
                val = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                throw new ArgumentParserException("must be a positive integer", parser, arg);
            }
            if (val <= 0) {
                throw new ArgumentParserException("must be a positive integer", parser, arg);
            }
            return val;
        }

    }

    public static class ExtensionsArgument implements ArgumentType<Map<String, String>> {

        @Override
        public Map<String, String> convert(ArgumentParser parser, Argument arg, String value)
                throws ArgumentParserException
        {
            Map<String, String> result = new HashMap<String, String>();

            try {
                for (String token : value.split(";")) {
                    String language = token.split("=")[0];
                    for (String extension : token.split("=")[1].split(",")) {
                        // trim spaces, convert to lower case and remove the leading dot from the
                        // extension, if any
                        result.put(extension.trim().replaceAll("^\\.", "").toLowerCase(), language);
                    }
                }
            } catch (Exception e) {
                throw new ArgumentParserException("wrong syntax", parser, arg);
            }

            return result;
        }

    }

    public static class IniFileArgument implements ArgumentType<Map<String, Map<String, String>>> {

        @Override
        public Map<String, Map<String, String>> convert(ArgumentParser parser, Argument arg,
                String value) throws ArgumentParserException
        {
            File file = new File(value);
            try {
                if (file.isFile()) {
                    // first we read the default config
                    InputStream ini = this.getClass().getResourceAsStream("/rules-config.ini");
                    Map<String, Map<String, String>> iniSections = IniFile.read(ini);

                    // then override it with user settings
                    iniSections.putAll(IniFile.read(file));

                    return iniSections;
                } else {
                    throw new ArgumentParserException("File '" + file + "' does not exist.",
                            parser, arg);
                }
            } catch (SecurityException e) {
                throw new ArgumentParserException(e.getMessage(), e, parser, arg);
            } catch (InvalidFileFormatException e) {
                throw new ArgumentParserException(e.getMessage(), e, parser, arg);
            } catch (IOException e) {
                throw new ArgumentParserException(e.getMessage(), e, parser, arg);
            }
        }
    }

    public static class LogLevelArgument implements ArgumentType<Level> {

        @Override
        public Level convert(ArgumentParser parser, Argument arg, String value)
                throws ArgumentParserException
        {
            Level level = Level.toLevel(value, null);
            if (level == null) {
                throw new ArgumentParserException("'" + value + "' is not a valid log level.",
                        parser, arg);
            }
            return level;
        }

    }
    
    public static class SeverityArgument implements ArgumentType<Severity> {

        @Override
        public Severity convert(ArgumentParser parser, Argument arg, String value)
                throws ArgumentParserException
        {
            try {
                return Severity.valueOf(value);
            } catch (Exception e) {
                throw new ArgumentParserException("must be one of " + Severity.values(), parser,
                        arg);
            }
        }

    }

}
