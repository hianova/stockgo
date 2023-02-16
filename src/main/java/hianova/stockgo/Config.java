package hianova.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Config {

  protected final Check check;
  protected final DateTimeFormatter uni_date;
  protected final ArrayList<String> label_title, label_URL, label_folder, label_tag, label_status;
  protected final String strategy_dir, downloads_dir;

  public Config() throws Exception {
    check = new Check();
    uni_date = DateTimeFormatter.ofPattern("yyyyMMdd");
    label_title = new ArrayList<>();
    label_URL = new ArrayList<>();
    label_folder = new ArrayList<>();
    label_tag = new ArrayList<>();
    label_status = new ArrayList<>();
    strategy_dir = check.strategyDir();
    downloads_dir = check.downloadsDir();

    new ObjectMapper().readTree(new File(downloads_dir + "config.json")).fields()
        .forEachRemaining(next -> {
          label_title.add(next.getKey());
          label_URL.add(next.getValue().get("URL").textValue());
          label_folder.add(next.getValue().get("folder").textValue());
          label_tag.add(next.getValue().get("tag").textValue());
          label_status.add(next.getValue().get("status").textValue());
        });
  }

  public ArrayList<String> streamDate(String URLIn, String stEdIn) {
    if (!URLIn.contains("@date")) {
      return new ArrayList<>(List.of("null"));
    }
    var out = new ArrayList<String>();
    var num = label_URL.indexOf(URLIn);
    var dateTag = Arrays.stream(label_tag.get(num).split("@"))
        .dropWhile(next -> next.contains("date")).collect(Collectors.joining()).split(":");
    var assertDC = dateTag[1].contains("yyyy");
    var stEd = stEdIn.matches("\\d+~\\d+") ? new LocalDate[] {
        LocalDate.parse(stEdIn.split("~")[0], uni_date),
        LocalDate.parse(stEdIn.split("~")[1], uni_date) }
        : new LocalDate[] { LocalDate.parse(label_status.get(num), uni_date), LocalDate.now() };

    while (stEd[0].isBefore(stEd[1])) {
      var tmp = assertDC ? stEd[0] : stEd[0].minusYears(1911);
      out.add(tmp.format(uni_date));
      switch (dateTag[2]) {
        case "Y" -> stEd[0] = stEd[0].plusYears(1);
        case "M" -> stEd[0] = stEd[0].plusMonths(1);
        case "W" -> stEd[0] = stEd[0].plusWeeks(1);
        case "D" -> stEd[0] = stEd[0].plusDays(1);
      }
    }
    return out;
  }

  public ArrayList<String> streamNum(String URLIn, String numIn) throws Exception {
    if (!URLIn.contains("@num")) {
      return new ArrayList<>(List.of("null"));
    }
    var num = label_URL.indexOf(URLIn);
    var tag = Arrays.stream(label_tag.get(num).split("@"))
        .dropWhile(next -> next.contains("num")).collect(Collectors.joining()).split(":");
    var tmp = numIn.matches("\\w(\\.\\w)+") ? List.of(numIn.split("\\.")) : check.num(tag[1]);
    var out = new ArrayList<>(tmp);
    return out;
  }

  public ArrayList<String> listConfig() {
    var out = new ArrayList<>(List.of("number", "title", "URL", "folder", "tag", "status", "\n"));

    IntStream.range(0, label_title.size()).forEach(next -> out.addAll(
        List.of(String.valueOf(next), label_title.get(next), label_URL.get(next),
            label_folder.get(next), label_tag.get(next), label_status.get(next), "\n")));
    return out;
  }

  public void syncConfig() throws Exception {
    var output = new FileOutputStream(downloads_dir + "config.txt");
    var json = new ObjectMapper().createObjectNode();

    label_title.forEach(next -> {
      var num = label_title.indexOf(next);
      var node = json.putObject(next);
      node.put("URL", label_URL.get(num));
      node.put("folder", label_folder.get(num));
      node.put("tag", label_tag.get(num));
      node.put("status", label_status.get(num));
    });
    output.write(json.toPrettyString().getBytes());
    output.close();
  }

  public String toOriDate(String dateIn, String URLIn) {
    if (dateIn.contains("null")) {
      return dateIn;
    }
    var out = LocalDate.parse(dateIn, uni_date).format(DateTimeFormatter.ofPattern(
        Arrays.stream(label_tag.get(label_URL.indexOf(URLIn))
            .split("@")).dropWhile(next -> next.contains("num"))
            .collect(Collectors.joining()).split(":")[1]));
    return out;
  }
}
