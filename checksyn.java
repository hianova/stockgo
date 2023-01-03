package com.mycompany.stockgo;

import java.io.FileInputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.jsoup.select.Elements;

public class checksyn {

  private final Random random;
  private final Hashtable<String, String> tag;
  private final DateTimeFormatter uni_date;
  private final String downloads_dir, strategy_dir;
  private final ArrayList<String> num_stock, num_ETF;

  public checksyn() throws Exception {
    random = new Random();
    tag = new Hashtable<>();
    uni_date = DateTimeFormatter.ofPattern("yyyyMMdd");
    downloads_dir =
        System.getProperty("user.dir") + System.getProperty("file.separator") + "downloads"
            + System.getProperty("file.separator");
    strategy_dir =
        System.getProperty("user.dir") + System.getProperty("file.separator") + "strategy"
            + System.getProperty("file.separator");
    num_stock = new ArrayList<>();
    num_ETF = new ArrayList<>();

    var parse_rule = new String(new FileInputStream(
        System.getProperty("user.dir") + System.getProperty("file.separator")
            + "parse_rule.txt").readAllBytes()).replace("\"", "").split("\n");
    for (var tmp : parse_rule) {
      tag.put(tmp.split(":")[0], tmp.split(":")[1]);
    }
  }

  public String UrlToName(String in) throws Exception {
    var out = "";
    var list = new String[]{"\\.\\w+", "_@num", "_@date", "@num", "@date"};
    var url = new URL(in).getPath();
    for (var list_tmp : list) {
      url = url.replaceAll(list_tmp, "");
    }
    var tmp = url.split("/");
    out = tmp[tmp.length - 2] + "_" + tmp[tmp.length - 1];
    return out;
  }

  public String getTag(String in) {
    var out = tag.get(in);
    return out;
  }

  public ArrayList<String> getNum(String in) throws Exception {
    var out = new ArrayList<String>();

    switch (in) {
      case "stock" -> out.addAll(new checksyn().getStock_num());
      case "ETF" -> out.addAll(new checksyn().getETF_num());
    }
    return out;
  }

  public ArrayList<String> getStock_num() throws Exception {
    var out = new ArrayList<String>();
    var pattern = Pattern.compile("^[0-9]{4}　");

    if (num_stock.isEmpty()) {
      new data(downloads_dir + "上市證券代號" + System.getProperty("file.separator")
          + "isin_C_public.txt", new ArrayList<>(List.of("有價證券代號及名稱"))).getData()
          .forEach((tmp) -> {
            if (pattern.matcher(tmp).find()) {
              num_stock.add(tmp.split("　")[0]);
            }
          });
      new data(downloads_dir + "上櫃證券代號" + System.getProperty("file.separator")
          + "isin_C_public.txt", new ArrayList<>(List.of("有價證券代號及名稱"))).getData()
          .forEach((tmp) -> {
            if (pattern.matcher(tmp).find()) {
              num_stock.add(tmp.split("　")[0]);
            }
          });
    }
    out = num_stock;
    return out;
  }

  public ArrayList<String> getETF_num() throws Exception {
    var out = new ArrayList<String>();
    var pattern = Pattern.compile("^T[0-9]+\\w");

    if (num_ETF.isEmpty()) {
      new data(downloads_dir + "基金＿國際證券代號" + System.getProperty("file.separator")
          + "isin_C_public.txt", new ArrayList<>(List.of("有價證券代號及名稱"))).getData()
          .forEach((tmp) -> {
            if (pattern.matcher(tmp).find()) {
              num_ETF.add(tmp.split("　")[0]);
            }
          });
    }
    out = num_ETF;
    return out;
  }

  public String getDownloads_dir() {
    var out = downloads_dir;
    return out;
  }

  public String getStrategy_dir() {
    var out = strategy_dir;
    return out;
  }

  public String getUA() throws Exception {
    var out = "";
    var file_in = new FileInputStream(
        System.getProperty("user.dir") + System.getProperty("file.separator") + "useragent.txt");
    var UA = new ArrayList<>(Arrays.asList(new String(file_in.readAllBytes()).split("\n")));

    out = UA.get(random.nextInt(UA.size()));
    return out;
  }

  public DateTimeFormatter getUni_date() {
    var out = uni_date;
    return out;
  }

  public Elements clean_rowspan(Elements in) {
    Elements out = in;

    out.forEach((row) -> {
      row.children().forEach((cell) -> {
        if (cell.hasAttr("rowspan")) {
          var span_count = Integer.parseInt(cell.attr("rowspan"));
          for (var count = 1; count < span_count; count++) {
            out.get(row.elementSiblingIndex() + count)
                .insertChildren(cell.elementSiblingIndex(), cell.clone().removeAttr("rowspan"));
          }
        }
      });
    });
    return out;
  }
}