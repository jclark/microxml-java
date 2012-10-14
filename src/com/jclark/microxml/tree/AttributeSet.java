package com.jclark.microxml.tree;

import java.util.Set;

/**
 * A set of Attributes.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface AttributeSet extends Set<Attribute> {
    /**
     * Returns the Attribute with the specified name, or null if there is no such attribute
     * @param name the name of the Attribute to be returned
     * @return the Attribute with the specified name, or null if there is no such attribute
     */
    Attribute get(String name);

    /**
     * Returns the value of the Attribute with the specified name, or null if there is no such attribute.
     * @param name the name of the Attribute
     * @return a String giving the value of the Attribute, or null if there is no such attribute
     */
    String getValue(String name);

    /**
     * Removes the Attribute with the specified name, if there is one.
     * @param name the name of the Attribute to be removed
     * @return the Attribute that was removed, or null if there is no Attribute with the specified name
     */
    Attribute remove(String name);

    /**
     * Clones the AttributeSet.
     * @return an AttributeSet that is equal to this Attribute but shares no structure
     */
    AttributeSet clone();

    /**
     * Adds an Attribute, provided there is not already an Attribute with the same name.
     * @param attribute the attribute to be added; must not be null
     * @return true if the attribute was added
     */
    boolean add(Attribute attribute);
}
