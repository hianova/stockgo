package com.mycompany.stockgo;

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class manager extends config {

  public manager() throws Exception {
  }

  public void add(ArrayList<String> in) throws Exception {
    var url = in.get(0).replace(" ", "");

    if (label_url.contains(url)) {
      System.out.println("URL exist line: " + label_url.lastIndexOf(url));
      return;
    }
    label_url.add(url);
    label_title.add(in.get(1));
    label_folder.add(in.get(2).isBlank() ?
        check.UrlToName(url.split("@Post:")[0]) : in.get(2));
    label_tag.add(in.get(3));
    label_status.add(in.get(4).isBlank() ?
        LocalDate.now().minusYears(10).format(uni_date) : in.get(4));
    download(url);
    label_status.set(label_url.lastIndexOf(url), LocalDate.now().format(uni_date));
    sync_config();
    System.out.println(in.get(1) + " added");
  }

  public void delete(int in) throws Exception {
    label_url.remove(in);
    label_title.remove(in);
    label_folder.remove(in);
    label_tag.remove(in);
    label_status.remove(in);
    sync_config();
    System.out.println(in + "line deleted");
  }

  public void update() {
    for (var count = 0; count < label_status.size(); count++) {
      if (Period.between(LocalDate.parse(label_status.get(count), uni_date),
          LocalDate.now()).getDays() > 1) {
        try {
          download(label_url.get(count));
          label_status.set(count, LocalDate.now().format(uni_date));
          sync_config();
        } catch (Exception e) {
          System.out.println("update has suspend " + e);
        }
      }
    }
    System.out.println("files are updated");
  }

  public void download(String in) throws Exception {
    var dir = downloads_dir + label_folder.get(label_url.lastIndexOf(in)) +
        System.getProperty("file.separator");

    new File(dir).mkdir();
    batch_num(in, "").forEach((num) -> {
      try {
        batch_time(in, "").forEach((time) -> {
          try {
            var url = in.replaceAll("@date", toOri_date_form(time, in))
                .replaceAll("@num", num).split("@Post:");
            var crawl = new crawl(url[0]);
            var path_tmp = dir + check.UrlToName(url[0]);
            if (in.contains("@num")) {
              path_tmp = path_tmp.concat("_" + num);
            }
            if (in.contains("@date")) {
              path_tmp = path_tmp.concat("_" + time);
            }
            if (in.contains("@Post")) {
              crawl.set_Post(url[1]);
            }
            crawl.setPath(path_tmp + ".txt");
            crawl.start();
            Thread.sleep((long) (Math.random() * 5000));
          } catch (Exception e) {
            System.out.println("time iterator stopped " + e);
          }
        });
      } catch (Exception e) {
        System.out.println("number iterator stopped " + e);
      }
    });
  }
}