package com.jclark.microxml.tree;

import java.util.Set;

/**
 * A set of Attributes. An AttributeSet preserves the invariant that it never contains
 * two Attributes with the same name.
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
     * Clones this AttributeSet.
     * The returned AttributeSet will be equal to this AttributeSet but independent of it:
     * modifications to one AttributeSet will not affect the other.
     * @return an AttributeSet that is equal to this AttributeSet but shares no structure
     */
    AttributeSet clone();

    /**
     * Adds an Attribute to this AttributeSet. If this AttributeSet already contains an Attribute with the same name and
     * same value, then the Attribute is not added; if the AttributeSet already contains an Attribute with the same name
     * but a different value, then a DuplicateAttributeException is thrown.
     *
     * @param attribute the Attribute to be added; must not be null
     * @return true if the Attribute was added; false otherwise
     * @throws NullPointerException        if attribute is null
     * @throws DuplicateAttributeException if this Attribute contains an Attribute with the same name and a different
     *                                     value
     */
    boolean add(Attribute attribute);
}
