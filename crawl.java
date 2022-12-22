package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class crawl extends Thread {

  private final checksyn check;
  private final String tag;
  private final HttpURLConnection trans;
  private File file;

  public crawl(String in) throws Exception {
    var url = new URL(in);
    check = new checksyn();
    tag = url.getHost().replace(".", "_");
    file = new File(check.getDownloads_dir() + check.UrlToName(in) + ".txt");
    trans = (HttpURLConnection) url.openConnection();

    trans.setRequestProperty("Referer", in);
    trans.setRequestProperty("User-Agent", check.getUA());
    trans.setRequestProperty("Origin", url.getHost());
    trans.setRequestProperty("Host", url.getHost());
    trans.setRequestProperty("Accept-Encoding", check.getTag(tag + "_encode"));
    trans.setRequestProperty("Accept",
        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    trans.setRequestProperty("Accept-Language", "zh-TW");
    trans.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    trans.setRequestProperty("Accept-Language", "h-TW,zh-Hant;q=0.9");
    trans.setRequestProperty("Connection", "keep-alive");
    trans.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
  }

  public void set_Post(String in) throws Exception {
    trans.setRequestMethod("POST");
    trans.setDoOutput(true);
    trans.getOutputStream()
        .write(in.getBytes());
  }

  public void setPath(String in) {
    file = new File(in);
  }

  public void save() throws Exception {
    var file_out = new OutputStreamWriter(new FileOutputStream(file),
        check.getTag(tag + "_encode"));
    var page = new String(trans.getInputStream().readAllBytes());

    file.createNewFile();
    if (page.contains("<body")) {
      var tmp = page + "<tag>" + tag + "</tag>";
      file_out.write(tmp);
    } else {
      var tmp = (ObjectNode) new ObjectMapper().readTree(page);
      tmp.put("tag", tag);
      file_out.write(tmp.toPrettyString());
    }
    file_out.close();
    trans.disconnect();
    System.out.print(file.getName() + " saved\n");
  }

  public void run() {
    try {
      save();
    } catch (Exception e) {
      System.out.println("crawling went wrong " + e);
    }
  }
}
