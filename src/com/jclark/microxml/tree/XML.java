package com.jclark.microxml.tree;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * Provides functions for parsing XML into Elements.
 *
 * @see Element
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class XML {
    // prevent instantiation
    private XML() {}

    static private class ParserFactory {
        static private SAXParserFactory factory = getFactory();
        static private SAXParserFactory getFactory() {
            return SAXParserFactory.newInstance();
        }
        static private SAXParser newParser() {
            try {
                return factory.newSAXParser();
            }
            catch (ParserConfigurationException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            catch (SAXException e) {
                tryUnwrapRuntimeException(e);
                Exception wrapped = e.getException();
                if (wrapped != null)
                    throw new RuntimeException(wrapped);
                else
                    throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * Parses an XML File into an Element.
     * No namespace processing is performed.
     *
     * @param f the File to parse
     * @return an Element
     * @throws IOException if an I/O error occurs
     * @throws ParseException if the file is not well-formed XML
     */
    static public Element parse(File f) throws IOException, ParseException {
        BuildHandler builder = new BuildHandler();
        try {
            ParserFactory.newParser().parse(f, builder);
        }
        catch (SAXException e) {
            throwSAXException(e);
        }
        return builder.getRoot();
    }

    static void throwSAXException(SAXException e) throws IOException, ParseException {
        if (e instanceof SAXParseException)
            throw new ParseException(e.getMessage(), toLocation((SAXParseException)e));
        tryUnwrapRuntimeException(e);
        throw new ParseException(e.getMessage(), e.getException());
    }

    static private Location toLocation(final SAXParseException e) {
         return new AbstractLocation() {
             @Override
             public String getURL() {
                 return e.getSystemId();
             }

             @Override
             public int getLineNumber() {
                 return e.getLineNumber();
             }

             @Override
             public int getColumnNumber() {
                 return e.getColumnNumber();
             }
         };
     }

    static private void tryUnwrapRuntimeException(SAXException e) {
        Exception wrapped = e.getException();
        if (wrapped instanceof RuntimeException)
            throw (RuntimeException)wrapped;
    }

}
