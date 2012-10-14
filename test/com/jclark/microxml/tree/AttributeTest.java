package com.jclark.microxml.tree;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

    @Test(expectedExceptions = NullPointerException.class)
    public void testSetValueNull() throws Exception {
        new Attribute("foo", "bar").setValue(null);
    }

    @Test
    public void testSetValue() throws Exception {
        Attribute a = new Attribute("foo", "bar");
        a.setValue("baz");
        assertEquals(a.getValue(), "baz");
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(new Attribute("foo", "bar").equals(new Attribute("foo", "bar")));
        assertFalse(new Attribute("foo", "bar").equals(new Attribute("foofoo", "bar")));
        assertFalse(new Attribute("foo", "bar").equals(new Attribute("foo", "")));
    }
}
