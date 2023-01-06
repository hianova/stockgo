package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class data {

  private final ArrayList<Integer> request;
  private final boolean has_tag;
  private final ArrayList<String>[] req_tag;
  private boolean is_html;
  private Elements head_html, body_html;
  private JsonNode head_json, body_json;

  public data(String in, ArrayList<String> request_in) throws Exception {
    var file = new String(new FileInputStream(in).readAllBytes(), StandardCharsets.UTF_8);
    request = new ArrayList<>();
    has_tag = request_in.contains("#");
    req_tag = new ArrayList[request_in.size()];

    if (file.contains("<body")) {
      var check = new checksyn();
      var tag = Jsoup.parse(file).select("tag").text();
      var page = check.clean_html(Jsoup.parse(file));
      is_html = true;
      head_html = page.select(check.getTag(tag + "/head")).first().children();
      body_html = page.select(check.getTag(tag + "/body"));

      if (in.contains("etfDiv")) {
        head_html = page.select("thead>tr>th");
      }
      request_in.forEach((req_tmp) -> {
        var tmp = req_tmp.split("#")[0];
        if (tmp.contains("ALL")) {
          for (var count = 0; count < head_html.size(); count++) {
            request.add(count);
          }
          return;
        }
        for (var count = 0; count < head_html.size(); count++) {
          if (tmp.equals(head_html.get(count).text())) {
            request.add(count);
            return;
          }
        }
      });
    } else {
      var json = new ObjectMapper().readTree(file);
      head_json = json.at("/fields");
      body_json = json.at("/data");
      request_in.forEach((req_tmp) -> {
        var tmp = req_tmp.split("#")[0];
        if (tmp.contains("ALL")) {
          for (var count = 0; count < head_json.size(); count++) {
            request.add(count);
          }
          return;
        }
        for (var count = 0; count < head_json.size(); count++) {
          if (tmp.equals(head_json.get(count).textValue())) {
            request.add(count);
            return;
          }
        }
      });
    }
    request_in.forEach((req_tmp) -> {
      if (has_tag) {
        req_tag[request_in.indexOf(req_tmp)] = new ArrayList<>();
        if (req_tmp.contains("#")) {
          Arrays.stream(req_tmp.split("#")).iterator()
              .forEachRemaining((tmp) -> req_tag[request_in.indexOf(tmp)].add(tmp));
        } else {
          req_tag[request_in.indexOf(req_tmp)].add("");
        }
      }
    });
  }

  public ArrayList<String> getData() {
    var out = new ArrayList<String>();

    if (is_html) {
      body_html.forEach((body_tmp) -> {
        var line = new ArrayList<String>();
        var line_check = true;

        request.forEach((request_tmp) -> {
          if (body_tmp.childrenSize() < request.size()) {
            return;
          }
          var tmp = body_tmp.child(request_tmp).text();
          line.add(tmp.isEmpty() ? "null" : tmp);
        });
        if (has_tag) {
          for (var count = 0; count < req_tag.length; count++) {
            for (var count_tag = 0; count_tag < req_tag[count].size(); count_tag++) {
              if (req_tag[count].get(count_tag).equals(line.get(count))) {
                out.addAll(line);
                return;
              }
            }
          }
          line_check = false;
        }
        if (line_check) {
          out.addAll(line);
        }
      });
    } else {
      body_json.forEach((body_tmp) -> {
        var line = new ArrayList<String>();
        var line_check = true;

        request.forEach((request_tmp) -> {
          var tmp = body_tmp.get(request_tmp).textValue();
          line.add(tmp.isEmpty() ? "null" : tmp);
        });
        if (has_tag) {
          for (var count = 0; count < req_tag.length; count++) {
            for (var count_tag = 0; count_tag < req_tag[count].size(); count_tag++) {
              if (req_tag[count].get(count_tag).equals(line.get(count))) {
                out.addAll(line);
                return;
              }
            }
          }
          line_check = false;
        }
        if (line_check) {
          out.addAll(line);
        }
      });
    }
    return out;
  }

  public ArrayList<String> getHead() {
    var out = new ArrayList<String>();
    if (is_html) {
      request.forEach((tmp) -> out.add(head_html.get(tmp).text()));
    } else {
      request.forEach((tmp) -> out.add(head_json.get(tmp).textValue()));
    }
    return out;
  }
}