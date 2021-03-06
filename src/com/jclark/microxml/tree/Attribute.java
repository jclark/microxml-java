package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;

/**
 * An attribute, representing a name-value pair.
 *
 * Two attributes are equal if their names and values are equal.
 *
 * This class does not provide location information.  A parser that is able to provide
 * location information for attributes should subclass this class.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Attribute implements Cloneable {
    @NotNull
    private final String name;
    @NotNull
    private String value;

    public Attribute(@NotNull String name, @NotNull String value) {
        Util.requireNonNull(name);
        Util.requireNonNull(value);
        this.name = name;
        this.value = value;
    }

    @NotNull
    public final String getName() {
        return name;
    }

    @NotNull
    public final String getValue() {
        return value;
    }

    public final void setValue(@NotNull String value) {
        Util.requireNonNull(value);
        this.value = value;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof Attribute))
            return false;
        Attribute att = (Attribute)obj;
        return name.equals(att.name) && value.equals(att.value);
    }

    @Override
    public final int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }

    @Override
    public Attribute clone() {
        try {
            return (Attribute)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Returns the Location of the name of this Attribute.
     *
     * @return the Location of this Attribute; null if not available
     */
    Location getNameLocation() {
        return null;
    }

    /**
     * Return the Location of a range of characters in the value of this Attribute.
     *
     * @param beginIndex the index of the first character of the range
     * @param endIndex   the index after the last character of the range
     * @return the Location for the specified range; null if no Location is available
     * @throws IndexOutOfBoundsException if {@code beginIndex < 0} or {@code beginIndex > endIndex} or {@code endIndex >
     *                                   getValue().length}
     */
    Location getValueLocation(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > value.length())
            throw new IndexOutOfBoundsException();
        return null;
    }
}
