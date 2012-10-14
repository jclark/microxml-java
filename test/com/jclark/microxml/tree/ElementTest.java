package com.jclark.microxml.tree;

import org.testng.annotations.Test;

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
        ContentList con = e.content();
        assertEquals(con.size(), 0);
        assertEquals(con.getText(0), "");
        con.add("Hello");
        assertEquals(con.size(), 0);
        assertEquals(con.getText(0), "Hello");
        Element child = new Element("y");
        con.add(child);
        assertEquals(con.size(), 1);
        assertTrue(con.get(0) == child);
        assertTrue(child.getParent() == e);
        assertEquals(child.getIndexInParent(), 0);
        assertEquals(con.getText(0), "Hello");
        assertEquals(con.getText(1), "");
        con.setText(1, "World");
        assertEquals(con.getText(1), "World");
        con.clearText();
        assertEquals(con.getText(0), "");
        assertEquals(con.getText(1), "");
    }

    @Test
    public void testLarge() throws Exception {
        Element root = new Element("root");
        final int N = 100000;
        for (int i = 0; i < N; i++)
            root.content().add(new Element("x" + Integer.toString(i)));
        assertEquals(root.content().size(), N);
        for (int i = 0; i < N; i++)
            assertEquals(root.content().get(i).getName(),
                         "x" + Integer.toString(i));
    }

    @Test
    public void testRemove() throws Exception {
        Element root = new Element("root");
        ContentList con = root.content();
        final int N = 10;
        for (int i = 0; i < N; i++)
            con.add(new Element("x" + Integer.toString(i)));
        Element e5 = con.get(5);
        Element removed = con.remove(5);
        assertNull(removed.getParent());
        assertEquals(removed.getIndexInParent(), -1);
        assertEquals(con.size(), N - 1);
        assertTrue(removed == e5);
        for (int i = 5; i < N - 1; i++)
            assertEquals(con.get(i).getName(), "x" + Integer.toString(i + 1));
        for (int i = 0; i < 5; i++)
            assertEquals(con.get(i).getName(), "x" + Integer.toString(i));
    }
}
