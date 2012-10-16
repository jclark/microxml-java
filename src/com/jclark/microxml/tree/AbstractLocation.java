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

    public long getIndex() {
        return -1L;
    }

    public int getLength() {
        return -1;
    }

    public int getLineNumber() {
        return -1;
    }

    public int getColumnNumber() {
        return -1;
    }
}
