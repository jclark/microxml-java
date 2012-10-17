package com.jclark.microxml.tree;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler that builds an Element.
 * This implementation does store Location information in the Element.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
class BuildHandler extends DefaultHandler {
    protected Element root = null;
    protected Element currentElement = null;

    Element getRoot() {
        return root;
    }

    protected Element createElement(String qName) {
        return new Element(qName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        Element elem = createElement(qName);
        if (currentElement == null)
            root = elem;
        else
            currentElement.add(elem);
        currentElement = elem;
        // Avoid creating the AttributeSet object unnecessarily.
        if (elem.hasAttributes()) {
            AttributeSet atts = elem.attributes();
            int length = attributes.getLength();
            for (int i = 0; i < length; i++)
                atts.add(new Attribute(attributes.getQName(i), attributes.getValue(i)));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        currentElement = currentElement.getParent();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        currentElement.add(new String(ch, start, length));
    }
}
