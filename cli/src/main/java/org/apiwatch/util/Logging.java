/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

public class Logging {

    private static final String PROPERTY_NAME = "log4j.properties";
    private static final String DEFAULT_LAYOUT = "[%p] %m%n";

    public static void configureLogging() {
        configureLogging(null);
    }
    
    public static void configureLogging(Level defaultLevel) {
        Logger.getRootLogger().removeAllAppenders();
        String configFile = System.getProperty(PROPERTY_NAME);
        if (configFile != null && configFile.endsWith(".xml")) {
            DOMConfigurator.configure(configFile);
        } else if (configFile != null && configFile.endsWith(".properties")) {
            PropertyConfigurator.configure(configFile);
        } else {
            if (defaultLevel == null) {
                defaultLevel = Level.WARN;
            }
            Layout layout = new PatternLayout(DEFAULT_LAYOUT);
            Appender appender = new ConsoleAppender(layout, ConsoleAppender.SYSTEM_ERR);
            BasicConfigurator.configure(appender);
            Logger.getRootLogger().setLevel(defaultLevel);
        }
    }

}
