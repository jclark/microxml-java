package com.jclark.microxml.tree;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Test correct handling of name characters.
 */
public class NamingTest {

    static final int[] forbiddenRanges = {
            0x0, 0x1F,
            0x7F, 0x9F,
            0xD800, 0xDFFF,
            0xFDD0, 0xFDEF,
            0xFFFE, 0xFFFF,
            0x1FFFE, 0x1FFFF,
            0x2FFFE, 0x2FFFF,
            0x3FFFE, 0x3FFFF,
            0x4FFFE, 0x4FFFF,
            0x5FFFE, 0x5FFFF,
            0x6FFFE, 0x6FFFF,
            0x7FFFE, 0x7FFFF,
            0x8FFFE, 0x8FFFF,
            0x9FFFE, 0x9FFFF,
            0xAFFFE, 0xAFFFF,
            0xBFFFE, 0xBFFFF,
            0xCFFFE, 0xCFFFF,
            0xDFFFE, 0xDFFFF,
            0xEFFFE, 0xEFFFF,
            0xFFFFE, 0xFFFFF,
            0x10FFFE, 0x10FFFF
    };

    static final int[] nameStartRanges = {
            'A', 'Z',
            'a', 'z',
            '_', '_',
            0xC0, 0xD6,
            0xD8, 0xF6,
            0xF8, 0x2FF,
            0x370, 0x37D,
            0x37F, 0x1FFF,
            0x200C, 0x200D,
            0x2070, 0x218F,
            0x2C00, 0x2FEF,
            0x3001, 0xD7FF,
            0xF900, 0xEFFFF
    };

    static final int[] nameRanges = {
            '0', '9',
            '-', '-',
            '.', '.',
            0xB7, 0xB7,
            0x0300, 0x036F,
            0x203F, 0x2040
    };

    boolean inRange(int ch, int[] ranges) {
        for (int i = 0; i < ranges.length; i += 2)
            if (ranges[i] <= ch && ch <= ranges[i + 1])
                return true;
        return false;
    }

    @Test
    public void testNameStart() throws Exception {
        for (int cp = 0; cp <= 0x10FFFF; cp++) {
            String name = new String(Character.toChars(cp));
            String message = "name start #x" + Integer.toHexString(cp);
            if (inRange(cp, nameStartRanges) && !inRange(cp, forbiddenRanges)) {
                try {
                    String doc = "<" + name + " " + name + "=\"val\"/>";
                    Element element = MicroXML.parse(doc);
                    assertEquals(element.getName(), name, "Incorrect name with " + message);
                    assertEquals(element.attributes().getValue(name), "val");
                }
                catch (ParseException e) {
                    fail("Valid " + message + " not accepted");
                }
            }
            else {
                try {
                    String doc = "<" + name + "/>";
                    MicroXML.parse(doc);
                    fail("Invalid " + message);
                }
                catch (ParseException e) {
                }
                try {
                    String doc = "<x " + name + "=\"val\"/>";
                    MicroXML.parse(doc);
                    fail("Invalid " + message);
                }
                catch (ParseException e) {
                }
            }
        }
    }

    @Test
    public void testNameChar() throws Exception {
        for (int cp = 0; cp <= 0x10FFFF; cp++) {
            String ch = new String(Character.toChars(cp));
            String message = "name char #x" + Integer.toHexString(cp);
            if (inRange(cp, nameRanges)) {
                try {
                    String name = "x" + ch;
                    String doc = "<" + name + "/>";
                    Element element = MicroXML.parse(doc);
                    assertEquals(element.getName(), name, "Incorrect name with " + message);
                }
                catch (ParseException e) {
                    fail("Valid " + message + " not accepted");
                }
                try {
                    MicroXML.parse("<" + ch + "/>");
                    fail("Valid " + message + " but not name start incorrectly accepted as name start");
                }
                catch (ParseException e) {
                }
            }
        }
    }

}
