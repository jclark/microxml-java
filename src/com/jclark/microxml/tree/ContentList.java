package com.jclark.microxml.tree;

import java.util.Iterator;

/**
 * A ordered list of Elements interspersed with characters.
 *
 * There may be characters in between elements, before the first Element and after the last Element.
 * The list may contain characters even if it contains no Elements.
 * Each Element in the list has a position between 0 and n - 1, where n is the number of Elements in the list.
 * The characters in the list are accessed as text chunks, based on their position relative to Elements.
 * For a list containing n Elements, there are exactly n + 1 text chunks, each of which may be empty;
 * each text chunk has a position between 0 and n.
 * The text chunk with position i consists of the characters between the Element with position i - 1 and
 * the Element with position i.
 * Thus the Element at position i is preceded by the text chunk with position i,
 * and followed by the text chunk with position i + 1.
 * A ContentList with no Elements has a single text chunk with position 0.
 * A ContentList object is associated permanently with a particular Element.
 * A ContentList can be used to represent the result of parsing the content of a MicroXML element;
 * the content of a MicroXML element consists of the characters between the start-tag and the end-tag.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public interface ContentList extends Iterable<Element> {
    /**
     * Returns the Element with which this ContentList is associated.
     * @return the Element with which this ContentList is associated; never null
     */
    Element getOwner();

    /**
     * Returns true if this ContentList contains no Elements and no characters.
     * @return  true if this ContentList has no Elements and no characters.
     */
    boolean isEmpty();

    /**
     * Returns the number of Elements in this ContentList.
     * @return the number of Elements
     */
    int size();

    /**
     * Adds an Element on to the end of the list.
     * The number of Elements is increased by 1.
     * The element to be added must not have a parent.
     *
     * @param element the element to be added; must not be null
     * @throws NullPointerException if element is null
     * @throws IllegalArgumentException if element already has a parent
     */
    void add(Element element);

    /**
     * Returns the Element at a specified position.
     *
     * @param index the position of the Element to be returned
     * @return the element at the position; never null
     * @throws IndexOutOfBoundsException if index is not a valid position
     */
    Element get(int index);

    /**
     * Removes all elements and characters.
     * This makes all text chunks empty.
     */
    void clear();

    /**
     * Replaces the Element at a specified position.
     * @param index the position for the Element to be replaced
     * @param element the replacement Element; must not be null
     * @return the Element previously at the position, or null if none
     * @throws IllegalArgumentException if element already has a parent
     * @throws NullPointerException if element is null
     */
    Element set(int index, Element element);

    /**
     * Inserts an Element at a specified position.
     * All Elements with position >= index will have their position increased by 1.
     * @param index the position for the Element to be inserted
     * @param element the Element to be inserted; must not be null
     * @throws IllegalArgumentException if element already has a parent
     * @throws NullPointerException if element is null
     */
    void add(int index, Element element);
    void ensureCapacity(int minCapacity);
    void trimToSize();

    /**
     * Removes the Element at a specified position.
     * The removed Element is detached from its parent.
     * No characters are removed;
     * thus the text chunks immediately before and after the element are joined.
     * @param index the position of the Element to be removed
     * @return the Element that was removed
     */
    Element remove(int index);
    void removeRange(int fromIndex, int toIndex);

    /**
     * Returns the text chunk at a specified position.
     *
     * @param index the position of the text chunk to be returned
     * @return the text chunk at position index; this may be empty but is never null
     */
    String getText(int index);

    void add(String text);

    /**
     * Changes the text chunk at a specified position.
     * @param index the position
     * @param text the new text chunk for the position; this may be empty, but must not be null
     */
    void setText(int index, String text);

    /**
     * Removes all characters.
     * This makes all text chunks empty. The Elements are left unchanged.
     */
    void clearText();

    /**
     * Returns the position of the first text chunk that contains a character that is not a whitespace character.
     * A whitespace character is one of tab, space, line-feed and carriage return.
     * @return the position of the first text chunk that is not all whitespace; -1 is all characters are whitespace
     */
    int indexOfNonWhitespaceText();

    /**
     * Returns a new Iterator that iterates over the Elements in this ContentList.
     * @return an Iterator for the Elements in the list
     */
    Iterator<Element> iterator();
}
