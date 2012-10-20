package com.jclark.microxml.tree;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class DuplicateAttributeException extends IllegalArgumentException {
    public DuplicateAttributeException(String name) {
        super(name);
    }
}
