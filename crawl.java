package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class crawl extends Thread {

  private final checksyn check;
  private final HttpURLConnection trans;
  private String tag, file;

  public crawl(String in) throws Exception {
    var url = new URL(in);
    check = new checksyn();
    trans = (HttpURLConnection) url.openConnection();
    tag = url.getHost().replace(".", "_");
    file = check.getDownloads_dir() + check.UrlToName(in) + ".txt";

    trans.setRequestProperty("Referer", in);
    trans.setRequestProperty("Host", url.getHost());
    trans.setRequestProperty("Accept", "text/html,*/*");
    trans.setRequestProperty("Connection", "keep-alive");
    trans.setRequestProperty("User-Agent", check.getUA());
    trans.setRequestProperty("Origin", "https://" + url.getHost());
    trans.setRequestProperty("X-Requested-With", "XMLHttpRequest");
    trans.setRequestProperty("Accept-Language", "zh-TW,zh-Hant;q=0.9");
    trans.setRequestProperty("Content-Type",
        "application/x-www-form-urlencoded; charset=" + check.getTag(tag + "/encode"));
  }

  public void set_Post(String in) throws Exception {
    trans.setDoOutput(true);
    trans.setRequestMethod("POST");
    trans.getOutputStream().write(in.getBytes());
  }

  public void setPath(String in) {
    file = in;
  }

  public void save() throws Exception {
    var file_out = new FileOutputStream(file);
    var page = new String(trans.getInputStream().readAllBytes(), check.getTag(tag + "/encode"));

    new File(file).createNewFile();
    if (page.contains("<body")) {
      var tmp = page + "<tag>" + tag + "</tag>";
      file_out.write(tmp.getBytes(StandardCharsets.UTF_8));
    } else {
      var tmp = (ObjectNode) new ObjectMapper().readTree(page);
      tmp.put("tag", tag);
      file_out.write(tmp.toPrettyString().getBytes(StandardCharsets.UTF_8));
    }
    file_out.close();
    trans.disconnect();
    System.out.print(file + " saved\n");
  }

  public void run() {
    try {
      save();
    } catch (Exception e) {
      System.out.println("crawling went wrong " + e);
    }
  }
}
