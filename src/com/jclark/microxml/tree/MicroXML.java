package com.jclark.microxml.tree;

import java.io.IOException;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class MicroXML {
    private MicroXML() {}

    static public void serialize(Element element, Appendable appendable) throws IOException {
        SerializeUtil.serialize(element, appendable);
        appendable.append('\n');
    }

    static public void canonicalize(Element element, Appendable appendable) throws IOException {
        SerializeUtil.canonicalize(element, appendable, new Attribute[16]);
        appendable.append('\n');
    }

}
