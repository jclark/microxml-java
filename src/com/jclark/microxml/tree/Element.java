package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;

/**
 * An element in a markup language such as XML or HTML. An element has three components: a name, a set of attributes,
 * and content. Each attribute consists of a name and a value, which are both strings; the names of the attributes of an
 * element are always distinct. The content of an element is a sequence of characters and elements.
 * <p/>
 * Element identity is object identity: two elements are considered different if they are different objects. An element
 * can occur in the content of at most one element, which is called its <i>parent</i>. An element keeps a reference to
 * its parent. The <i>children</i> of an element are the elements occurring in its content. Each child of an element has
 * an integer index, which uniquely identifies it amongst the children of the element: the first child has index 0, and
 * the last child has index of one less than the number of children of the element.
 * <p/>
 * The characters in the content of an element are called the <i>text</i> of the element. They are not considered to be
 * children of the element. Identity for characters in the content of an element is value identity. The characters in
 * the content are accessed as text chunks, based on their position in the content relative to child elements. The
 * characters in the content of an element having n children are divided into n + 1 text chunks, each of which may be
 * empty. The text chunk with index 0 is the subsequence of the characters in the content that precedes all elements;
 * the text chunk with index n is the subsequence of the characters in the content that follows all elements; for
 * {@literal 0 < i < n}, the text chunk with index i is the subsequence of the characters in the content that follows
 * the element with index i - 1 and precedes the element with index i. Thus an element with no children has a single
 * text chunk with index 0.
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
     * Creates an element with a given name.
     * The created element has empty attributes and content, and has no parent.
     * @param name the name of the element; must not be null
     * @throws NullPointerException if name is null
     */
    public Element(@NotNull String name) {
        this(name, EMPTY_ATTRIBUTES);
    }

    /**
     * Creates an element with a given name and attributes.
     * The created element has empty content and no parent.
     *
     * @param name the name of the element; must not be null
     * @param attributeSet the attributes of the element to be created; may be null
     * @throws NullPointerException if name is null
     */
    public Element(@NotNull String name, @NotNull AttributeSet attributeSet) {
        Util.requireNonNull(name);
        this.name = name;
        this.attributeSet = attributeSet;
        parent = null;
        indexInParent = -1;
        childElements = EMPTY_CHILDREN;
        text = EMPTY_TEXT;
        textLength = 0;
    }

    /**
     * Creates and returns a copy of this element.
     * The copy will have no parent.
     * The newly created element is independent of this element: they can each be modified without affecting the other.
     * @return a copy of this element
     */
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
     * Returns the name of this element.
     * @return the name of this element; never null
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Changes the name of this element.
     * @param name the new name of this element; must not be null
     * @throws NullPointerException if name is null
     */
    public void setName(@NotNull String name) {
        Util.requireNonNull(name);
        this.name = name;
    }

    /**
     * Returns the attributes of this element.
     * @return an AttributeSet representing the attributes of this element; never null
     */
    @NotNull
    public AttributeSet attributes() {
        if (attributeSet == EMPTY_ATTRIBUTES)
            attributeSet = new HashAttributeSet();
        return attributeSet;
    }

    /**
     * Returns true if this element has one or more attributes.
     * This is a shorthand for {@code !attributes().isEmpty()}.
     * Use of this method may allow the implementation to avoid creating an {@code AttributeSet} object
     * for Elements that have no attributes.
     * @return true if this element has one or more attributes
     */
    public boolean hasAttributes() {
        return !attributeSet.isEmpty();
    }

    /**
     * Returns a list of the children of this element. The list is "live": modifying the list changes the children of
     * this element. Modifying this list does not, however, modify the text of this element. When an element is inserted
     * in the list at index i, it is inserted in this element's content immediately after the characters comprising text
     * chunk i.
     *
     * @return a list of the children of this element; never null
     */
    @NotNull
    public List<Element> children() {
        if (childElements == EMPTY_CHILDREN)
            childElements = new ChildElementList(this);
        return childElements;
    }

    /**
      * Returns true if this element has one or more children.
      * @return true if this element has one or more children
      */
     public boolean hasChildren() {
         return !childElements.isEmpty();
     }

    /**
     * Returns the text of this element.
     * @return a String containing the text of this element; never null
     */
    @NotNull
    public String getText() {
        if (textLength == 0)
            return "";
        return new String(text, 0, textLength);

    }

    /**
     * Returns true if this element's content contains one or more characters.
     * @return true if this element's content contains one or more characters
     */
    public boolean hasText() {
        return textLength > 0;
    }

    /**
     * Returns true if this element has non-empty content.
     * An element's content is non-empty if its content includes one or more elements or characters.
     * @return true if this element has non-empty content
     */
    public boolean hasContent() {
        return textLength > 0 || !childElements.isEmpty();
    }

    /**
     * Returns the parent of this element, or null if this element has no parent.
     * Methods that add to or remove from the children of element automatically modify the
     * @return the parent element of this element, if there is one; null otherwise
     */
    @Nullable
    public Element getParent() {
        return parent;
    }

    /**
     * Returns the root element of this element.
     * An element that has no parent is its own root element; otherwise the root element of an element
     * is the root element of its parent.
     * @return the root element of this element, never null
     */
    @NotNull
    public Element getRoot() {
        Element root = this;
        while (root.parent != null)
            root = root.parent;
        return root;
    }

    /**
     * Returns the index that this element has in its parent, or -1 if this element has no parent.
     * The index in its parent is the zero-based index amongst the children of the parent. Thus,
     * the first child of an element will have index 0.
     * @return the index that this element has in its parent, or -1 if this element has no parent
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
        // hasChildren() call avoids potentially expensive call to getRoot() in common case
        if (child.hasChildren() && getRoot() == child)
            throw new IllegalArgumentException("an Element cannot be its own ancestor");
        attachNoCheck(child, indexInParent, charIndexInParent);
    }

    private void attachNoCheck(Element child, int indexInParent, int charIndexInParent) {
        child.parent = this;
        child.indexInParent = indexInParent;
        child.charIndexInParent = charIndexInParent;
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
                    owner.attachNoCheck(elements[i], i, list.elements[i].charIndexInParent);
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
            Util.requireNonNull(element);
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
            Util.requireNonNull(element);
            Element old = elements[index];
            int charIndex = old.charIndexInParent;
            owner.attach(element, index, charIndex);
            old.detach();
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
            Util.requireNonNull(element);
            checkIndex(index);
            int charIndex;
            if (index == length)
                charIndex = owner.textLength;
            else
                charIndex = elements[index].charIndexInParent;
            modCount++;
            ensureCapacity(length + 1);
            owner.attach(element, index, charIndex);
            for (int i = length; i > index; --i) {
                Element e = elements[i - 1];
                elements[i] = e;
                e.indexInParent = i;
            }
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

        void selfCheck() {
            assert length >= 0;
            assert length <= elements.length;
            for (int i = 0; i < length; i++) {
                assert elements[i].parent == owner;
                elements[i].selfCheck();
                if (i > 0)
                    assert elements[i - 1].charIndexInParent <= elements[i].charIndexInParent;
            }
        }
    }

    /**
     * Removes all content from this element.
     * After this method returns, {@code hasContent()} will return false.
     */
    public void clearContent() {
        if (textLength > 0)
            textChanging(0, textLength, 0);
        childElements.clear();
        text = EMPTY_TEXT;
        textLength = 0;
    }

    /**
     * Removes all text content from this element.
     * After this method returns, {@code hasText()} will return false.
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
     * Appends an element to the content of this element.
     * Equivalent to {@code children().add(child)}.
     * @param child the element to be added
     * @return a reference to this element
     */
    public Element append(Element child) {
        children().add(child);
        return this;
    }

    /**
     * Appends a CharSequence to the content of this element.
     * If the CharSequence is null, appends the 4 characters {@code null} (this is required by the
     * {@link Appendable} interface).
     * @param csq a CharSequence with the characters to add
     * @return a reference to this element
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
     * @return a reference to this element
     * @throws IndexOutOfBoundsException {@inheritDoc}
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
     * @return a reference to this element
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
     * Returns true if all the text of this element is whitespace.
     * This will trivially be true if the text of this element is empty.
     *
     * @return true if all text content is whitespace; false otherwise
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
     * Returns the text chunk with a specified index.
     *
     * @param index the index of the text chunk to be returned
     * @return a String with the text chunk at the specified index; this may be empty but is never null
     * @throws IndexOutOfBoundsException if index is less than 0 or greater than the number of children
     */
    @NotNull
    public String getText(int index) {
        int startIndex = getTextChunkStartIndex(index);
        int endIndex = getTextChunkEndIndex(index);
        return startIndex == endIndex ? "" : new String(text, startIndex, endIndex - startIndex);
    }

    /**
     * Returns the text chunk immediately before this element.
     * @return a String with the text chunk immediately before this element
     */
    public String getTextBefore() {
        if (parent == null)
            return "";
        return parent.getText(indexInParent);
    }

    /**
     * Returns the text chunk immediately after this element.
     * @return a String with the text chunk immediately after this element
     */
    public String getTextAfter() {
        if (parent == null)
            return "";
        return parent.getText(indexInParent + 1);
    }

    /**
     * Changes the text chunk with a specified index.
     * @param index the index of the text chunk to be changed
     * @param str the new text chunk; this may be empty, but must not be null
     * @throws IndexOutOfBoundsException if index is less than 0 or greater than the number of children
     * @throws NullPointerException if str is null
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

    // Locations

    /**
     * Returns the Location of the start-tag or empty-element tag for this element.
     *
     * @return the Location of the start-tag or empty-element tag for this element; null if no Location is available
     */
    public Location getStartTagLocation() {
        return null;
    }

    /**
     * Returns the Location of the end-tag for this element.
     * If this element used an empty-element tag, returns the location of the "/>" that terminated the tag.
     *
     * @return the Location of the end-tag of this element; null if no Location is available
     */
    public Location getEndTagLocation() {
        return null;
    }

    /**
     * Returns the location of a range of characters in the content.
     * The range is allowed to be empty.
     *
     * @param chunkIndex the index of the text chunk containing the characters
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
     * Called when a range of characters in the content is about to change.
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

    void selfCheck() {
        if (childElements != EMPTY_CHILDREN)
            assert childElements.owner == this;
        assert childElements != null;
        assert attributeSet != null;
        if (attributeSet instanceof HashAttributeSet)
            ((HashAttributeSet)attributeSet).selfCheck();
        assert name != null;
        if (parent != null) {
            assert indexInParent >= 0;
            assert parent.childElements.elements[indexInParent] == this;
            assert charIndexInParent >= 0;
            assert charIndexInParent <= parent.textLength;
        }
        childElements.selfCheck();
    }
}
