/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.Severity;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.errors.SerializationError;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

/* package */class ArgsUtil {

    private static final Logger LOGGER = Logger.getLogger(ArgsUtil.class);

    public static final String VERSION = ArgsUtil.class.getPackage().getImplementationVersion();
    public static final String VERSION_NAME = "APIWATCH version " + VERSION;

    /* package */static Level[] LOG_LEVELS = new Level[] { Level.TRACE, Level.DEBUG, Level.INFO,
            Level.WARN, Level.ERROR };

    /* package */static class IntegerArgument implements ArgumentType<Integer> {

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

    /* package */static class IniFileArgument implements
            ArgumentType<Map<String, Map<String, String>>>
    {

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
    
    /* package */static class IniFile {
        static Map<String, Map<String, String>> read(File file) throws InvalidFileFormatException,
                IOException
        {
            Ini ini = new Ini();
            ini.load(file);
            return read(ini);
        }

        static Map<String, Map<String, String>> read(InputStream is)
                throws InvalidFileFormatException, IOException
        {
            Ini ini = new Ini();
            ini.load(is);
            return read(ini);
        }

        static Map<String, Map<String, String>> read(Reader r)
                throws InvalidFileFormatException, IOException
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
    

    /* package */static class SeverityArgument implements ArgumentType<Severity> {

        @Override
        public Severity convert(ArgumentParser parser, Argument arg, String value)
                throws ArgumentParserException
        {
            Severity val = null;
            try {
                val = Severity.valueOf(value);
            } catch (Exception e) {
                throw new ArgumentParserException("must be one of " + Severity.values(), parser,
                        arg);
            }
            return val;
        }

    }

    /* package */static class LogLevelArgument implements ArgumentType<Level> {

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

    /* package */static class ListLanguagesAction implements ArgumentAction {

        @Override
        public void run(ArgumentParser parser, Argument arg, Map<String, Object> attrs,
                String flag, Object value) throws ArgumentParserException
        {
            System.out.println("TODO TODO TODO TODO");
            System.exit(0);
        }

        @Override
        public void onAttach(Argument arg) {

        }

        @Override
        public boolean consumeArgument() {
            return false;
        }

    }

    /* package */static APIScope getAPIData(String source, String encoding, String username,
            String password) throws IOException, SerializationError, HttpException
    {
        File file = new File(source);
        APIScope scope = null;
        if (file.isFile()) {
            /* get format from file extension */
            String format = source.substring(source.lastIndexOf('.') + 1);
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, encoding);
            scope = Serializers.loadAPIScope(reader, format);
            reader.close();
            in.close();
        } else {
            /* maybe source is a URL */
            DefaultHttpClient client = new DefaultHttpClient();
            if (username != null && password != null) {
                client.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
                        new UsernamePasswordCredentials(username, password));
            }
            HttpResponse response = client.execute(new HttpGet(source));
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new HttpException(response.getStatusLine().getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            ContentType contentType = ContentType.fromHeader(entity.getContentType().getValue());
            if (entity.getContentEncoding() != null) {
                encoding = entity.getContentEncoding().getValue();
            } else if (contentType.charset != null) {
                encoding = contentType.charset;
            }
            String format = contentType.type;
            InputStream in = entity.getContent();
            Reader reader = new InputStreamReader(in, encoding);
            scope = Serializers.loadAPIScope(reader, format);
            reader.close();
            in.close();
            client.getConnectionManager().shutdown();
        }
        return scope;
    }

    private static final Pattern URL_RX = Pattern
            .compile("http[s]?://.+", Pattern.CASE_INSENSITIVE);

    /* package */static void putAPIData(APIScope scope, String format, String encoding,
            String location, String username, String password) throws SerializationError,
            IOException, HttpException
    {
        if (URL_RX.matcher(location).matches()) {
            DefaultHttpClient client = new DefaultHttpClient();
            if (username != null && password != null) {
                client.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
                        new UsernamePasswordCredentials(username, password));
            }
            HttpPost req = new HttpPost(location);
            StringWriter writer = new StringWriter();
            Serializers.dumpAPIScope(scope, writer, format);
            HttpEntity entity = new StringEntity(writer.toString(), encoding);
            req.setEntity(entity);
            req.setHeader("content-type", format);
            req.setHeader("content-encoding", encoding);
            HttpResponse response = client.execute(req);
            client.getConnectionManager().shutdown();
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new HttpException(response.getStatusLine().getReasonPhrase());
            }
            LOGGER.info("Sent results to URL: " + location);
        } else {
            File dir = new File(location);
            dir.mkdirs();
            File file = new File(dir, "api." + format);
            OutputStream out = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(out, encoding);
            Serializers.dumpAPIScope(scope, writer, format);
            writer.flush();
            writer.close();
            out.close();
            LOGGER.info("Wrote results to file: " + file);
        }
    }

    /* package */static class ContentType {

        String type;
        String charset;

        public ContentType(String type, String charset) {
            this.type = type;
            this.charset = charset;
        }

        public static ContentType fromHeader(String header) {
            String type = null;
            String charset = null;
            if (header != null && header.contains(";charset=")) {
                String[] split = header.split(";charset=");
                type = split[0];
                charset = split[1];
            } else {
                type = header;
            }
            return new ContentType(type, charset);
        }

    }

    /* package */static class AuthFileReader {

        String username = null;
        String password = null;

        public AuthFileReader() {
            try {
                String home = System.getProperty("user.home");
                FileInputStream fis = new FileInputStream(home + "/.apiwatchrc");
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
}
