package com.jclark.microxml.tree;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class BuildTest  {

    static public void main(String[] args) throws IOException {
        try {
            Element element = XML.parse(new File(args[0]), new XML.ParseOption[] { XML.ParseOption.STORE_LOCATIONS });
            element.selfCheck();
            MicroXML.serialize(element, new File(args[1]));
        }
        catch (ParseException e) {
            printLocation(e.getLocation(), System.err);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    static public void printLocation(Location loc, PrintStream err) {
        if (loc == null)
            return;
        String url = loc.getURL();
        if (url != null) {
            err.print(url);
            err.print(':');
        }
        LinePosition lp = loc.getStartLinePosition();
        int lineNumber = lp.getLineNumber();
        if (lineNumber >= 1) {
            err.print(lineNumber);
            err.print(':');
            int columnNumber = lp.getColumnNumber();
            if (columnNumber >= 0) {
                System.err.print(columnNumber);
                System.err.print(':');
            }
        }
        if (url != null || lineNumber >= 1)
            err.print(' ');
    }
}
