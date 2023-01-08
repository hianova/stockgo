package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class checksyn {

  public String UrlToName(String in) {
    var url = in.replaceAll("(\\.\\w+|@num|@date|\\?.+)", "");
    var tmp = url.split("/");
    var out = tmp[tmp.length - 2] + "_" + tmp[tmp.length - 1];
    return out;
  }

  public String getTag(String in) throws Exception {
    var file = new ObjectMapper().readTree(
        new File(System.getProperty("user.dir") + File.separator + "parse_rule.txt"));
    var out = file.at("/" + in).textValue();
    return out;
  }

  public ArrayList<String> getNum(String in) throws Exception {
    ArrayList<String> out;

    switch (in) {
      case "stock" -> out = getStock_num();
      case "ETF" -> out = getETF_num();
      default -> out = new ArrayList<>();
    }
    return out;
  }

  public ArrayList<String> getStock_num() throws Exception {//beta
    var out = new ArrayList<String>();
    var pattern = Pattern.compile("^[0-9]{4}　");

    new data(getDownloads_dir() + "上市證券代號" + File.separator + "isin_C_public.txt",
        new ArrayList<>(List.of("有價證券代號及名稱"))).getData().forEach((tmp) -> {
      if (pattern.matcher(tmp).find()) {
        out.add(tmp.split("　")[0]);
      }
    });
    new data(getDownloads_dir() + "上櫃證券代號" + File.separator + "isin_C_public.txt",
        new ArrayList<>(List.of("有價證券代號及名稱"))).getData().forEach((tmp) -> {
      if (pattern.matcher(tmp).find()) {
        out.add(tmp.split("　")[0]);
      }
    });
    return out;
  }

  public ArrayList<String> getETF_num() throws Exception {//beta
    var out = new ArrayList<String>();
    var pattern = Pattern.compile("^T[0-9]+\\w");

    new data(getDownloads_dir() + "基金＿國際證券代號" + File.separator + "isin_C_public.txt",
        new ArrayList<>(List.of("有價證券代號及名稱"))).getData().forEach((tmp) -> {
      if (pattern.matcher(tmp).find()) {
        out.add(tmp.split("　")[0]);
      }
    });
    return out;
  }

  public String getDownloads_dir() {
    var out = System.getProperty("user.dir") + File.separator + "downloads" + File.separator;
    return out;
  }

  public String getStrategy_dir() {
    var out = System.getProperty("user.dir") + File.separator + "strategy" + File.separator;
    return out;
  }

  public String getUA() throws Exception {
    var file_in = new FileInputStream(
        System.getProperty("user.dir") + File.separator + "useragent.txt");
    var UA = new ArrayList<>(List.of(new String(file_in.readAllBytes()).split("\n")));
    var out = UA.get(new Random().nextInt(UA.size()));
    return out;
  }

  public DateTimeFormatter getUni_date() {
    var out = DateTimeFormatter.ofPattern("yyyyMMdd");
    return out;
  }

  public Elements clean_html(Document in) {//beta
    var out = new Elements();

    in.select("tr").forEach((tmp) -> {
      if (tmp.children().select("table").size() == 0 && tmp.parent().select("table").size() == 0) {
        tmp.select("br").forEach(Node::remove);
        out.add(tmp);
      }
    });
    for (var row_count = 0; row_count < out.size(); row_count++) {
      var row = out.get(row_count);
      for (var cell_count = 0; cell_count < row.childrenSize(); cell_count++) {
        var cell = row.children().get(cell_count);
        if (cell.hasAttr("colspan")) {
          var colspan = Integer.parseInt(cell.attr("colspan"));
          for (int count = 1; count < colspan; count++) {
            row.insertChildren(cell_count + count, cell.clone().removeAttr("colspan"));
          }
        }
        if (cell.hasAttr("rowspan")) {
          var span_count = Integer.parseInt(cell.attr("rowspan"));
          for (var count = 1; count < span_count; count++) {
            out.get(row_count + count)
                .insertChildren(cell_count, cell.clone().removeAttr("rowspan"));
          }
        }
      }
    }
    return out;
  }
}