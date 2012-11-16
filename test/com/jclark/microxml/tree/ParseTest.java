package com.jclark.microxml.tree;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author James Clark
 */
public class ParseTest {

    static private class CountingErrorHandler implements ErrorHandler {
        int count = 0;
        public void error(Location location, String message) throws ParseException {
            count++;
        }
    }

    @Test(dataProvider = "tests.json")
    public void testParse(String id, String source, Element expected, boolean isValid) throws Exception {
        CountingErrorHandler eh = new CountingErrorHandler();
        Element actual = MicroXML.parse(source, new ParseOptions(eh));
        if (expected != null)
            assertTrue(Element.equivalent(actual, expected));
        assertEquals(eh.count == 0, isValid);
    }

    @DataProvider(name = "tests.json")
    Object[][] provideTestsJson() throws Exception {
        return loadSuite("/tests.json");
    }

    Object[][] loadSuite(String resourceName) throws Exception {
        List<Object[]> tests = new ArrayList<Object[]>();
        InputStreamReader reader = new InputStreamReader(ParseTest.class.getResourceAsStream(resourceName),
                                                         Charset.forName("UTF-8"));
        JSONArray a = (JSONArray)JSONValue.parseWithException(reader);
        for (Object obj : a) {
            JSONObject testObj = (JSONObject)obj;
            Object[] test = new Object[4];
            test[0] = testObj.get("id");
            test[1] = testObj.get("source");
            JSONArray result = (JSONArray)testObj.get("result");
            test[2] = result == null ? null : toElement(result);
            test[3] = testObj.get("valid");
            if (test[3] == null)
                test[3] = result == null ? Boolean.FALSE : Boolean.TRUE;
            tests.add(test);
        }
        return tests.toArray(new Object[][]{});
    }

    static Element toElement(JSONArray a) {
        Element elem = new Element((String)a.get(0));
        JSONObject attrsObj = (JSONObject)a.get(1);
        for (Object obj : attrsObj.keySet()) {
            elem.add(new Attribute((String)obj,
                                   (String)attrsObj.get(obj)));

        }
        for (Object obj : (JSONArray)a.get(2)) {
            if (obj instanceof String)
                elem.append((String)obj);
            else
                elem.append(toElement((JSONArray)obj));
        }
        return elem;
    }
}
