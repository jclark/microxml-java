package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class Element {
    @NotNull
    private String name;
    @Nullable
    private Element parent;
    private int indexInParent;
    @Nullable
    private AttributeSet attributeSet;
    private final ContentListImpl contentList;

    /**
     * Creates an Element with a given name.
     * The attributes and content of the element are empty.
     * @param name the name of the Element
     */
    public Element(@NotNull String name) {
        this(name, null);
    }

    public Element(@NotNull String name, @Nullable AttributeSet attributeSet) {
        checkNotNull(name);
        this.name = name;
        this.parent = null;
        this.indexInParent = -1;
        this.attributeSet = attributeSet;
        this.contentList = new ContentListImpl();
    }

    public Element(@NotNull Element element) {
        checkNotNull(element);
        this.name = element.name;
        this.parent = null;
        this.indexInParent = -1;
        this.attributeSet = element.attributeSet == null ? null : element.attributeSet.clone();
        this.contentList = new ContentListImpl(element.contentList);
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
     * Returns the content of this Element.
     * @return a ContentList for the content of this Element; never null
     */
    @NotNull
    public ContentList content() {
        return contentList;
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

    private void attach(Element parent, int indexInParent) {
        if (this.parent != null)
            throw new IllegalArgumentException("Element already has a parent");
        this.parent = parent;
        this.indexInParent = indexInParent;
    }

    static void checkNotNull(Object obj) {
        if (obj == null)
            throw new NullPointerException();
    }

    /**
     * The contentList of an element.
     */
    private final class ContentListImpl implements ContentList {
        // The number of elements in the ContentList.
        private int size;

        // An array containing the elements.
        // If size is 0, maybe null.
        private Element[] elements;

        // An array containing the text chunks.
        // If non-null, then
        // + length equal to elements.length + 1
        // + for 0 <= i <= size, textChunks[i] != null
        // If null, means all text chunks are empty.
        @Nullable
        private String[] textChunks;

        // Used to provide fail-fast behaviour for iterators.
        private int modCount = 0;

        private ContentListImpl() {
            size = 0;
            elements = null;
            textChunks = null;
        }

        private ContentListImpl(ContentListImpl content) {
            size = content.size;
            textChunks = content.textChunks == null ? null : Arrays.copyOf(content.textChunks, size + 1);
            elements = size == 0 ? null : new Element[size];
            for (int i = 0; i < size; i++) {
                Element e = new Element(content.elements[i]);
                e.attach(Element.this, i);
                elements[i] = e;
            }
        }

        @NotNull
        public Element getOwner() {
            return Element.this;
        }

        public boolean isEmpty() {
            return size == 0 && (textChunks == null || textChunks[0].isEmpty());
        }

        public int size() {
            return size;
        }

        public void add(@NotNull Element element) {
            modCount++;
            ensureCapacity(size + 1);
            element.attach(Element.this, size);
            elements[size++] = element;
            if (textChunks != null)
                textChunks[size] = "";
        }

        public void ensureCapacity(int minCapacity) {
            int newLength;
            if (elements == null)
                newLength = 8;
            else if (elements.length >= minCapacity)
                return;
            else
                newLength = elements.length * 2;
            if (newLength < minCapacity) {
                if (minCapacity == Integer.MAX_VALUE)
                    throw new IllegalArgumentException();
                newLength = minCapacity;
            }
            if (elements == null)
                elements = new Element[newLength];
            else
                elements = Arrays.copyOf(elements, newLength);
            if (textChunks != null)
                textChunks = Arrays.copyOf(textChunks, newLength + 1);
         }

        @NotNull
        private String[] allocateTextChunks() {
            String[] v = new String[elements == null ? 1 : elements.length + 1];
            Arrays.fill(v, 0, size + 1, "");
            return v;
        }

        public void add(@NotNull String text) {
            if (!text.isEmpty()) {
                if (textChunks == null)
                   textChunks = allocateTextChunks();
                textChunks[size] += text;
            }
        }

        public class ContentIterator implements Iterator<Element> {
            private int nextIndex = 0;
            private int lastReturned = -1;
            private int expectedModCount = modCount;

            public boolean hasNext() {
                checkModCount();
                return nextIndex < size;
            }

            /**
             * Returns the text before the element to be returned by next().
             * @return the text chunk before the element to be returned by next()
             */
            public String getText() {
                return ContentListImpl.this.getText(nextIndex);
            }

            public Element next() {
                checkModCount();
                return elements[lastReturned = nextIndex++];
            }

            public void remove() {
                if (lastReturned == -1)
                    throw new IllegalStateException();
                checkModCount();
                ContentListImpl.this.remove(lastReturned);
                --nextIndex;
                expectedModCount = modCount;
                lastReturned = -1;
            }

            private void checkModCount() {
                if (expectedModCount != modCount)
                    throw new ConcurrentModificationException();
            }
        }

        public ContentIterator iterator() {
            return new ContentIterator();
        }

        @NotNull
        public Element get(int index) {
            checkElementIndex(index);
            return elements[index];
        }

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
         * Removes all elements and text.
         */
        public void clear() {
            modCount++;
            for (int i = 0; i < size; i++)
                elements[i].detach();
            elements = null;
            textChunks = null;
            size = 0;
        }

        public void clearText() {
            textChunks = null;
        }

        public int indexOfNonWhitespaceText() {
            if (textChunks != null) {
                for (int i = 0; i <= size; i++) {
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

        public Element set(int index, @NotNull Element element) {
            checkElementIndex(index);
            checkNotNull(element);
            Element old = elements[index];
            // Detach the old element first, in case we are replacing it by itself.
            old.detach();
            element.attach(Element.this, index);
            elements[index] = element;
            return old;
        }

        public void add(int index, @NotNull Element element) {
            if (index == size) {
                add(element);
                return;
            }
            checkNotNull(element);
            checkElementIndex(index);
            modCount++;
            ensureCapacity(size + 1);
            for (int i = size; i > index; --i) {
                Element e = elements[i - 1];
                elements[i] = e;
                e.indexInParent = i;
            }
            elements[index] = element;
            if (textChunks != null) {
                System.arraycopy(textChunks, index, textChunks, index + 1, size + 1 - index);
                textChunks[index] = "";
            }
        }

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

        public void trimToSize() {
            // TODO
        }

        public Element remove(int index) {
            checkElementIndex(index);
            ++modCount;
            Element old = elements[index];
            old.detach();
            for (int i = index; i < size - 1; i++) {
                Element e = elements[i + 1];
                e.indexInParent = i;
                elements[i] = e;
            }
            elements[size - 1] = null;
            if (textChunks != null) {
                textChunks[index] += textChunks[index + 1];
                System.arraycopy(textChunks, index + 2, textChunks, index + 1, size - index - 1);
                textChunks[size] = null;
            }
            size -= 1;
            return old;
        }

        public void removeRange(int fromIndex, int toIndex) {
            checkElementIndex(fromIndex);
            for (int to = toIndex, from = fromIndex; to < fromIndex; to++, from++) {
                Element e = elements[from];
                e.indexInParent = to;
                elements[to] = e;
            }
            int newSize = size - (toIndex - fromIndex);
            Arrays.fill(elements, newSize, size, null);
            if (textChunks != null) {
                textChunks[fromIndex] += textChunks[toIndex];
                System.arraycopy(textChunks, toIndex, textChunks, fromIndex, size - toIndex);
                Arrays.fill(textChunks, newSize + 1, size + 1, null);
            }
            size = newSize;
        }

        private void checkElementIndex(int index) {
            if (index >= size)
                throw new IndexOutOfBoundsException();
        }

        private void checkTextIndex(int index) {
            if (index > size)
                throw new IndexOutOfBoundsException();
        }

    }
}
