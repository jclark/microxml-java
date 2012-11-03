package com.jclark.microxml.tree;

import com.sun.servicetag.SystemEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class MicroXMLTest {
    static public void main(String[] args) throws IOException, ParseException {
        Element element = MicroXML.parse(loadFile(args[0]), new ParseOptions(new ErrorPrinter()));
        MicroXML.canonicalize(element, new File(args[1]));
    }

    static class ErrorPrinter implements ErrorHandler {
        private PrintStream err;
        ErrorPrinter() {
            err = System.err;
        }

        ErrorPrinter(PrintStream err) {
            this.err = err;
        }

        public void error(Location location, String message) throws ParseException {
            LinePosition lp = location.getStartLinePosition();
            err.printf("%d:%d: %s\n", lp.getLineNumber(), lp.getColumnNumber(), message);
        }
    }

    static String loadFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream f = new FileInputStream(file);
        long length = file.length();
        if (length == 0)
            return "";
        if (length > Integer.MAX_VALUE)
            throw new IOException("file too big");
        byte[] buffer = new byte[(int)length];
        if (f.read(buffer, 0, buffer.length) != buffer.length)
            throw new IOException("read length");
        return new String(buffer, 0, buffer.length, "UTF-8");
    }
}
