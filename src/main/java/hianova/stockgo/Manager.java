package hianova.stockgo;

import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class Manager extends Config {

  public Manager() throws Exception {
  }

  public void add(ArrayList<String> listIn) throws Exception {
    if (listIn.size() != 5) {
      System.out.println("Wrong syntax: " + listIn.size() + " != 5");
      return;
    }
    if (label_title.contains(listIn.get(0))) {
      System.out.println("Title exists at " + label_title.indexOf(listIn.get(0)));
      return;
    }
    label_title.add(listIn.get(0));
    label_URL.add(listIn.get(1));
    label_folder.add(listIn.get(2));
    label_tag.add(listIn.get(3));
    label_status.add(listIn.get(4));
    download(listIn.get(1));
    label_status.set(label_title.indexOf(listIn.get(0)),LocalDate.now().format(uni_date));
    syncConfig();
    System.out.println(listIn.get(0) + " added");
  }

  public void delete(int numIn) throws Exception {
    label_URL.remove(numIn);
    label_title.remove(numIn);
    label_folder.remove(numIn);
    label_tag.remove(numIn);
    label_status.remove(numIn);
    syncConfig();
    System.out.println(numIn + " line deleted");
  }

  public void update() {
    IntStream.range(0, label_status.size()).forEach(next -> {
      if (Period.between(LocalDate.parse(label_status.get(next), uni_date), LocalDate.now())
          .getDays() > 1) {
        try {
          download(label_URL.get(next));
          label_status.set(next, LocalDate.now().format(uni_date));
          syncConfig();
        } catch (Exception e) {
          System.out.println("Update has been suspended: " + e);
        }
      }
    });
    System.out.println("Files are updated");
  }

  public void download(String URLIn) throws Exception {
    var dir = downloads_dir + label_folder.get(label_URL.indexOf(URLIn)) + File.separator;

    new File(dir).mkdir();
    streamNum(URLIn, "").forEach(nextNum -> {
      try {
        streamDate(URLIn, "").forEach(nextDate -> {
          try {
            var url = URLIn.replace("@date", toOriDate(
                nextDate,URLIn))
                .replace("@num", nextNum).split("@Post:");
            var crawl = new Crawl(url[0]);
            var path = dir + check.URLToName(url[0]) + (URLIn.contains("@num") ? "_" + nextNum : "")
                + (URLIn.contains("@date") ? "_" + nextDate : "") + ".txt";
            if (URLIn.contains("@Post")) {
              crawl.setPost(url[1]);
            }
            crawl.setPath(path);
            crawl.save();
            Thread.sleep((long) (Math.random() * 5000));
          } catch (Exception e) {
            System.out.println("Time iterator stopped: " + e);
          }
        });
      } catch (Exception e) {
        System.out.println("Number iterator stopped: " + e);
      }
    });
  }
}
