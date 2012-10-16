package com.jclark.microxml.tree;

/**
 * The location of a range of characters within a textual resource.
 * Implementations of this should be immutable.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 * @see java.net.URI
 */
public interface Location {
    /**
     * Returns the URL of the resource.
     * The term URL is used loosely: the returned string may be a URI or
     * an IRI or a Legacy Extended IRI.
     * This URL is intended to be human-readable, so non-ASCII characters should
     * be left as is, rather than percent-encoded.
     * The URL should be absolute rather than relative and should not have
     * a fragment identifier.
     * @return a String with the URL of the resource; null if not available
     */
    String getURL();

    /**
     * Return the index of the start of the range.
     * The index is 0-based and measured in Java chars, which are equivalent to 16-bit UTF-16 code-units.
     * @return the index in chars of the start of the range; -1 if not available
     */
    long getIndex();

    /**
     * Returns the length of the range.
     * The length is measured in Java chars, which are equivalent to 16-it code-units.
     * If the length is greater than Integer.MAX_VALUE, then returns Integer.MAX_VALUE.
     * The length may be zero.
     * @return the length of the range; -1 if not available
     */
    int getLength();

    /**
     * Returns the line number of the start of the range.
     * The first line-number is 1.
     * If the line number is greater than Integer.MAX_VALUE, then returns Integer.MAX_VALUE.
     * Lines are considered to be delimited by \n, \r, or \r\n only.
     * @return the line number of the start of the range; -1 if not available
     */
    int getLineNumber();

    /**
     * Returns the column number of the start of the range.
     * The column number of a character is one plus the number of Java chars (16-bit UTF-16 code units)
     * preceding it on the line.
     * This will not always correspond to the column number in a text editor.
     * @return the column number of the start of the range; -1 if not available
     */
    int getColumnNumber();
}
