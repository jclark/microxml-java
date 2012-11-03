package com.jclark.microxml.tree;

/**
 * Interface between frontend and backend of MicroXML parsing.
 * Frontend is {@link Tokenizer}; backend is {@link TreeBuilder}.
 * No guarantee is made that every startTag has a matching endTag,
 * nor about where text occurs.
 * Every call to a *open method is guaranteed to have a call to a matching *close method:
 * a startTagOpen can be closed by either a startTagClose or an emptyElementClose.
 *
 * @author James Clark
 */
interface TokenHandler<E extends Throwable> {
    void startTagOpen(int position, String name) throws E;
    void attributeOpen(int namePosition, int valuePosition, String name) throws E;
    void attributeClose() throws E;
    // position is following th '>'
    void startTagClose(int position) throws E;
    // position is following the '/>'
    void emptyElementTagClose(int position) throws E;
    void endTag(int startPosition, int endPosition, String name) throws E;
    void literalChars(int position, char[] chars, int offset, int count) throws E;
    void charRef(int position, int refLength, char[] chars) throws E;
    void crLf(int position) throws E;
    void end(int position) throws E;
    void error(int startPosition, int endPosition, ParseError error, Object... args) throws E;
}
