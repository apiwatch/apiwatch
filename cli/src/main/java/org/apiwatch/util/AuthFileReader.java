/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class AuthFileReader {

    public String username = null;
    public String password = null;

    public AuthFileReader() {
        try {
            String home = System.getProperty("user.home");
            FileInputStream fis = new FileInputStream(new File(home, ".apiwatchrc"));
            Properties prop = new Properties();
            prop.load(fis);
            username = prop.getProperty("username");
            password = prop.getProperty("password");
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
