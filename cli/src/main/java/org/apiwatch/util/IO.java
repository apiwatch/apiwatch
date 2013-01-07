package org.apiwatch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.apiwatch.models.APIScope;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.errors.SerializationError;

public class IO {

    private static final Logger LOGGER = Logger.getLogger(IO.class);

    private static class ContentType {

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

    public static APIScope getAPIData(String source, String encoding, String username,
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

    public static void putAPIData(APIScope scope, String format, String encoding, String location,
            String username, String password) throws SerializationError, IOException, HttpException
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

}
