package com.jclark.microxml.tree;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class LocatedElementTest {
    @Test
    public void testOffsets() throws Exception {
        LineMap lineMap = new LineMap("foo.xml");
        TreeBuilder.LocatedElement elem = new TreeBuilder.LocatedElement("foo", 0, lineMap);
        elem.setStartTagCloseOffset(5);
        elem.noteComment(10);
        // assertEquals(elem.getTextLocation(0, 0, 0).getStartIndex(), 5);
        elem.append("Hello!");

        elem.append('\n');
        elem.noteIgnoredLf();
        Location loc = elem.getTextLocation(0, 0, 6);
        assertEquals(loc.getStartIndex(), 5 + 10);
        assertEquals(loc.getEndIndex(), 5 + 10 + 6);
        loc = elem.getTextLocation(0, 0, 7);
        assertEquals(loc.getStartIndex(), 5 + 10);
        assertEquals(loc.getEndIndex(), 5 + 10 + 6 + 2);
    }
}
