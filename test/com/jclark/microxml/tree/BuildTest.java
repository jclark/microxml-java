package com.jclark.microxml.tree;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class BuildTest extends DefaultHandler {

    Element currentElement = null;

    static public void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(false);
        SAXParser parser = parserFactory.newSAXParser();
        BuildTest builder = new BuildTest();
        parser.parse(new File(args[0]), builder);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Element elem = new Element(qName);
        if (currentElement != null)
            currentElement.content().add(elem);
        currentElement = elem;
        AttributeSet atts = elem.attributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++)
            atts.add(new Attribute(attributes.getQName(i), attributes.getValue(i)));
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        currentElement = currentElement.getParent();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentElement.content().add(new String(ch, start, length));
    }
}
