package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

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

public class Element implements Cloneable, Appendable {
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
    @NotNull
    private AttributeSet attributeSet;

    static private final ChildElementList EMPTY_CHILDREN = new ChildElementList(null);
    static private final AttributeSet EMPTY_ATTRIBUTES = new HashAttributeSet();

    private ChildElementList childElements = EMPTY_CHILDREN;

    // An array containing all the text.
    @NotNull
    private char[] text;

    // Total number of characters of content (stored in text)
    private int textLength;


    /**
     * Creates an Element with a given name.
     * The attributes and content of the element are empty.
     * @param name the name of the Element
     */
    public Element(@NotNull String name) {
        this(name, EMPTY_ATTRIBUTES);
    }

    /**
     * Creates an Element with a given name and attributes.
     * The content of the Element is empty.
     * @param name the name of the Element to be created
     * @param attributeSet the attributes of the Element to be created; may be null
     */
    public Element(@NotNull String name, @NotNull AttributeSet attributeSet) {
        checkNotNull(name);
        this.name = name;
        this.attributeSet = attributeSet;
        parent = null;
        indexInParent = -1;
        childElements = EMPTY_CHILDREN;
        text = EMPTY_TEXT;
        textLength = 0;
    }

    public Element clone() {
        try {
            Element cloned = (Element)super.clone();
            cloned.parent = null;
            cloned.indexInParent = -1;
            if (attributeSet != EMPTY_ATTRIBUTES)
                cloned.attributeSet = attributeSet.clone();
            cloned.text = text.length == 0 ? EMPTY_TEXT : Arrays.copyOf(text, textLength);
            if (childElements != EMPTY_CHILDREN)
                cloned.childElements = new ChildElementList(cloned, childElements);
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
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
        if (attributeSet == EMPTY_ATTRIBUTES)
            attributeSet = new HashAttributeSet();
        return attributeSet;
    }

    @NotNull
    public List<Element> children() {
        if (childElements == EMPTY_CHILDREN)
            childElements = new ChildElementList(this);
        return childElements;
    }

    /**
     * Returns true if this Element has one or more Attributes.
     * This is a shorthand for <code>!attributes().isEmpty()</code>.
     * Using <code>hasAttributes()</code> may allow the implementation to avoid creating an AttributeSet object
     * for Elements that have no attributes.
     * @return true if this Element has one or more Attributes
     */
    public boolean hasAttributes() {
        return !attributeSet.isEmpty();
    }

    public boolean hasContent() {
        return textLength > 0 || !childElements.isEmpty();
    }

    /**
     * Returns true if this Element has one or more child elements.
     */
    public boolean hasChildren() {
        return !childElements.isEmpty();
    }

    /**
     * Returns true if this Element's content contains one or more characters.
     * @return true if this Element's content contains one or more characters
     */
    public boolean hasText() {
        return textLength > 0;
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
     * Returns the root Element.
     * @return the root Element, never null
     */
    @NotNull
    public Element getRoot() {
        Element root = this;
        while (root.parent != null)
            root = root.parent;
        return root;
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

    private static class ChildElementList extends AbstractList<Element> implements RandomAccess {
        static final int INITIAL_CAPACITY = 8;
        static final Element[] EMPTY = new Element[0];

        // The number of child elements.
        int length;
        // An array containing the child elements.
        @NotNull
        Element[] elements;
        final Element owner;


        ChildElementList(Element owner) {
            this.owner = owner;
            length = 0;
            elements = EMPTY;
        }

        ChildElementList(Element owner, ChildElementList list) {
            this.owner = owner;
            if (list.length == 0)
                elements = EMPTY;
            else {
                elements = new Element[list.length];
                for (int i = 0; i < list.length; i++) {
                    elements[i] = list.elements[i].clone();
                    owner.attach(elements[i], i, list.elements[i].charIndexInParent);
                }
            }
            length = list.length;
        }

        /**
         * Returns the number of Elements in this List.
         * @return the number of Elements in this List.
         */
        public int size() {
            return length;
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
            checkIndex(index);
            return elements[index];
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
        public boolean add(@NotNull Element element) {
            checkNotNull(element);
            modCount++;
            ensureCapacity(length + 1);
            owner.attach(element, length, owner.textLength);
            elements[length++] = element;
            return true;
        }

        public void ensureCapacity(int minCapacity) {
            if (elements.length >= minCapacity)
                return;
            int newLength = elements.length == 0 ? INITIAL_CAPACITY : elements.length*2;
            if (newLength < minCapacity)
                newLength = minCapacity;
            elements = Arrays.copyOf(elements, newLength);
        }

        public void clear() {
            if (length == 0)
                return;
            modCount++;
            for (int i = 0; i < length; i++)
                elements[i].detach();
            elements = EMPTY;
            length = 0;
        }

        /**
         * Replaces the Element at a specified position.
         * @param index the position for the Element to be replaced
         * @param element the replacement Element; must not be null
         * @return the Element previously at the position, or null if none
         * @throws IllegalArgumentException if element already has a parent
         * @throws NullPointerException if element is null
         */
        @NotNull
        public Element set(int index, @NotNull Element element) {
            checkIndex(index);
            checkNotNull(element);
            Element old = elements[index];
            int charIndex = old.charIndexInParent;
            // Detach the old element first, in case we are replacing it by itself.
            old.detach();
            owner.attach(element, index, charIndex);
            elements[index] = element;
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
            if (index == length) {
                add(element);
                return;
            }
            checkNotNull(element);
            checkIndex(index);
            int charIndex;
            if (index == length)
                charIndex = owner.textLength;
            else
                charIndex = elements[index].charIndexInParent;
            modCount++;
            ensureCapacity(length + 1);
            for (int i = length; i > index; --i) {
                Element e = elements[i - 1];
                elements[i] = e;
                e.indexInParent = i;
            }
            owner.attach(element, index, charIndex);
            elements[index] = element;
        }

        /**
         * Removes the child Element at a specified position.
         * The removed Element is detached from its parent.
         * No characters are removed;
         * thus the text chunks immediately before and after the element are joined.
         * @param index the position of the Element to be removed
         * @return the Element that was removed
         */
        @NotNull
        public Element remove(int index) {
            checkIndex(index);
            ++modCount;
            Element old = elements[index];
            old.detach();
            for (int i = index; i < length - 1; i++) {
                Element e = elements[i + 1];
                e.indexInParent = i;
                elements[i] = e;
            }
            elements[length - 1] = null;
            // Don't need to do anything to text.
            length -= 1;
            return old;
        }

        void checkIndex(int index) {
            if (index >= length)
                throw new IndexOutOfBoundsException();
        }

        void clearCharIndexInParent() {
            for (int i = 0; i < length; i++)
                elements[i].charIndexInParent = 0;
        }

        void incCharIndexInParent(int startIndex, int endIndex, int inc) {
            for (; startIndex < endIndex; startIndex++)
                elements[startIndex].charIndexInParent += inc;
        }
    }

    /**
     * Removes all content.
     */
    public void clearContent() {
        if (textLength > 0)
            textChanging(0, textLength, 0);
        childElements.clear();
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
        childElements.clearCharIndexInParent();
        text = EMPTY_TEXT;
        textLength = 0;
    }

    /**
     * Adds an Element to the list of children.
     * Equivalent to <tt>children().add(element)</tt.>
     * @param element
     * @return this Element
     */
    public Element append(Element element) {
        children().add(element);
        return this;
    }

    /**
     * Adds characters at the end of the content of this Element.
     * @param csq a CharSequence with the characters to add; must not be empty
     * @return this Element
     */
    public Element append(CharSequence csq) {
        if (csq == null)
            csq = "null";
        int length = csq.length();
        ensureTextCapacity(textLength + length);
        textChanging(textLength, textLength, length);
        if (csq instanceof String) {
            ((String)csq).getChars(0, length, text, textLength);
            textLength += length;

        }
        else {
            for (int i = 0; i < length; i++) {
                text[textLength] = csq.charAt(i);
                textLength++;
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * @return a reference to this Element
     */
    public Element append(CharSequence csq, int start, int end) {
        int length = end - start;
        if (length < 0 || start < 0 || end > textLength)
            throw new IndexOutOfBoundsException();
        ensureTextCapacity(textLength + length);
        textChanging(textLength, textLength, length);
        for (; start < end; start++)
            text[textLength++] = csq.charAt(start);
        return this;
    }

    /**
     * Adds a character at the end of the content of this Element.
     *
     * @param c the character to be added.
     * @return this Element
     */
    public Element append(char c) {
        ensureTextCapacity(textLength + 1);
        textChanging(textLength, textLength, 1);
        text[textLength++] = c;
        return this;
    }

    public Element append(char[] buf, int offset, int length) {
        if (length < 0 || offset < 0 || offset + length > buf.length)
            throw new IndexOutOfBoundsException();
        ensureTextCapacity(textLength + length);
        textChanging(textLength, textLength, length);
        System.arraycopy(buf, offset, text, textLength, length);
        textLength += length;
        return this;
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

    /**
     * Returns the text chunk immediately before this element.
     * @return the text immediately before this element.
     */
    public String getTextBefore() {
        if (parent == null)
            return "";
        return parent.getText(indexInParent);
    }

    /**
     * Returns the text chunk immediately after this element.
     * This contains all characters after this element up to the next element (if there is one).
     * @return the text immediately after this element.
     */
    public String getTextAfter() {
        if (parent == null)
            return "";
        return parent.getText(indexInParent + 1);
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
            childElements.incCharIndexInParent(index, childElements.size(), newTextLength - textLength);
            textLength = newTextLength;
        }
    }

    private int getTextChunkStartIndex(int index) {
         if (index <= 0) {
             if (index < 0)
                 throw new IndexOutOfBoundsException();
             return 0;
         }
         if (index > childElements.length)
             throw new IndexOutOfBoundsException();
         return childElements.elements[index - 1].charIndexInParent;
    }

    // This assumes that index is in bounds
    private int getTextChunkEndIndex(int index) {
        return index == childElements.length ? textLength : childElements.elements[index].charIndexInParent;
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
