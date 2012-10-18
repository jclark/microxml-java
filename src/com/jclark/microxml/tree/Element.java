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
public class Element implements Iterable<Element> {
    private static char[] EMPTY_TEXT = new char[0];
    private static final int INITIAL_TEXT_CAPACITY = 8;

    @NotNull
    private String name;
    @Nullable
    private Element parent;
    // index in its parent, -1 if not attached
    private int indexInParent;
    // index in the text array of its parent; undefined if not attached
    private int charIndexInParent;
    @Nullable
    private AttributeSet attributeSet;

    // The number of child elements.
    private int numChildElements;

    // An array containing the child elements.
    // If numChildElements is 0, maybe null.
    private Element[] childElements;

    // An array containing all the text.
    @NotNull
    private char[] text;

    // Total number of characters of content (stored in text)
    int textLength;

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
        text = EMPTY_TEXT;
        textLength = 0;
    }

    public Element(@NotNull Element element) {
        checkNotNull(element);
        name = element.name;
        parent = null;
        indexInParent = -1;
        attributeSet = element.attributeSet == null ? null : element.attributeSet.clone();
        numChildElements = element.numChildElements;
        text = Arrays.copyOf(text, textLength);
        textLength = element.textLength;
        childElements = numChildElements == 0 ? null : new Element[numChildElements];
        for (int i = 0; i < numChildElements; i++) {
            Element childElement = element.childElements[i];
            Element e = new Element(childElement);
            attach(e, i, childElement.charIndexInParent);
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
            attributeSet = new HashAttributeSet();
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
    private void attach(Element child, int indexInParent, int charIndexInParent) {
        if (child.parent != null)
            throw new IllegalArgumentException("Element already has a parent");
        child.parent = this;
        child.indexInParent = indexInParent;
        child.charIndexInParent = charIndexInParent;
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
        return numChildElements == 0 && textLength == 0;
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
        attach(element, numChildElements, textLength);
        childElements[numChildElements++] = element;
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
     * Removes all content.
     */
    public void clear() {
        if (textLength > 0)
            textChanging(0, textLength, 0);
        modCount++;
        for (int i = 0; i < numChildElements; i++)
            childElements[i].detach();
        childElements = null;
        numChildElements = 0;
        text = EMPTY_TEXT;
        textLength = 0;
    }

    /**
     * Removes all text.
     */
    public void clearText() {
        if (textLength == 0)
            return;
        textChanging(0, textLength, 0);
        for (int i = 0; i < numChildElements; i++)
            childElements[i].charIndexInParent = 0;
        text = EMPTY_TEXT;
        textLength = 0;
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
        int charIndex = old.charIndexInParent;
        // Detach the old element first, in case we are replacing it by itself.
        old.detach();
        attach(element, index, charIndex);
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
        int charIndex;
        if (index == numChildElements)
            charIndex = textLength;
        else
            charIndex = childElements[index].charIndexInParent;
        modCount++;
        ensureCapacity(numChildElements + 1);
        for (int i = numChildElements; i > index; --i) {
            Element e = childElements[i - 1];
            childElements[i] = e;
            e.indexInParent = i;
        }
        attach(element, index, charIndex);
        childElements[index] = element;
    }

    /**
     * Adds characters at the end of the content of this Element.
     * @param str a String with the characters to add; must not be empty
     */
    public void add(@NotNull String str) {
        int length = str.length();
        ensureTextCapacity(textLength + length);
        textChanging(textLength, textLength, length);
        str.getChars(0, length, text, textLength);
        textLength += length;
    }

    /**
     * Adds a character at the end of the content of this Element.
     *
     * @param c the character to be added.
     */
    public void add(char c) {
        ensureTextCapacity(textLength + 1);
        textChanging(textLength, textLength, 1);
        text[textLength++] = c;
    }

    public void add(char[] buf, int offset, int length) {
        if (length < 0 || offset < 0 || offset + length > buf.length)
            throw new IndexOutOfBoundsException();
        ensureTextCapacity(textLength + length);
        textChanging(textLength, textLength, length);
        System.arraycopy(buf, offset, text, textLength, length);
        textLength += length;
    }

    private void ensureTextCapacity(int minCapacity) {
        if (text.length >= minCapacity)
            return;
        int newLength;
        if (text.length == 0)
            newLength = INITIAL_TEXT_CAPACITY;
        else
            newLength = text.length * 2;
        if (newLength < minCapacity)
            newLength = minCapacity;
        text = Arrays.copyOf(text, newLength);
    }

    /**
     * Returns true if this Element's content contains one or more characters.
     * @return true if this Element's content contains one or more characters
     */
    public boolean hasText() {
        return textLength > 0;
    }

    /**
     * Returns true if all the text is whitespace.
     * This will automatically be true if the text is empty.
     *
     * @return true if all text is whitespace; false otherwise
     */
    public boolean isTextAllWhitespace() {
        int i = textLength;
        while (i > 0) {
            char c = text[--i];
            if (c > ' ')
                return false;
            switch (c) {
            case ' ':
            case '\r':
            case '\t':
            case '\n':
                break;
            default:
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the text chunk at a specified position.
     *
     * @param index the position of the text chunk to be returned
     * @return the text chunk at position index; this may be empty but is never null
     */
    @NotNull
    public String getText(int index) {
        int startIndex = getTextChunkStartIndex(index);
        int endIndex = getTextChunkEndIndex(index);
        return startIndex == endIndex ? "" : new String(text, startIndex, endIndex - startIndex);
    }

    private int getTextChunkStartIndex(int index) {
        if (index <= 0) {
            if (index < 0)
                throw new IndexOutOfBoundsException();
            return 0;
        }
        if (index > numChildElements)
            throw new IndexOutOfBoundsException();
        return childElements[index - 1].charIndexInParent;
    }

    // This assumes that index is in bounds
    private int getTextChunkEndIndex(int index) {
        return index == numChildElements ? textLength : childElements[index].charIndexInParent;
    }

    /**
     * Changes the text chunk at a specified position.
     * @param index the position
     * @param str the new text chunk for the position; this may be empty, but must not be null
     */
    public void setText(int index, @NotNull String str) {
        int startIndex = getTextChunkStartIndex(index);
        int endIndex = getTextChunkEndIndex(index);
        // str.length() ensures NullPointerException is thrown if str is null
        int strLength = str.length();
        int newTextLength = (textLength - (endIndex - startIndex)) + strLength;
        ensureTextCapacity(newTextLength);
        textChanging(startIndex, endIndex, strLength);
        // Copy existing characters to the right position
        System.arraycopy(text, endIndex, text, startIndex + strLength, textLength - endIndex);
        // Copy new characters
        str.getChars(0, strLength, text, startIndex);
        if (textLength != newTextLength) {
            int inc = newTextLength - textLength;
            for (; index < numChildElements; index++)
                childElements[index].charIndexInParent += inc;
            textLength = newTextLength;
        }
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
        // Don't need to do anything to text.
        numChildElements -= 1;
        return old;
    }

    /**
     * Removes count elements starting at start.
     * Text between the removed elements is also removed.
     * @param start the index of the first element to remove
     * @param count the number of elements to remove
     */
    public void remove(int start, int count) {
        if (count < 2) {
            if (count == 1) {
                remove(start);
                return;
            }
            if (count == 0)
                return;
            throw new IndexOutOfBoundsException();
        }
        checkElementIndex(start);
        modCount++;
        int end = start + count;
        // Remove the text chars
        int textStartIndex = childElements[start].indexInParent;
        int textEndIndex = childElements[end - 1].indexInParent;
        textChanging(textStartIndex, textEndIndex, 0);
        int removedCharCount = textEndIndex - textStartIndex;
        System.arraycopy(text, textEndIndex, text, textStartIndex, textLength - textEndIndex);
        textEndIndex -= removedCharCount;
        // Detach the elements that will be removed
        for (int i = start; i < end; i++)
            childElements[i].detach();
        // Move the following elements into place
        for (int src = end, dst = start; src < numChildElements; src++, dst++) {
            Element e = childElements[src];
            e.indexInParent = dst;
            e.charIndexInParent -= removedCharCount;
            childElements[dst] = e;
        }
        int newSize = numChildElements - count;
        Arrays.fill(childElements, newSize, numChildElements, null);
        numChildElements = newSize;
    }

    private void checkElementIndex(int index) {
        if (index >= numChildElements)
            throw new IndexOutOfBoundsException();
    }

    /* Locations */

    /**
     * Returns the Location of the start-tag or empty-element tag for this Element.
     *
     * @return the Location of the start-tag or empty-element tag for this Element; null if no Location is available
     */
    public Location getStartTagLocation() {
        return null;
    }

    /**
     * Returns the Location of the end-tag for this Element.
     * If this Element used an empty-element tag, returns the location of the "/>" that terminated the tag.
     *
     * @return the Location of the end-tag of this Element; null if no Location is available
     */
    public Location getEndTagLocation() {
        return null;
    }

    /**
     * Returns the location of a range of characters in the content.
     * The range is allowed to be empty.
     *
     * @param chunkIndex the position of the text chunk containing the characters
     * @param beginIndex the index within the text chunk of the first character of the range
     * @param endIndex the index within the text chunk following the last character of the range
     * @return the Location of the specified range of characters; null if no Location is available
     * @throws IndexOutOfBoundsException if an index is out of range
     */
    public Location getTextLocation(int chunkIndex, int beginIndex, int endIndex) {
        // Check argument validity
        getAbsoluteStartIndex(chunkIndex, beginIndex, endIndex);
        return null;
    }

    protected int getAbsoluteStartIndex(int chunkIndex, int startIndex, int endIndex) {
        int chunkStartIndex = getTextChunkStartIndex(chunkIndex);
        int chunkEndIndex = getTextChunkEndIndex(chunkIndex);
        if (startIndex < 0 || endIndex > chunkEndIndex - chunkStartIndex)
            throw new IndexOutOfBoundsException();
        return chunkStartIndex + startIndex;
    }
    /**
     * Called when a range of characters in the content are about to change.
     *
     * @param start the index of the first character that is to be changed
     * @param end the index after the last character that is to be changed
     * @param length the number of characters that will be in the range after the change
     */
    protected void textChanging(int start, int end, int length) {
        // do nothing
    }

    protected final int getTextLength() {
        return textLength;
    }

}
