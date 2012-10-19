package com.jclark.microxml.tree;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class ElementTest {

    @Test
    public void testSetName() throws Exception {
        Element e = new Element("foo");
        e.setName("bar");
        assertEquals(e.getName(), "bar");
    }

    @Test
    public void testBasic() throws Exception {
        Element e = new Element("x");
        assertNull(e.getParent());
        assertEquals(e.getIndexInParent(), -1);
        assertEquals(e.getName(), "x");
        assertFalse(e.hasChildren());
        assertEquals(e.getText(0), "");
        e.append("Hello");
        assertFalse(e.hasChildren());
        assertEquals(e.getText(0), "Hello");
        Element child = new Element("y");
        e.children().add(child);
        assertEquals(e.children().size(), 1);
        assertTrue(e.children().get(0) == child);
        assertTrue(child.getParent() == e);
        assertEquals(child.getIndexInParent(), 0);
        assertEquals(e.getText(0), "Hello");
        assertEquals(e.getText(1), "");
        e.setText(1, "World");
        assertEquals(e.getText(1), "World");
        e.clearText();
        assertEquals(e.getText(0), "");
        assertEquals(e.getText(1), "");
    }

    @Test
    public void testLarge() throws Exception {
        Element root = new Element("root");
        List<Element> children = root.children();
        final int N = 100000;
        for (int i = 0; i < N; i++)
            children.add(new Element("x" + Integer.toString(i)));
        assertEquals(root.children().size(), N);
        for (int i = 0; i < N; i++)
            assertEquals(children.get(i).getName(),
                         "x" + Integer.toString(i));
    }

    @Test
    public void testRemove() throws Exception {
        Element root = new Element("root");
        List<Element> children = root.children();
        final int N = 10;
        for (int i = 0; i < N; i++)
            children.add(new Element("x" + Integer.toString(i)));
        Element e5 = children.get(5);
        Element removed = children.remove(5);
        assertNull(removed.getParent());
        assertEquals(removed.getIndexInParent(), -1);
        assertEquals(children.size(), N - 1);
        assertTrue(removed == e5);
        for (int i = 5; i < N - 1; i++)
            assertEquals(children.get(i).getName(), "x" + Integer.toString(i + 1));
        for (int i = 0; i < 5; i++)
            assertEquals(children.get(i).getName(), "x" + Integer.toString(i));
    }
}
