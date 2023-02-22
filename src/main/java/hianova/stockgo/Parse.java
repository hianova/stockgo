package hianova.stockgo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Parse {

  private final ArrayList<String> hd, bd;
  private final ArrayList<Integer> req;
  private final ArrayList<String>[] req_tag;
  private boolean assertTag;

  public Parse(String pathIn, ArrayList<String> reqIn) throws Exception {
    String file;
    var check = new Check();
    
    hd = new ArrayList<>();
    bd = new ArrayList<>();
    req = new ArrayList<>();
    req_tag = new ArrayList[reqIn.size()];
      var path = Paths.get(pathIn);
      file = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    if (check.isHTML(file)) {
      var tmp = check.cleanHTML(file);
      var tag = tmp.select("tag").text();
      hd.addAll(tmp.select(check.tag(tag + "/head/HTML")).first().children().eachText());
      tmp.select(check.tag(tag + "/body/HTML"))
          .forEach(next -> bd.addAll(next.children().eachText()));
    } else if (check.isJSON(file)) {
      var tmp = new ObjectMapper().readTree(file);
      var tag = tmp.at("tag").textValue();
      var hdTmp = tmp.at(check.tag(tag + "/head/JSON"));
      var bdTmp = tmp.at(check.tag(tag + "/body/JSON"));
      IntStream.range(0, hdTmp.size())
          .forEach(next -> hd.add(hdTmp.get(next).textValue()));
      IntStream.range(0, bdTmp.size()).forEach(
          nextRow -> IntStream.range(0, bdTmp.get(nextRow).size())
              .forEach(nextCell -> bd.add(bdTmp.get(nextRow).get(nextCell).textValue())));
    } else {
      var removePat = Pattern.compile("\" ");
      var tmp = removePat.matcher(file).replaceAll("").split("\n");
      hd.addAll(List.of(tmp[0].split(",")));
      IntStream.range(1, tmp.length).forEach(
          next -> bd.addAll(List.of(tmp[next].split(","))));
    }
    var tagPat = Pattern.compile("#\\w+");
    IntStream.range(0, reqIn.size()).forEach(nextReq -> {
      var match = tagPat.matcher(reqIn.get(nextReq));
      if (match.find()) {
        assertTag = true;
        req_tag[nextReq] = new ArrayList<>();
        IntStream.range(0, match.groupCount()).forEach(next -> {
          req_tag[nextReq].add(match.group(next).replace("#", ""));
        });
      }
      req.add(hd.indexOf(reqIn.get(nextReq).split("#")[0]));
    });
  }

  public ArrayList<String>[] data() {
    var out = new ArrayList[req.size()];

    IntStream.range(0, out.length).forEach(
        next -> out[next] = new ArrayList<>());
    for (var num = 0; num < bd.size(); num += hd.size()) {
      var pass = !assertTag;
      var line = new ArrayList<>();
      for (var line_num = 0; line_num < req.size(); line_num++) {
        var tmp = bd.get(num + req.get(line_num)).isEmpty() ? "null" : bd.get(num + req.get(line_num));
        line.add(tmp);
        if (assertTag && !pass) {
          pass = req_tag[line_num].stream().anyMatch(tmp::equals);
        }
      }
      if (pass) {
        IntStream.range(0, out.length).forEach(
            next -> out[next].add(line.get(next)));
      }
    }
    return out;
  }
}