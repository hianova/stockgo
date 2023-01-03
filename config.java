package com.mycompany.stockgo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class config {

  protected checksyn check;
  protected String downloads_dir, strategy_dir;
  protected DateTimeFormatter uni_date;
  protected ArrayList<String> label, label_url, label_title, label_folder, label_tag, label_status;

  public config() throws Exception {
    check = new checksyn();
    downloads_dir = check.getDownloads_dir();
    strategy_dir = check.getStrategy_dir();
    uni_date = check.getUni_date();
    label = new ArrayList<>();
    label_url = new ArrayList<>();
    label_title = new ArrayList<>();
    label_folder = new ArrayList<>();
    label_tag = new ArrayList<>();
    label_status = new ArrayList<>();
    var file_in = Arrays.stream(
        new String(new FileInputStream(downloads_dir + "config.txt").readAllBytes())
            .replace("\"", "").split("\n")).iterator();

    label.addAll(List.of(file_in.next().split(",")));
    for (var tmp = file_in.next(); file_in.hasNext(); tmp = file_in.next()) {
      label_url.add(tmp.split(",")[0]);
      label_title.add(tmp.split(",")[1]);
      label_folder.add(tmp.split(",")[2]);
      label_tag.add(tmp.split(",")[3]);
      label_status.add(tmp.split(",")[4]);
    }
  }

  public ArrayList<String> batch_time(String in, String st_ed) {
    if (!in.contains("@date")) {
      return new ArrayList<>(List.of("null"));
    }
    var out = new ArrayList<String>();
    var session = label_url.lastIndexOf(in);
    var tag_tmp = label_tag.get(session).split("@");

    for (var tmp : tag_tmp) {
      if (tmp.contains("date")) {
        tag_tmp = tmp.split(":");
        break;
      }
    }
    var is_DC = tag_tmp[1].contains("yyyy");
    var st_ed_tmp = (st_ed.matches("\\d+~\\d+") ? st_ed.split("~")
        : new String[]{label_status.get(session), LocalDate.now().format(uni_date)});
    for (var date_tmp = LocalDate.parse(st_ed_tmp[0], uni_date);
        date_tmp.isBefore(LocalDate.parse(st_ed_tmp[1], uni_date)); ) {
      var tmp = is_DC ? date_tmp : date_tmp.minusYears(1911);
      out.add(tmp.format(uni_date));
      switch (tag_tmp[2]) {
        case "Y" -> date_tmp = date_tmp.plusYears(1);
        case "M" -> date_tmp = date_tmp.plusMonths(1);
        case "W" -> date_tmp = date_tmp.plusWeeks(1);
        case "D" -> date_tmp = date_tmp.plusDays(1);
      }
    }
    return out;
  }

  public ArrayList<String> batch_num(String in, String select_num_in) throws Exception {
    if (!in.contains("@num")) {
      return new ArrayList<>(List.of("null"));
    }
    var out = new ArrayList<String>();
    var session = label_url.lastIndexOf(in);
    var tag_tmp = label_tag.get(session).split("@");

    for (var tmp : tag_tmp) {
      if (tmp.contains("num")) {
        tag_tmp = tmp.split(":");
        break;
      }
    }
    var select_num_tmp = select_num_in.isEmpty() ? check.getNum(tag_tmp[1])
        : (ArrayList<String>) Arrays.asList(select_num_in.split("\\."));
    out.addAll(select_num_tmp);
    return out;
  }

  public ArrayList<String> getConfig() {
    var out = new ArrayList<String>();
    out.add("序號");
    out.addAll(label);
    out.add("\n");
    for (var count = 0; count < label_url.size(); count++) {
      out.add(count + ".");
      out.add(label_url.get(count));
      out.add(label_title.get(count));
      out.add(label_folder.get(count));
      out.add(label_tag.get(count));
      out.add(label_status.get(count));
      out.add("\n");
    }
    return out;
  }


  public void sync_config() throws Exception {
    var output = new OutputStreamWriter(new FileOutputStream(downloads_dir + "config.txt"));

    label.forEach((tmp) -> {
      try {
        output.write("\"" + tmp + "\"" + (tmp.contains(label.get(label.size() - 1)) ? "\n" : ","));
      } catch (Exception e) {
        System.out.println("label can't output " + e);
      }
    });
    for (var count = 0; count < label_url.size(); count++) {
      output.write(
          "\"" + label_url.get(count) + "\"," + "\"" + label_title.get(count) + "\"," + "\""
              + label_folder.get(count) + "\"," + "\"" + label_tag.get(count) + "\"," + "\""
              + label_status.get(count) + "\"\n");
    }
    output.close();
  }

  public ArrayList<String> search_title(String in) {
    var out = new ArrayList<String>();
    var session = label_title.lastIndexOf(in);
    out.add(label_url.get(session));
    out.add(label_title.get(session));
    out.add(label_folder.get(session));
    out.add(label_tag.get(session));
    out.add(label_status.get(session));
    return out;
  }

  public String toOri_date_form(String in, String url) {
    if (in.contains("null")) {
      return in;
    }
    String out = "";
    var tag_tmp = label_tag.get(label_url.lastIndexOf(url)).split("@");
    var date = LocalDate.parse(in, uni_date);
    for (var tmp : tag_tmp) {
      if (tmp.contains("date")) {
        tag_tmp = tmp.split(":");
        break;
      }
    }
    out = date.format(DateTimeFormatter.ofPattern(tag_tmp[1]));
    return out;
  }
}
