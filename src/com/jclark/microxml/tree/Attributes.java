package com.jclark.microxml.tree;

import java.util.Set;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Attributes extends Set<Attribute> {
    Attribute get(String name);
    String getValue(String name);
    Attribute remove(String name);
}
