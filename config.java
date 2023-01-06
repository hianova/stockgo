package com.mycompany.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class config {

  protected final checksyn check;
  protected final DateTimeFormatter uni_date;
  protected final ArrayList<String> label_title, label_URL, label_folder, label_tag, label_status;
  protected String downloads_dir, strategy_dir;

  public config() throws Exception {
    check = new checksyn();
    uni_date = check.getUni_date();
    label_title = new ArrayList<>();
    label_URL = new ArrayList<>();
    label_folder = new ArrayList<>();
    label_tag = new ArrayList<>();
    label_status = new ArrayList<>();
    strategy_dir = check.getStrategy_dir();
    downloads_dir = check.getDownloads_dir();
    strategy_dir = check.getStrategy_dir();
    downloads_dir = check.getDownloads_dir();

    new ObjectMapper().readTree(new File(downloads_dir + "config.txt")).fields()
        .forEachRemaining((tmp) -> {
          label_title.add(tmp.getKey());
          label_URL.add(tmp.getValue().get("URL").textValue());
          label_folder.add(tmp.getValue().get("folder").textValue());
          label_tag.add(tmp.getValue().get("tag").textValue());
          label_status.add(tmp.getValue().get("status").textValue());
        });
  }

  public ArrayList<String> batch_time(String url, String st_ed) {
    if (!url.contains("@date")) {
      return new ArrayList<>(List.of("null"));
    }
    var out = new ArrayList<String>();
    var session = label_URL.indexOf(url);
    var date_tag = Arrays.stream(label_tag.get(session).split("@"))
        .dropWhile((tmp) -> tmp.contains("date")).collect(Collectors.joining()).split(":");
    var is_DC = date_tag[1].contains("yyyy");
    var st_ed_tmp =
        st_ed.matches("\\d+~\\d+") ? new LocalDate[]{LocalDate.parse(st_ed.split("~")[0], uni_date),
            LocalDate.parse(st_ed.split("~")[1], uni_date)}
            : new LocalDate[]{LocalDate.parse(label_status.get(session), uni_date),
                LocalDate.now()};

    while (st_ed_tmp[0].isBefore(st_ed_tmp[1])) {
      var tmp = is_DC ? st_ed_tmp[0] : st_ed_tmp[0].minusYears(1911);
      out.add(tmp.format(uni_date));
      switch (date_tag[2]) {
        case "Y" -> st_ed_tmp[0] = st_ed_tmp[0].plusYears(1);
        case "M" -> st_ed_tmp[0] = st_ed_tmp[0].plusMonths(1);
        case "W" -> st_ed_tmp[0] = st_ed_tmp[0].plusWeeks(1);
        case "D" -> st_ed_tmp[0] = st_ed_tmp[0].plusDays(1);
      }
    }
    return out;
  }

  public ArrayList<String> batch_num(String url, String sel_num) throws Exception {
    if (!url.contains("@num")) {
      return new ArrayList<>(List.of("null"));
    }
    var session = label_URL.indexOf(url);
    var num_tag = Arrays.stream(label_tag.get(session).split("@"))
        .dropWhile((tmp) -> tmp.contains("num")).collect(Collectors.joining()).split(":");
    var out = new ArrayList<>(
        sel_num.isEmpty() ? check.getNum(num_tag[1]) : List.of(sel_num.split("\\.")));
    return out;
  }

  public ArrayList<String> getConfig() {
    var out = new ArrayList<>(List.of("number", "title", "URL", "folder", "tag", "status", "\n"));

    label_title.forEach((title) -> {
      var tmp = label_title.indexOf(title);
      out.addAll(List.of(String.valueOf(tmp), title, label_URL.get(tmp), label_folder.get(tmp),
          label_tag.get(tmp), label_status.get(tmp), "\n"));
    });
    return out;
  }

  public void sync_config() throws Exception {
    var file_out = new FileOutputStream(downloads_dir + "config.txt");
    var config = new ObjectMapper().createObjectNode();

    label_title.forEach((title) -> {
      var tmp = label_title.indexOf(title);
      var node = config.putObject(title);
      node.put("URL", label_URL.get(tmp));
      node.put("folder", label_folder.get(tmp));
      node.put("tag", label_tag.get(tmp));
      node.put("status", label_status.get(tmp));
    });
    file_out.write(config.toPrettyString().getBytes());
    file_out.close();
  }

  public String getOri_dateform(String date, String url) {
    if (date.contains("null")) {
      return date;
    }
    var date_tag = Arrays.stream(label_tag.get(label_URL.indexOf(url)).split("@"))
        .dropWhile((tmp) -> tmp.contains("num")).collect(Collectors.joining()).split(":");
    var out = LocalDate.parse(date, uni_date).format(DateTimeFormatter.ofPattern(date_tag[1]));
    return out;
  }
}
