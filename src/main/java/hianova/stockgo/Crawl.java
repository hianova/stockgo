package hianova.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Crawl {

  private final Check check;
  private final HttpURLConnection trans;
  private final String tag;
  private final String encode;
  private String path;

  public Crawl(String URLIN) throws Exception {
    var URL = new URL(URLIN.replace("https", "http"));
    check = new Check();
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
    var tmp = formIn.replace(" ", "").replaceAll("\n", "&");

    trans.setDoOutput(true);
    trans.setRequestMethod("POST");
    trans.getOutputStream().write(tmp.getBytes());
  }

  public void setPath(String pathIn) {
    path = pathIn;
  }

  public void save() throws Exception {
    var output = new FileOutputStream(path);
    var file = new String(trans.getInputStream().readAllBytes(), encode);

    new File(path).createNewFile();
    if (file.contains("html")) {
      var tmp = file + "<tag>" + tag + "</tag>";
      output.write(tmp.getBytes("UTF-8"));
    } else if (check.isJSON(file)) {
      var tmp = (ObjectNode) new ObjectMapper().readTree(file);
      tmp.put("tag", tag);
      output.write(tmp.toPrettyString().getBytes("UTF-8"));
    } else {
      output.write(file.getBytes("UTF-8"));
    }
    output.close();
    trans.disconnect();
    System.out.println(path + " added");
  }
}
