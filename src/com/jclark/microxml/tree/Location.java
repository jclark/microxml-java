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
    long getStartIndex();

    /**
     * Return the index of the end of the range.
     * The index is 0-based and measured in Java chars, which are equivalent to 16-bit UTF-16 code-units.
     * @return the index in chars of the start of the range; -1 if not available
     */
    long getEndIndex();

    /**
     * Return the line and column number of the start of the range.
     * @return a LinePosition giving the line and column number of the start of the range; never null
     */
    LinePosition getStartLinePosition();

    /**
     * Returns the line and column number of the end of the range.
     * @return a LinePosition giving the line and column number of the start of the range; never null
     */
    LinePosition getEndLinePosition();
}
