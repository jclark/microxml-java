package com.jclark.microxml.tree;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class for serializing MicroXML.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class SerializeUtil {
    private SerializeUtil() {}

    static public void serialize(Element element, Appendable a) throws IOException {
        a.append('<').append(element.getName());
        if (element.hasAttributes()) {
            for (Attribute attr : element.attributes())
                serialize(attr, a);
        }
        if (element.hasContent()) {
            a.append('>');
            boolean hasText = element.hasText();
            if (hasText)
                serialize(element.getText(0), a);
            if (element.hasChildren()) {
                for (Element child : element.children()) {
                    serialize(child, a);
                    if (hasText)
                        serialize(child.getTextAfter(), a);
                }
            }
            a.append('<').append('/').append(element.getName()).append('>');
        }
        else
            a.append('/').append('>');
    }

    static public void canonicalize(Element element, Appendable a, Attribute[] attributeBuffer) throws IOException {
        a.append('<').append(element.getName());
        if (element.hasAttributes()) {
            int size = element.attributes().size();
            Attribute[] attrs = element.attributes().toArray(attributeBuffer);
            if (size > 1)
                Arrays.sort(attrs, 0, size,
                            new Comparator<Attribute>() {
                                public int compare(Attribute a1, Attribute a2) {
                                    // TODO: wrong result for non-BMP characters
                                    return a1.getName().compareTo(a2.getName());
                                }
                            });
            for (int i = 0; i < size; i++)
                serialize(attrs[i], a);
        }
        a.append('>');
        boolean hasText = element.hasText();
        if (hasText)
            serialize(element.getText(0), a);
        if (element.hasChildren()) {
            for (Element child : element.children()) {
                canonicalize(child, a, attributeBuffer);
                if (hasText)
                    serialize(child.getTextAfter(), a);
            }
        }
        a.append('<').append('/').append(element.getName()).append('>');
    }

    static public void serialize(String text, Appendable a) throws IOException {
        int length = text.length();
        int start = 0;
        for (int i = 0; i < length; i++) {
            String ref = null;
            switch (text.charAt(i)) {
            case '<':
                ref = "&lt;";
                break;
            case '>':
                ref = "&gt;";
                break;
            case '&':
                ref = "&amp;";
                break;
            }
            if (ref != null) {
                if (start < i)
                    a.append(text.substring(start, i));
                a.append(ref);
                start = i + 1;
            }
        }
        if (start < length)
            a.append(start == 0 ? text : text.substring(start));
    }

    static public void serialize(Attribute attr, Appendable a) throws IOException {
        a.append(' ').append(attr.getName()).append('=').append('"');
        String value = attr.getValue();
        int length = value.length();
        int start = 0;
        for (int i = 0; i < length; i++) {
            String ref = null;
            switch (value.charAt(i)) {
            case '<':
                ref = "&lt;";
                break;
            case '>':
                ref = "&gt;";
                break;
            case '&':
                ref = "&amp;";
                break;
            case '"':
                ref = "&quot;";
                break;
            }
            if (ref != null) {
                if (start < i)
                    a.append(value.substring(start, i));
                a.append(ref);
                start = i + 1;
            }
        }
        if (start < length)
            a.append(start == 0 ? value : value.substring(start));
        a.append('"');
    }
}
