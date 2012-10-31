package com.jclark.microxml.tree;

/**
 * Every open has to have a matching close.
 * A startTagOpen can be closed by either a startTagClose or an emptyElementClose.
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
interface TokenHandler<E extends Throwable> {
    void startTagOpen(int position, String name) throws E;
    void attributeOpen(int namePosition, int valuePosition, String name) throws E;
    void attributeClose() throws E;
    // position is of '>'
    void startTagClose(int position) throws E;
    // position is of '/>'
    void emptyElementTagClose(int position) throws E;
    void endTag(int startPosition, int endPosition, String name) throws E;
    void literalChars(int position, char[] chars, int offset, int count) throws E;
    void charRef(int position, int refLength, char[] chars) throws E;
    void end() throws E;
    void error(int startPosition, int endPosition, String message) throws E;
    void fatal(int startPosition, int endPosition, String message) throws E;
}
