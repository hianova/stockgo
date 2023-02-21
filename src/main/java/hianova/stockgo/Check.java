package hianova.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Check {

  public String URLToName(String URLIn) {
    var removePat = Pattern.compile("(\\.\\w+|@num|@date|\\?.+)");
    var tmp = removePat.matcher(URLIn).replaceAll("").split("/");
    var out = tmp[tmp.length - 2] + "_" + tmp[tmp.length - 1];
    return out;
  }

  public String tag(String tagIn) throws Exception {
    var out = new ObjectMapper().readTree(
        new File(System.getProperty("user.dir") + File.separator + "parse_rule.json"))
        .at("/" + tagIn).textValue();

    if (out == null) {
      var tmp = tagIn.split("/");
      if (tmp.length >= 3) {
        if (tmp[1].equals("encode") && tmp[2].equals("UTF-8")) {
          out = "UTF-8";
        } else if (tmp[1].equals("head")) {
          if (tmp[2].equals("JSON")) {
            out = "fields";
          } else if (tmp[2].equals("HTML")) {
            out = "tr:eq(0)";
          }
        } else if (tmp[1].equals("body")) {
          if (tmp[2].equals("JSON")) {
            out = "data";
          } else if (tmp[2].equals("HTML")) {
            out = "tr:gt(0)";
          }
        }
      }
    }
    return out;
  }

  public ArrayList<String> num(String typeIn) throws Exception {
    ArrayList<String> out;
    var tmp = typeIn.split(":");

    if (tmp[0].equals("stock")) {
      out = stockNum(tmp.length > 1 ? tmp[1] : "");
    } else if (tmp[0].equals("ETF")) {
      out = ETFNum();
    } else {
      out = new ArrayList<>();
    }
    return out;
  }

  public ArrayList<String> stockNum(String typeIn) throws Exception {
    var out = new ArrayList<String>();
    var list = new ArrayList<String>();
    var match = Pattern.compile("^[0-9]{4}　");

    if (typeIn.equals("listed")) {
      list.add("上市");
    } else if (typeIn.equals("OTC")) {
      list.add("上櫃");
    } else {
      list.addAll(List.of("上市", "上櫃"));
    }
    list.forEach(nextList -> {
      try {
        new Parse(downloadsDir() + nextList + "證券代號" + File.separator + "isin_C_public.txt",
            new ArrayList<>(List.of("有價證券代號及名稱"))).data()[0].forEach(
              next -> { if (match.matcher(next).find()) {
                out.add(next.split("　")[0]);
              }
            });
      } catch (Exception e) {
        System.out.println("number not exist");
      }
    });
    return out;
  }

  public ArrayList<String> ETFNum() throws Exception {
    var out = new ArrayList<String>();
    var match = Pattern.compile("^T[0-9]+\\w");

    new Parse(downloadsDir() + "基金＿國際證券代號" + File.separator + "isin_C_public.txt",
        new ArrayList<>(List.of("有價證券代號及名稱"))).data()[0].forEach(next -> {
          if (match.matcher(next).find()) {
            out.add(next.split("　")[0]);
          }
        });
    return out;
  }

  public String downloadsDir() {
    var out = System.getProperty("user.dir") + File.separator + "downloads" + File.separator;
    return out;
  }

  public String strategyDir() {
    var out = System.getProperty("user.dir") + File.separator + "strategy" + File.separator;
    return out;
  }

  public String UA() throws Exception {
    var input = new FileInputStream(
        System.getProperty("user.dir") + File.separator + "useragent.txt");
    var UA = new ArrayList<>(List.of(new String(input.readAllBytes()).split("\n")));
    var out = UA.get(new Random().nextInt(UA.size()));

    input.close();
    return out;
  }

  public Element cleanHTML(String pathIn) {
    var page = Jsoup.parse(pathIn);
    var out = new Element("html");

    page.select("tr").forEach(nextRow -> {
      if (nextRow.children().select("table").isEmpty()
          && nextRow.parent().select("table").isEmpty()) {
        var line = new Element("tr");
        nextRow.children().forEach(nextCell -> {
          nextCell.select("br").remove();
          line.appendChild(nextCell);
          if (nextCell.hasAttr("colspan")) {
            IntStream.range(1, Integer.parseInt(nextCell.attr("colspan")))
                .forEach(next -> line.appendChild(nextCell.clone().removeAttr("colspan")));
          }
        });
        out.appendChild(line);
      }
    });
    IntStream.range(0, out.childNodeSize()).forEach(nextRow -> {
      var row = out.child(nextRow);
      IntStream.range(0, row.childrenSize()).forEach(nextCell -> {
        var cell = row.children().get(nextCell);
        if (cell.hasAttr("rowspan")) {
          IntStream.range(1, Integer.parseInt(cell.attr("rowspan")))
              .forEach(next -> out.child(nextRow + next)
                  .insertChildren(nextCell, cell.clone().removeAttr("rowspan")));
        }
      });
    });
    out.appendChild(page.select("tag").first());
    return out;
  }

  public boolean isJSON(String JSONIn) {
    boolean out;
    try {
      new ObjectMapper().readTree(JSONIn);
      out = true;
    } catch (Exception e) {
      out = false;
    }
    return out;
  }

  public boolean isHTML(String HTMLIn) {
    var tmp = Jsoup.parse(HTMLIn);
    var out = tmp.childNodeSize() > 0;
    return out;
  }

  public HashMap<String, String> relay(String cmdIn) throws Exception {
    var out = new HashMap<String, String>();
    var file = new ObjectMapper().readTree(new File(downloadsDir() + "relay.json"));
    var cmd = cmdIn.split("-");
    var title = cmd[0].replace(".", "/").trim();
    var exist = file.has(title);

    out.put("URL", exist ? file.at(title + "/URL").asText() : "");
    out.put("request", exist ? file.at(title + "/request").asText() : "");
    out.put("date", exist ? file.at(title + "/date").asText() : "");
    out.put("num", exist ? file.at(title + "/num").asText() : "");
    return out;
  }
}
