package hianova.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
 
public class Crawl {

  private final Check check;
  private final HttpURLConnection trans;
  private final String tag;
  private final String encode;
  private String path;

  public Crawl(String URLIN) throws Exception {
    check = new Check();
    var URL = new URL(URLIN.replace("https", "http"));
    trans = (HttpURLConnection) URL.openConnection();
    tag = URL.getHost().replace(".", "_");
    encode = check.tag(tag + "/encode");
    path = check.downloadsDir() + check.URLToName(URL.toString()) + ".txt";

    trans.setRequestProperty("Origin", URL.toString());
    trans.setRequestProperty("Referer", URL.toString());
    trans.setRequestProperty("Host", URL.getHost());
    trans.setRequestProperty("Accept", "*/*");
    trans.setInstanceFollowRedirects(true);
    trans.setRequestProperty("Connection", "keep-alive");
    trans.setRequestProperty("User-Agent", check.UA());
    trans.setRequestProperty("X-Requested-With", "XMLHttpRequest");
    trans.setRequestProperty("Accept-Language", "zh-TW,zh-Hant;q=0.9");
    trans.setRequestProperty("Content-Type",
        "application/x-www-form-urlencoded; charset=" + encode);
  }

  public void setPost(String formIn) throws Exception {
    var removePat = Pattern.compile("\n");
    var replacePat = Pattern.compile(" ");
    trans.setDoOutput(true);
    trans.setRequestMethod("POST");
    trans.getOutputStream().write(removePat.matcher(
        replacePat.matcher(formIn).replaceAll("")).replaceAll("&").getBytes());
  }

  public void setPath(String pathIn) {
    path = pathIn;
  }

  public void save() throws Exception {
    var filePath = Paths.get(path);
    var file = new String(trans.getInputStream().readAllBytes(), encode);

    if (check.isHTML(file)) {
      file = file + "<tag>" + tag + "</tag>";
    } else if (check.isJSON(file)) {
      var tmp = (ObjectNode) new ObjectMapper().readTree(file);
      tmp.put("tag", tag);
      file = tmp.toPrettyString();
    }
    Files.createFile(filePath);
    Files.write(filePath, file.getBytes(StandardCharsets.UTF_8));
    trans.disconnect();
    System.out.println(path + " added");
  }
}