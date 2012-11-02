package com.jclark.microxml.tree;

/**
 * @author James Clark
 */
public interface ErrorHandler {
    void error(Location location, String message) throws ParseException;
}
