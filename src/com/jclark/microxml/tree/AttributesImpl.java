package com.jclark.microxml.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A collection of AttributesImpl.
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
class AttributesImpl extends AbstractSet<Attribute> implements Attributes {
    // hash table of attributes; uses open-addressing, linear-probing
    @Nullable
    private Attribute[] atts;
    // number of used, non-removed entries
    private int size;
    // number of used entries
    private int used;
    // the maximum value for used before rehashing
    private int usedLimit;
    // used for fast-fail iteration
    private int modCount = 0;
    // marks removed hash entries
    private static final Attribute REMOVED = new Attribute("", "");
    private static final int INITIAL_CAPACITY = 8; // must be power of 2
    private static final float LOAD_FACTOR = 0.6f;

    AttributesImpl() {
        atts = null;
        size = 0;
        used = 0;
        usedLimit = 0;
    }

    AttributesImpl(AttributesImpl atts) {
        // TODO
    }

    public int size() {
        return size;
    }

    public void clear() {
        atts = null;
        size = 0;
        used = 0;
        usedLimit = 0;
    }

    @Override
    public int hashCode() {
        if (size == 0)
            return 0;
        int h = 0;
        int i = atts.length;
        while (--i >= 0) {
            if (atts[i] != null && atts[i] != REMOVED) {
                // need a symmetric operation, since we are combining hash codes
                // in an undefined order
                h ^= atts[i].hashCode();
            }
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AttributesImpl other = (AttributesImpl)obj;
        if (size != other.size)
            return false;
        if (size == 0)
            return true;
        int i = atts.length;
        while (--i >= 0) {
            Attribute att = atts[i];
            if (att != null && att != REMOVED && !att.getValue().equals(other.getValue(att.getName())))
                return false;
        }
        return true;
    }

    /**
     * If the AttributesImpl do not already contain an attribute with the same name as att,
     * then add the attribute and return true. Otherwise, do not add the attribute and return false.
     * @param att
     * @return true if the attribute was added
     */
    public boolean add(@NotNull Attribute att) {
        String name = att.getName();
        ++modCount;
        if (used == usedLimit)
            rehash();
        int i = name.hashCode() & (atts.length - 1);
        while (atts[i] != null) {
            if (atts[i] != REMOVED && atts[i].getName().equals(name))
                return false;
            if (i == 0)
                i = atts.length;
            --i;
        }
        atts[i] = att;
        ++used;
        ++size;
        return true;
    }

    private void rehash() {
        if (atts == null) {
            atts = new Attribute[INITIAL_CAPACITY];
            usedLimit = (int)(INITIAL_CAPACITY*LOAD_FACTOR);
            return;
        }
        Attribute[] newAtts = new Attribute[atts.length * 2];
        for (int i = 0; i < atts.length; i++) {
            if (atts[i] != null && atts[i] != REMOVED) {
                int j = atts[i].getName().hashCode() & (newAtts.length - 1);
                while (newAtts[j] != null) {
                    if (j == 0)
                        j = newAtts.length;
                    --j;
                }
                newAtts[j] = atts[i];
            }
        }
        atts = newAtts;
        used = size;
        usedLimit = (int)(newAtts.length * LOAD_FACTOR);
    }

    @Nullable
    public Attribute get(@NotNull String name) {
        int i = find(name);
        if (i < 0)
            return null;
        return atts[i];
    }

    @Nullable
    public String getValue(@NotNull String name) {
        int i = find(name);
        if (i < 0)
            return null;
        return atts[i].getValue();
    }

    @Nullable
    public Attribute remove(@NotNull String name) {
        int i = find(name);
        if (i < 0)
            return null;
        Attribute old = atts[i];
        modCount++;
        atts[i] = REMOVED;
        --size;
        return old;
    }

    @Override
    public boolean contains(Object o) {
        return find(o) >= 0;
    }

    /**
     *
     * @param o
     * @return true if an element was removed
     */
    @Override
    public boolean remove(Object o) {
        int i = find(o);
        if (i < 0)
            return false;
        atts[i] = REMOVED;
        modCount++;
        --size;
        return true;
    }

    private int find(Object o) {
        Element.checkNotNull(o);
        Attribute att = (Attribute)o;
        String name = att.getName();
        int i = find(name);
        if (i >= 0 && atts[i].getValue().equals(att.getValue()))
            return i;
        return -1;
    }

    private int find(String name) {
        if (atts == null)
            Element.checkNotNull(name);
        else {
            int i = name.hashCode() & (atts.length - 1);
            while (atts[i] != null) {
                if (atts[i] != REMOVED && atts[i].getName().equals(name))
                    return i;
                if (i == 0)
                    i = atts.length;
                --i;
            }
        }
        return -1;
    }

    private class Iter implements Iterator<Attribute> {
        private int expectedModCount = modCount;
        private int nextIndex;
        private int toRemoveIndex = -1;

        private Iter() {
            if (atts == null)
                nextIndex = -1;
            else {
                nextIndex = atts.length;
                moveToNextUsed();
            }
        }

        private void moveToNextUsed() {
           while (--nextIndex >= 0) {
                if (atts[nextIndex] != null && atts[nextIndex] != REMOVED)
                    break;
           }
        }

        public boolean hasNext() {
            return nextIndex >= 0;
        }

        public Attribute next() {
            checkModCount();
            if (nextIndex < 0)
                throw new NoSuchElementException();
            Attribute att = atts[nextIndex];
            toRemoveIndex = nextIndex;
            moveToNextUsed();
            return att;
        }

        public void remove() {
            if (toRemoveIndex < 0)
                throw new IllegalStateException();
            checkModCount();
            atts[toRemoveIndex] = REMOVED;
            --size;
            toRemoveIndex = -1;
            expectedModCount = modCount;
        }

        private void checkModCount() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    @NotNull
    public Iterator<Attribute> iterator() {
        return new Iter();
    }

}
