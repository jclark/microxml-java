package com.jclark.microxml.tree;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author <a href="mailto:jjc@jclark.com">James Clark</a>
 */
public class LineMapTest {
    @DataProvider(name = "lines")
    Object[][] createLookupTests() {
        int[] lineOffsets = new int[] { 7, 9, 21, 36 };
        return new Object[][] {
                { lineOffsets, 21, 4, 1 },
                { lineOffsets, 0, 1, 1 },
                { lineOffsets, 7, 2, 1 },
                { lineOffsets, 37, 5, 2 },
                { lineOffsets, 36, 5, 1 },
                { lineOffsets, 2, 1, 3 },
                { lineOffsets, 15, 3, 7 }
        };
    }

    @Test(dataProvider = "lines")
    public void testLookup(int[] lineOffsets, int offset, int expectedLineNumber, int expectedColumnNumber) throws Exception {
        LineMap lineMap = new LineMap("");
        for (int lineOffset : lineOffsets)
            lineMap.addLineStart(lineOffset);
        LinePosition lp = lineMap.get(offset);
        assertEquals(lp.getLineNumber(), expectedLineNumber);
        assertEquals(lp.getColumnNumber(), expectedColumnNumber);
    }
}
