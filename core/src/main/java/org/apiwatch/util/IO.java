package org.apiwatch.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;

public class IO {

    public static String readToString(InputStream in) throws IOException {
        return readToString(new InputStreamReader(in));
    }
    
    public static String readToString(InputStream in, String charset) throws IOException {
        return readToString(new InputStreamReader(in, charset));
    }
    
    public static String readToString(Reader reader) throws IOException {
        int n;
        char[] buffer = new char[4096];
        StringWriter writer = new StringWriter();
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        return writer.toString();
    }
    
    
    public static void writeString(OutputStream out, String s) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(out);
        w.write(s);
        w.flush();
        w.close();
    }
    
    public static void writeString(OutputStream out, String s, String encoding) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(out, encoding);
        w.write(s);
        w.flush();
        w.close();
    }
    
    
}
