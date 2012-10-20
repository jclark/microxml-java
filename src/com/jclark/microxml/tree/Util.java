package com.jclark.microxml.tree;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
class Util {
    static void requireNonNull(Object obj) {
        if (obj == null)
            throw new NullPointerException();
    }
}
