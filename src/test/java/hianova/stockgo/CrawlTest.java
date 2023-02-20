package hianova.stockgo;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CrawlTest {
    @Test
    void testSave() throws Exception {
        String url = "https://example.com";
        Crawl crawl = new Crawl(url);
        crawl.save();
        File file = new File("");
        Assertions.assertTrue(file.exists());
    }

    @Test
    void testSetPath() throws Exception {
        String url = "https://example.com";
        Crawl crawl = new Crawl(url);
        String newPath = "new/path/to/file.txt";
        crawl.setPath(newPath);
        Assertions.assertTrue(new File(newPath).exists());
    }

    @Test
    void testSetPost() throws Exception {
        String url = "https://example.com";
        Crawl crawl = new Crawl(url);
        String form = "param1=value1&param2=value2";
        crawl.setPost(form);
        Assertions.assertTrue(new File("").exists());
    }
}
