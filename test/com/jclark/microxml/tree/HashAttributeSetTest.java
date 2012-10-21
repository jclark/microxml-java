package com.jclark.microxml.tree;

import org.testng.annotations.Test;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.testng.Assert.*;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class HashAttributeSetTest {

    static final int N = 10000;

    HashAttributeSet createLarge() {
        HashAttributeSet atts = new HashAttributeSet();
        atts.selfCheck();
        for (int i = 0; i < N; i++) {
            atts.add(new Attribute("a" + i, String.valueOf(i)));
            atts.selfCheck();
        }
        return atts;
    }

    @Test
    public void testLarge() throws Exception {
        HashAttributeSet atts = createLarge();
        assert(atts.size() == N);
        HashAttributeSet atts2 = atts.clone();
        assertEquals(atts, atts2);
        assertEquals(atts.hashCode(), atts2.hashCode());
        boolean[] found = new boolean[N];
        int count = 0;
        for (Attribute att : atts) {
            assertEquals(att.getName(), "a" + att.getValue());
            int n = Integer.parseInt(att.getValue());
            assertFalse(found[n]);
            found[n] = true;
            ++count;
        }
        assertEquals(count, N);
        for (int i = 0; i < N; i++) {
            String name = "a" + i;
            Attribute att = atts.remove(name);
            atts.selfCheck();
            assertNotNull(att);
            assertEquals(att.getValue(),String.valueOf(i));
            assertFalse(atts.equals(atts2));
        }
        assertTrue(atts.isEmpty());
        atts2.clear();
        atts2.selfCheck();
        assertEquals(atts, atts2);
        assertEquals(atts.hashCode(), atts2.hashCode());
    }

    @Test
    public void testIteratorRemove() throws Exception {
        HashAttributeSet atts = createLarge();
        Iterator<Attribute> iter = atts.iterator();
        while (iter.hasNext()) {
            String value = iter.next().getValue();
            // Remove the even attributes
            if ((Integer.parseInt(value) & 1) == 0)
                iter.remove();
            atts.selfCheck();
        }
        testRemovedEven(atts);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIteratorRemove1() throws Exception {
        new HashAttributeSet().iterator().remove();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIteratorRemove2() throws Exception {
        HashAttributeSet atts = new HashAttributeSet();
        atts.add(new Attribute("foo", "bar"));
        Iterator iter = atts.iterator();
        assertTrue(iter.hasNext());
        assertNotNull(iter.next());
        iter.remove();
        iter.remove();
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testIteratorNoSuchElement()throws Exception {
        HashAttributeSet atts = new HashAttributeSet();
        atts.iterator().next();
    }

    @Test(expectedExceptions = ConcurrentModificationException.class)
    public void testIteratorCoMod() throws Exception {
        HashAttributeSet atts = new HashAttributeSet();
        atts.add(new Attribute("foo", "0"));
        Iterator iter = atts.iterator();
        atts.add(new Attribute("bar", "1"));
        iter.next();
    }

    @Test
    public void testRemoveByName() throws Exception {
        HashAttributeSet atts = createLarge();
        assertNull(atts.remove("foo"));
        for (int i = 0; i < N; i += 2) {
            Attribute att = atts.remove("a" + i);
            assertNotNull(att);
            assertEquals(att.getName(), "a" + i);
        }
        testRemovedEven(atts);
    }

    @Test
    public void testRemoveObject() throws Exception {
        HashAttributeSet atts = createLarge();
        assertFalse(atts.remove(new Attribute("foo", "bar")));
        assertFalse(atts.remove(new Attribute("a0", "")));
        for (int i = 0; i < N; i += 2) {
            Attribute target = new Attribute("a" + i, String.valueOf(i));
            assertTrue(atts.remove(target));
        }
        testRemovedEven(atts);
    }

    private void testRemovedEven(HashAttributeSet atts) {
        assertEquals(atts.size(), N / 2);
        for (int i = 0; i < N; i++) {
            String value = atts.getValue("a" + i);
            if ((i & 1) == 0)
                assertNull(value);
            else
                assertNotNull(value);
        }
    }

    @Test
    public void testGet() throws Exception {
        HashAttributeSet atts = createLarge();
        for (int i = 0; i < N; i++) {
            String name = "a" + i;
            Attribute att = atts.get(name);
            assertNotNull(att);
            assertEquals(att.getName(), name);
            assertEquals(Integer.parseInt(att.getValue()), i);
            assertEquals(atts.getValue(name), att.getValue());
        }
        assertNull(atts.getValue("foo"));
        assertNull(atts.get("foo"));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testGet1() throws Exception {
        new HashAttributeSet().get(null);
    }

    @Test
    public void testToArray() throws Exception {
        Attribute[] atts = createLarge().toArray(new Attribute[0]);
        boolean[] found = new boolean[N];
        assertEquals(atts.length, N);
        for (Attribute att : atts) {
            assertEquals(att.getName(), "a" + att.getValue());
            int n = Integer.parseInt(att.getValue());
            assertFalse(found[n]);
            found[n] = true;
        }
    }

    @Test
    public void testDuplicate1() throws Exception {
        HashAttributeSet atts = new HashAttributeSet();
        assertTrue(atts.add(new Attribute("foo", "bar")));
        assertNotNull(atts.remove("foo"));
        assertTrue(atts.add(new Attribute("foo", "bar")));
        assertFalse(atts.add(new Attribute("foo", "bar")));
        atts.selfCheck();
    }

    @Test(expectedExceptions = DuplicateAttributeException.class)
    public void testDuplicate2() throws Exception {
        HashAttributeSet atts = new HashAttributeSet();
        assertTrue(atts.add(new Attribute("foo", "bar")));
        atts.add(new Attribute("foo", "baz"));
    }
}
