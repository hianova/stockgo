package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class data {

  private final ArrayList<Integer> request;
  private final ArrayList<String>[] req_tag;
  private final boolean has_tag;
  private boolean is_html;
  private Elements head_html, body_html;
  private JsonNode head_json, body_json;

  public data(String in, ArrayList<String> request_in) throws Exception {
    var file = new String(new FileInputStream(in).readAllBytes());
    request = new ArrayList<>();
    req_tag = new ArrayList[request_in.size()];
    has_tag = request_in.toString().contains("#");

    if (file.contains("<body")) {
      var tag = Jsoup.parse(new File(in), "UTF-8").select("tag").text();
      var check = new checksyn();
      var parse = check.clean_rowspan(
          Jsoup.parse(file, check.getTag(tag + "_encode")).select(check.getTag(tag)));
      head_html = parse.select(check.getTag(tag + "_head"));
      body_html = parse.select(check.getTag(tag + "_body"));
      is_html = true;

      if ("mops_twse_com_tw".equals(tag)) {
        head_html.add(new Element("th").text("備註"));
      }
      if (in.contains("etfDiv")) {
        head_html = parse.select("thead>tr>th");
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
          if (tmp.equals(head_html.get(count).text().replace(" ",""))) {
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
    request_in.forEach((tmp) -> {
      if (has_tag) {
        req_tag[request_in.indexOf(tmp)] = new ArrayList<>();
        if (tmp.contains("#")) {
          for (var count = 1; count < tmp.split("#").length; count++) {
            req_tag[request_in.indexOf(tmp)].add(tmp.split("#")[count]);
          }
        } else {
          req_tag[request_in.indexOf(tmp)].add("");
        }
      }
    });
  }

  public ArrayList<String> getData() {
    var out = new ArrayList<String>();
    var skip_list = Pattern.compile("(計)");

    if (is_html) {
      body_html.forEach((body_tmp) -> {
        if (skip_list.matcher(body_tmp.text()).find()) {
          return;
        }
        var line = new ArrayList<String>();
        var line_check = true;

        request.forEach((request_tmp) -> {
          var tmp = body_tmp.child(request_tmp).text();//.select("td").text();
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
        if (skip_list.matcher(body_tmp.toString()).find()) {
          return;
        }
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
      request.forEach((tmp) -> {
        out.add(head_html.get(tmp).text().replace(" ",""));
      });
    } else {
      request.forEach((tmp) -> {
        out.add(head_json.get(tmp).textValue());
      });
    }
    return out;
  }
}