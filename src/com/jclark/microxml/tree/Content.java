package com.jclark.microxml.tree;

/**
 * A ordered list of Elements interspersed with possibly empty chunks of text.
 * For a list of n Elements, there are exactly n + 1 text chunks.
 * The Element with index i is preceded by the text chunk with index i,
 * and followed by the text chunk with index i + 1. Text chunks may be empty.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface Content extends Iterable<Element> {
    boolean isEmpty();

    /**
     * Returns the number of Elements.
     * @return
     */
    int size();

    /**
     * Adds an Element on to the end of the list.
     * The number of Elements is increased by 1.
     * The element to be added must not have a parent.
     *
     * @param element the element to be added; must not be null
     */
    void add(Element element);

    /**
     * Returns the Element at a specified position.
     * @param index the position of the Element to be returned
     * @return the element at the position; never null
     * @throws IndexOutOfBoundsException if the index is < 0 or >= the number of Elements
     */
    Element get(int index);

    /**
     * Removes all elements and makes all text chunks empty.
     */
    void clear();
    Element set(int index, Element element);
    void add(int index, Element element);
    void ensureCapacity(int minCapacity);
    void trimToSize();

    /**
     * Removes the element at a specified position.
     * Detaches the removed element from its parent.
     * @param index
     * @return the element that was removed
     */
    Element remove(int index);
    void removeRange(int fromIndex, int toIndex);

    /**
     * Returns the text chunk at a specified position.
     *
     * @param index the position of the text to be returned
     * @return the text at index; this may be empty but is never null
     */
    String getText(int index);

    void add(String text);

    /**
     * Sets the text chunk at a specified position.
     * @param index the position
     * @param text the new text chunk for the position; this may be empty, but must not be null
     */
    void setText(int index, String text);

    /**
     * Makes all text chunks empty. The Elements are left unchanged.
     */
    void clearText();

    /**
     * Returns the index of the first text chunk that is not all whitespace, or -1 if there is no such text chunk.
     */
    int indexOfNonWhitespaceText();
}
