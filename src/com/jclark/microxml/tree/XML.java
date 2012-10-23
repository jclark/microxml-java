package com.jclark.microxml.tree;

import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * Provides functions for parsing XML into {@link Element}s.
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

    public enum ParseOption {
        STORE_LOCATIONS
    }

    /**
     * Parses an XML File into an Element.
     * No namespace processing is performed.
     *
     * @param f the File to parse
     * @param options an array of ParseOptions to control parsing
     * @return an Element
     * @throws IOException if an I/O error occurs
     * @throws ParseException if the file is not well-formed XML
     */
    static public Element parse(File f, ParseOption[] options) throws IOException, ParseException {
        boolean storeLocations = false;
        for (ParseOption option : options)
            if (ParseOption.STORE_LOCATIONS.equals(option))
                storeLocations = true;
        BuildHandler builder = storeLocations ? new LocationBuildHandler() : new BuildHandler();
        SAXParser parser = ParserFactory.newParser();
        try {
            if (storeLocations) {
                try {
                    parser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", builder);
                }
                catch (SAXException ignored) {}
            }
            parser.parse(f, builder);
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
             public LinePosition getStartLinePosition() {
                 return new LinePosition(e.getLineNumber(), e.getColumnNumber());
             }

             @Override
             public LinePosition getEndLinePosition() {
                 return getStartLinePosition();
             }
         };
     }

    static private void tryUnwrapRuntimeException(SAXException e) {
        Exception wrapped = e.getException();
        if (wrapped instanceof RuntimeException)
            throw (RuntimeException)wrapped;
    }

}
