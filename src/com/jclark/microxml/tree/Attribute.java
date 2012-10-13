package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;

/**
 * An attribute, representing a name-value pair.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Attribute {
    @NotNull
    private final String name;
    @NotNull
    private String value;

    public Attribute(@NotNull String name, @NotNull String value) {
        Element.checkNotNull(name);
        Element.checkNotNull(value);
        this.name = name;
        this.value = value;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getValue() {
        return value;
    }

    public void setValue(@NotNull String value) {
        Element.checkNotNull(value);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Attribute att = (Attribute)obj;
        return name.equals(att.name) && value.equals(att.value);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + value.hashCode();
    }
}
