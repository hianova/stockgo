package hianova.stockgo;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

public class CheckTest extends Check {
    @Test
    void testETFNum() throws Exception {
        var expected = "00631L";
        var tmp = ETFNum();
        assertTrue(tmp.contains(expected));
    }

    @Test
    void testUA() throws Exception {
        var tmp = UA();
        assertFalse(tmp.isBlank());
    }

    @Test
    void testURLToName() {
        // Test case 1: simple URL with no query parameters
        String url1 = "https://example.com/path/to/page";
        String expected1 = "page";
        assertEquals(expected1, URLToName(url1));

        // Test case 2: URL with query parameters
        String url2 = "https://example.com/path/to/page?param1=value1&param2=value2";
        String expected2 = "page";
        assertEquals(expected2, URLToName(url2));

        // Test case 3: URL with trailing slash
        String url3 = "https://example.com/path/to/";
        String expected3 = "to_";
        assertEquals(expected3, URLToName(url3));

        // Test case 4: URL with special characters in path
        String url4 = "https://example.com/path/to/My%20Page%402021-02-17.html";
        String expected4 = "My_Page";
        assertEquals(expected4, URLToName(url4));
    }

    @Test
    void testCleanHTML() {
        String inputHTML = "<html><body><table><tr><td>cell1</td><td rowspan=\"2\">cell2</td></tr><tr><td>cell3</td></tr></table></body></html>";
        Element expectedOutput = Jsoup.parse(
                "<html><head></head><body><table><tbody><tr><td>cell1</td><td>cell2</td></tr><tr><td>cell3</td><td>cell2</td></tr></tbody></table></body></html>")
                .select("html").first();

        Element actualOutput = cleanHTML(inputHTML);

        assertEquals(expectedOutput.outerHtml(), actualOutput.outerHtml());
        String inputHTML2 = "not valid html";
        Element expectedOutput2 = new Element("html");

        Element actualOutput2 = cleanHTML(inputHTML2);

        assertEquals(expectedOutput2.outerHtml(), actualOutput2.outerHtml());

    }

    @Test
    void testDownloadsDir() {

        String expected = System.getProperty("user.dir") + File.separator + "downloads" + File.separator;
        assertEquals(expected, downloadsDir());
    }

    @Test
    void testIsHTML() {
        String VALID_HTML = "<html><body><h1>Hello World!</h1></body></html>";
        String INVALID_HTML = "Hello World!";
        boolean result = isHTML(VALID_HTML);
        assertTrue(result);
        boolean result2 = isHTML(INVALID_HTML);
        assertFalse(result2);
    }

    @Test
    void testIsJSON() {
        String VALID_JSON = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
        String INVALID_JSON = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"},";
        boolean result = isJSON(VALID_JSON);
        assertTrue(result);
        boolean result2 = isJSON(INVALID_JSON);
        assertFalse(result2);
    }

    @Test
    void testNum() throws Exception {
        var expected = "00631L";
        var tmp = num("ETF");
        assertTrue(tmp.contains(expected));

        String expectedOutput = "2330";
        ArrayList<String> actualOutput = num("stock:listed");

        assertTrue(actualOutput.contains(expectedOutput));
        String expectedOutput2 = "4968";
        ArrayList<String> actualOutput2 = num("stock:OTC");

        assertTrue(actualOutput2.contains(expectedOutput2));
        String expectedOutput3 = "2330";
        String expectedOutput4 = "4968";
        ArrayList<String> actualOutput3 = num("stock");

        assertTrue(actualOutput3.contains(expectedOutput3));
        assertTrue(actualOutput3.contains(expectedOutput4));

        var unexpected = "unknown";
        var tmp2 = num(unexpected);
        assertTrue(tmp2.size() == 0);

    }

    @Test
    void testRelay() {

    }

    @Test
    void testStockNum() throws Exception {
        String expectedOutput = "2330";
        ArrayList<String> actualOutput = stockNum("listed");

        assertTrue(actualOutput.contains(expectedOutput));
        String expectedOutput2 = "4968";
        ArrayList<String> actualOutput2 = stockNum("OTC");

        assertTrue(actualOutput2.contains(expectedOutput2));
        String expectedOutput3 = "2330";
        String expectedOutput4 = "4968";
        ArrayList<String> actualOutput3 = stockNum("");

        assertTrue(actualOutput3.contains(expectedOutput3));
        assertTrue(actualOutput3.contains(expectedOutput4));

    }

    @Test
    void testStrategyDir() {
        String expected = System.getProperty("user.dir") + File.separator + "strategy" + File.separator;
        assertEquals(expected, strategyDir());
    }

    @Test
    void testTag() {

    }
}
