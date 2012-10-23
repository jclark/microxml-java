package com.jclark.microxml.tree;

/**
 * A Location that provides no information.
 * This is intended for use as a superclass for implementations of Location.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class AbstractLocation implements Location {
    public String getURL() {
        return null;
    }

    public long getStartIndex() {
        return -1L;
    }

    public long getEndIndex() {
        return -1L;
    }

    public LinePosition getStartLinePosition() {
        return LinePosition.VOID;
    }

    public LinePosition getEndLinePosition() {
        return LinePosition.VOID;
    }
}
