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
    private Element parent;
    private int indexInParent;

    private final ContentImpl content;
    private final AttributesImpl attributes;

    /**
     * Creates an Element with a given name.
     * The attributes and content of the element are empty.
     * @param name
     */
    public Element(@NotNull String name) {
        checkNotNull(name);
        this.name = name;
        this.parent = null;
        this.indexInParent = -1;
        this.content = new ContentImpl();
        this.attributes = new AttributesImpl();
    }

    public Element(@NotNull Element element) {
        checkNotNull(element);
        this.name = element.name;
        this.parent = null;
        this.indexInParent = -1;
        this.content = new ContentImpl(element.content);
        this.attributes = new AttributesImpl(element.attributes);
    }

    /**
     * Returns the name of this Element.
     * @return
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Element.
     * @param name
     */
    public void setName(@NotNull String name) {
        checkNotNull(name);
        this.name = name;
    }

    /**
     * Returns the attributes of this Element.
     * @return an AttributesImpl object representing the attributes of this Element
     */
    @NotNull
    public final Attributes attributes() {
        return attributes;
    }

    /**
     * Returns the content of this Element.
     * @return a ContentImpl object representing the content of this Element
     */
    @NotNull
    public final Content content() {
        return content;
    }

    /**
     * Returns the parent Element, or null if this Element has no parent.
     * The parent of an element is set when it is added to the ContentImpl of another element.
     * @return
     */
    @Nullable
    public Element getParent() {
        return parent;
    }

    /**
     * Returns the index of this Element in its parent, or -1 if this Element has no parent.
     * @return
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
            throw new HierarchyException();
        this.parent = parent;
        this.indexInParent = indexInParent;
    }

    static void checkNotNull(Object obj) {
        if (obj == null)
            throw new NullPointerException();
    }

    /**
     * The content of an element.
     */
    private final class ContentImpl implements Content {
        // The number of elements in the content.
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

        private ContentImpl() {
            size = 0;
            elements = null;
            textChunks = null;
        }

        private ContentImpl(ContentImpl content) {
            size = content.size;
            textChunks = content.textChunks == null ? null : Arrays.copyOf(content.textChunks, size + 1);
            elements = size == 0 ? null : new Element[size];
            for (int i = 0; i < size; i++) {
                Element e = new Element(content.elements[i]);
                e.attach(Element.this, i);
                elements[i] = e;
            }
        }

        /**
         * Returns true if the ContentImpl has no elements and no text.
         * @return  true if this ContentImpl has no elements and no text.
         */
        public boolean isEmpty() {
            return size == 0 && (textChunks == null || textChunks[0].isEmpty());
        }

        /**
         * Returns the number of elements.
         * @return the number of elements
         */
        public int size() {
            return size;
        }

        /**
         * Appends an element.
         * @param element
         */
        public void add(@NotNull Element element) {
            ensureCapacity(size + 1);
            element.attach(Element.this, size);
            elements[size++] = element;
            if (textChunks != null)
                textChunks[size] = "";
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
                return ContentImpl.this.getText(nextIndex);
            }

            public Element next() {
                checkModCount();
                return elements[lastReturned = nextIndex++];
            }

            public void remove() {
                if (lastReturned == -1)
                    throw new IllegalStateException();
                checkModCount();
                ContentImpl.this.remove(lastReturned);
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
         * Creates an Iterator that iterates over the child elements.
         * @return
         */
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

        /**
         * Removes all text.
         */
        public void clearText() {
            textChunks = null;
        }

        /**
         * Returns the index of the first text chunk that is not all whitespace, or -1 if there is no such text chunk.
         */
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

        /**
         * Replaces an Element at a specified index.
         * @param index index of element to replace
         * @param element Element to be stored at index
         * @return Element previously stored at index
         */
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

        /**
         * Inserts an Element at a specified position.
         * @param index
         * @param element
         */
        public void add(int index, @NotNull Element element) {
            if (index == size) {
                add(element);
                return;
            }
            checkNotNull(element);
            checkElementIndex(index);
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

        /**
         *
         * @param index
         * @param str
         * @throws NullPointerException if str is null
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

        public void ensureCapacity(int minCapacity) {
             // TODO
        }

        public void trimToSize() {

        }


        @NotNull
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

        public void set(ContentImpl content) {
            // TODO

        }
        public void addAll(ContentImpl content) {
            // TODO
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


    private static class HierarchyException extends RuntimeException {
    }

}
