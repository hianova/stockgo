package hianova.stockgo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class ParseTest {
    @Test
    void testData() {
        String pathIn = "path/to/file";
        ArrayList<String> reqIn = new ArrayList<>();
        reqIn.add("column1#tag1");
        reqIn.add("column2");

        assertDoesNotThrow(() -> {
            Parse parse = new Parse(pathIn, reqIn);
            assertNotNull(parse);
        });
    }
}
