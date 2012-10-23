package com.jclark.microxml.tree;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class AttributeTest {
    @Test
    public void test() throws Exception {
        Attribute a = new Attribute("foo", "hello");
        assertEquals(a.getName(), "foo");
        assertEquals(a.getValue(), "hello");
    }

    @Test(expectedExceptions =  NullPointerException.class)
    public void testConstructorNameNull() throws Exception {
        new Attribute(null, "value");
    }

    @Test(expectedExceptions =  NullPointerException.class)
    public void testConstructorValueNull() throws Exception {
        new Attribute("foo", null);
    }

    private Attribute create() {
        return new Attribute("foo", "bar");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testSetValueNull() throws Exception {
        create().setValue(null);
    }

    @Test
    public void testSetValue() throws Exception {
        Attribute a = create();
        a.setValue("baz");
        assertEquals(a.getValue(), "baz");
    }

    @Test
    public void testEquals() throws Exception {
        Attribute att = create();
        assertTrue(att.equals(create()));
        assertFalse(att.equals(new Attribute("foofoo", "bar")));
        assertFalse(att.equals(new Attribute("foo", "")));
        assertTrue(att.equals(att));
        assertFalse(att.equals(null));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(create().hashCode(), create().hashCode());
    }

    @Test
    public void testGetLocation() throws Exception {
        Attribute att = create();
        assertNull(att.getNameLocation());
        assertNull(att.getValueLocation(0, 3));
        assertNull(att.getValueLocation(0, 0));
        assertNull(att.getValueLocation(3, 3));
        assertNull(att.getValueLocation(1, 1));
        assertNull(att.getValueLocation(1, 2));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetValueLocation1() throws Exception {
        create().getValueLocation(-1, 0);
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetValueLocation2() throws Exception {
        create().getValueLocation(1, 0);
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void testGetValueLocation3() throws Exception {
        create().getValueLocation(0, 4);
    }

    @Test
    public void testClone() throws Exception {
        Attribute att1 = create();
        Attribute att2 = att1.clone();
        assertEquals(att1, att2);
        att2.setValue("baz");
        assertEquals(att1.getValue(), "bar");
        assertEquals(att2.getValue(), "baz");
    }
}
