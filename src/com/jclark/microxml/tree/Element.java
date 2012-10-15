package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * An element having a name, attributes and content.
 * <p/>
 * The content is an ordered list of Elements interspersed with characters. The Elements in the content are the children
 * of this Element. There may be characters in between elements, before the first Element and after the last Element.
 * The list may contain characters even if it contains no Elements. Each Element in the list has a position between 0
 * and n - 1, where n is the number of Elements in the list. The characters in the list are accessed as text chunks,
 * based on their position relative to Elements. For a list containing n Elements, there are exactly n + 1 text chunks,
 * each of which may be empty; each text chunk has a position between 0 and n. The text chunk with position i consists
 * of the characters between the Element with position i - 1 and the Element with position i. Thus the Element at
 * position i is preceded by the text chunk with position i, and followed by the text chunk with position i + 1. An
 * Element with no child Elements has a single text chunk with position 0.
 *
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */

// TODO: compare document order
// TODO: trimToSize
// TODO: efficient test for empty text
public class Element implements Iterable<Element> {
    @NotNull
    private String name;
    @Nullable
    private Element parent;
    private int indexInParent;
    @Nullable
    private AttributeSet attributeSet;

    // The number of child elements.
    private int numChildElements;

    // An array containing the child elements.
    // If numChildElements is 0, maybe null.
    private Element[] childElements;

    // An array containing the text chunks.
    // If non-null, then
    // + length equal to elements.length + 1
    // + for 0 <= i <= numChildElements, textChunks[i] != null
    // If null, means all text chunks are empty.
    @Nullable
    private String[] textChunks;

    // Used to provide fail-fast behaviour for iterators.
    private int modCount = 0;

    /**
     * Creates an Element with a given name.
     * The attributes and content of the element are empty.
     * @param name the name of the Element
     */
    public Element(@NotNull String name) {
        this(name, null);
    }

    /**
     * Creates an Element with a given name and attributes.
     * The content of the Element is empty.
     * @param name the name of the Element to be created
     * @param attributeSet the attributes of the Element to be created; may be null
     */
    public Element(@NotNull String name, @Nullable AttributeSet attributeSet) {
        checkNotNull(name);
        this.name = name;
        this.attributeSet = attributeSet;
        parent = null;
        indexInParent = -1;
        numChildElements = 0;
        childElements = null;
        textChunks = null;
    }

    public Element(@NotNull Element element) {
        checkNotNull(element);
        name = element.name;
        parent = null;
        indexInParent = -1;
        attributeSet = element.attributeSet == null ? null : element.attributeSet.clone();
        numChildElements = element.numChildElements;
        textChunks = element.textChunks == null ? null : Arrays.copyOf(element.textChunks, numChildElements + 1);
        childElements = numChildElements == 0 ? null : new Element[numChildElements];
        for (int i = 0; i < numChildElements; i++) {
            Element e = new Element(element.childElements[i]);
            attach(e, i);
            childElements[i] = e;
        }
    }

    /**
     * Returns the name of this Element.
     * @return the name of this Element; never null
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Element.
     * @param name the name of this Element; must not be null
     */
    public void setName(@NotNull String name) {
        checkNotNull(name);
        this.name = name;
    }

    /**
     * Returns the attributes of this Element.
     * @return an AttributeSet representing the attributes of this Element
     */
    @NotNull
    public AttributeSet attributes() {
        if (attributeSet == null)
            attributeSet = new AttributeSetImpl();
        return attributeSet;
    }

    /**
     * Returns true if this Element has one or more Attributes.
     * This is a shorthand for <code>!attributes().isEmpty()</code>.
     * Using <code>hasAttributes()</code> may allow the implementation to avoid creating an AttributeSet object
     * for Elements that have no attributes.
     * @return true if this Element has one or more Attributes
     */
    public boolean hasAttributes() {
        return attributeSet != null && !attributeSet.isEmpty();
    }

    /**
     * Returns the parent Element, or null if this Element has no parent.
     * The parent of an element is set when it is added to the ContentListImpl of another element.
     * @return the parent Element of this Element, if there is one; null otherwise
     */
    @Nullable
    public Element getParent() {
        return parent;
    }

    /**
     * Returns the index of this Element in its parent, or -1 if this Element has no parent.
     * @return the index of this Element in its parent, or -1 if this Element has no parent
     */
    public int getIndexInParent() {
        return indexInParent;
    }

    private void detach() {
        parent = null;
        indexInParent = -1;
    }

    /**
     * Attach an Element as a child of this Element.
     * @param child the element to be attached
     * @param indexInParent the index of the child in this Element
     */
    private void attach(Element child, int indexInParent) {
        if (child.parent != null)
            throw new IllegalArgumentException("Element already has a parent");
        child.parent = this;
        child.indexInParent = indexInParent;
    }

    static void checkNotNull(Object obj) {
        if (obj == null)
            throw new NullPointerException();
    }

    /**
     * Returns true if the content of this Element is empty.
     * The content is empty if it has neither characters nor child elements.
     * @return  true if the content of this Element is empty.
     */
    public boolean isEmpty() {
        return numChildElements == 0 && (textChunks == null || textChunks[0].isEmpty());
    }

    /**
     * Returns the number of child Elements.
     * @return the number of child Elements
     */
    public int elementCount() {
        return numChildElements;
    }

    /**
     * Adds an Element on to the end of the list.
     * The number of Elements is increased by 1.
     * The element to be added must not have a parent.
     *
     * @param element the element to be added; must not be null
     * @throws NullPointerException if element is null
     * @throws IllegalArgumentException if element already has a parent
     */
    public void add(@NotNull Element element) {
        modCount++;
        ensureCapacity(numChildElements + 1);
        attach(element, numChildElements);
        childElements[numChildElements++] = element;
        if (textChunks != null)
            textChunks[numChildElements] = "";
    }

    public void ensureCapacity(int minCapacity) {
        int newLength;
        if (childElements == null)
            newLength = 8;
        else if (childElements.length >= minCapacity)
            return;
        else
            newLength = childElements.length * 2;
        if (newLength < minCapacity) {
            if (minCapacity == Integer.MAX_VALUE)
                throw new IllegalArgumentException();
            newLength = minCapacity;
        }
        if (childElements == null)
            childElements = new Element[newLength];
        else
            childElements = Arrays.copyOf(childElements, newLength);
        if (textChunks != null)
            textChunks = Arrays.copyOf(textChunks, newLength + 1);
    }

    @NotNull
    private String[] allocateTextChunks() {
        String[] v = new String[childElements == null ? 1 : childElements.length + 1];
        Arrays.fill(v, 0, numChildElements + 1, "");
        return v;
    }

    /**
     * Adds characters at the end of the content of this Element.
     * @param text a String with the characters to add; must not be empty
     */
    public void add(@NotNull String text) {
        if (!text.isEmpty()) {
            if (textChunks == null)
                textChunks = allocateTextChunks();
            textChunks[numChildElements] += text;
        }
    }

    public class ContentIterator implements Iterator<Element> {
        private int nextIndex = 0;
        private int lastReturned = -1;
        private int expectedModCount = modCount;

        public boolean hasNext() {
            checkModCount();
            return nextIndex < numChildElements;
        }

        /**
         * Returns the text before the element to be returned by next().
         * @return the text chunk before the element to be returned by next()
         */
        public String getText() {
            return Element.this.getText(nextIndex);
        }

        public Element next() {
            checkModCount();
            return childElements[lastReturned = nextIndex++];
        }

        public void remove() {
            if (lastReturned == -1)
                throw new IllegalStateException();
            checkModCount();
            Element.this.remove(lastReturned);
            --nextIndex;
            expectedModCount = modCount;
            lastReturned = -1;
        }

        private void checkModCount() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * Returns a new Iterator that iterates over the Elements in this ContentList.
     * @return an Iterator for the Elements in the list
     */
    public Iterator<Element> iterator() {
        return new ContentIterator();
    }

    /**
     * Returns the child Element at a specified position.
     *
     * @param index the position of the child Element to be returned
     * @return the child Element at the position; never null
     * @throws IndexOutOfBoundsException if index is not a valid position
     */
    @NotNull
    public Element get(int index) {
        checkElementIndex(index);
        return childElements[index];
    }

    /**
     * Returns the text chunk at a specified position.
     *
     * @param index the position of the text chunk to be returned
     * @return the text chunk at position index; this may be empty but is never null
     */
    @NotNull
    public String getText(int index) {
        checkTextIndex(index);
        if (textChunks == null) {
            if (index < 0)
                throw new IndexOutOfBoundsException();
            return "";
        }
        return textChunks[index];
    }

    /**
     * Removes all content.
     */
    public void clear() {
        modCount++;
        for (int i = 0; i < numChildElements; i++)
            childElements[i].detach();
        childElements = null;
        textChunks = null;
        numChildElements = 0;
    }

    /**
     * Removes all text content.
     * This makes all text chunks empty. The Elements are left unchanged.
     */
    public void clearText() {
        textChunks = null;
    }

    /**
     * Returns the position of the first text chunk that contains a character that is not a whitespace character.
     * A whitespace character is one of tab, space, line-feed and carriage return.
     * @return the position of the first text chunk that is not all whitespace; -1 is all characters are whitespace
     */
    public int indexOfNonWhitespaceText() {
        if (textChunks != null) {
            for (int i = 0; i <= numChildElements; i++) {
                String str = textChunks[i];
                if (!str.isEmpty()) {
                    for (int j = 0, n = str.length(); j < n; j++) {
                        char c = str.charAt(i);
                        if (c > ' ')
                            return i;
                        switch (c) {
                        case ' ':
                        case '\r':
                        case '\t':
                        case '\n':
                            break;
                        default:
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Replaces the Element at a specified position.
     * @param index the position for the Element to be replaced
     * @param element the replacement Element; must not be null
     * @return the Element previously at the position, or null if none
     * @throws IllegalArgumentException if element already has a parent
     * @throws NullPointerException if element is null
     */
    public Element set(int index, @NotNull Element element) {
        checkElementIndex(index);
        checkNotNull(element);
        Element old = childElements[index];
        // Detach the old element first, in case we are replacing it by itself.
        old.detach();
        attach(element, index);
        childElements[index] = element;
        return old;
    }

    /**
     * Inserts an Element at a specified position.
     * All Elements with position >= index will have their position increased by 1.
     * @param index the position for the Element to be inserted
     * @param element the Element to be inserted; must not be null
     * @throws IllegalArgumentException if element already has a parent
     * @throws NullPointerException if element is null
     */
    public void add(int index, @NotNull Element element) {
        if (index == numChildElements) {
            add(element);
            return;
        }
        checkNotNull(element);
        checkElementIndex(index);
        modCount++;
        ensureCapacity(numChildElements + 1);
        for (int i = numChildElements; i > index; --i) {
            Element e = childElements[i - 1];
            childElements[i] = e;
            e.indexInParent = i;
        }
        childElements[index] = element;
        if (textChunks != null) {
            System.arraycopy(textChunks, index, textChunks, index + 1, numChildElements + 1 - index);
            textChunks[index] = "";
        }
    }

    /**
     * Changes the text chunk at a specified position.
     * @param index the position
     * @param str the new text chunk for the position; this may be empty, but must not be null
     */
    public void setText(int index, @NotNull String str) {
        checkTextIndex(index);
        if (textChunks == null) {
            if (str.isEmpty()) {
                if (index < 0)
                    throw new IndexOutOfBoundsException();
                return;
            }
            textChunks = allocateTextChunks();
        }
        else
            checkNotNull(str);
        textChunks[index] = str;
    }

    /**
     * Removes the child Element at a specified position.
     * The removed Element is detached from its parent.
     * No characters are removed;
     * thus the text chunks immediately before and after the element are joined.
     * @param index the position of the Element to be removed
     * @return the Element that was removed
     */
    public Element remove(int index) {
        checkElementIndex(index);
        ++modCount;
        Element old = childElements[index];
        old.detach();
        for (int i = index; i < numChildElements - 1; i++) {
            Element e = childElements[i + 1];
            e.indexInParent = i;
            childElements[i] = e;
        }
        childElements[numChildElements - 1] = null;
        if (textChunks != null) {
            textChunks[index] += textChunks[index + 1];
            System.arraycopy(textChunks, index + 2, textChunks, index + 1, numChildElements - index - 1);
            textChunks[numChildElements] = null;
        }
        numChildElements -= 1;
        return old;
    }

    public void removeRange(int fromIndex, int toIndex) {
        checkElementIndex(fromIndex);
        for (int to = toIndex, from = fromIndex; to < fromIndex; to++, from++) {
            Element e = childElements[from];
            e.indexInParent = to;
            childElements[to] = e;
        }
        int newSize = numChildElements - (toIndex - fromIndex);
        Arrays.fill(childElements, newSize, numChildElements, null);
        if (textChunks != null) {
            textChunks[fromIndex] += textChunks[toIndex];
            System.arraycopy(textChunks, toIndex, textChunks, fromIndex, numChildElements - toIndex);
            Arrays.fill(textChunks, newSize + 1, numChildElements + 1, null);
        }
        numChildElements = newSize;
    }

    private void checkElementIndex(int index) {
        if (index >= numChildElements)
            throw new IndexOutOfBoundsException();
    }

    private void checkTextIndex(int index) {
        if (index > numChildElements)
            throw new IndexOutOfBoundsException();
    }
}
