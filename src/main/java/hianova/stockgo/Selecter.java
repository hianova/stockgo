package hianova.stockgo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Selecter extends Config {

  private final String[] URL, num, date;
  private final ArrayList<String>[] req, data;
  private boolean assert_date;

  public Selecter(ArrayList<String> cmdIn) throws Exception {
    var size = cmdIn.size();
    assert_date = true;
    URL = new String[size];
    num = new String[size];
    date = new String[size];
    req = new ArrayList[size];

    IntStream.range(0, size).forEach(nextURL -> {
      try {
        var relay = check.relay(cmdIn.get(nextURL));
        URL[nextURL] = relay.get("URL");
        req[nextURL] = new ArrayList<>(
            Arrays.asList(relay.get("requset").split("\\.")));
        num[nextURL] = relay.get("num");
        date[nextURL] = relay.get("date");
      } catch (Exception e) {
        System.out.print("relay not found " + e);
      }
      var cmd = cmdIn.get(nextURL).split("-");
      var requestPat = Pattern.compile("request");
      var withDatePat = Pattern.compile("withdate");
      if (URL[nextURL].isBlank()) {
        URL[nextURL] = label_URL.get(label_title.indexOf(cmd[0].trim()));
      }
      for (var count = 1; count < cmd.length; count++) {
        var tmp = cmd[count].trim();
        if (withDatePat.matcher(tmp).find() && tmp.replace("withdate=", "") == "false") {
          assert_date = false;
        }
        if (requestPat.matcher(tmp).find()) {
          req[nextURL] = new ArrayList<>(Arrays.asList(
              tmp.replace("request=", "").split("\\.")));
        }
        if (datePat.matcher(tmp).find()) {
          date[nextURL] = tmp.replace("date=", "");
        }
        if (numPat.matcher(tmp).find()) {
          num[nextURL] = tmp.replace("num=", "");
        }
      }
    });
    var sum = IntStream.range(0, req.length)
        .map(next -> (assert_date && datePat.matcher(URL[next]).find())
            ? req[next].size() + 1
            : req[next].size())
        .sum();
    data = new ArrayList[sum];
  }

  public ArrayList<String>[] select() throws Exception {
    ArrayList<String>[] out;
    var threads = new Thread[URL.length];

    IntStream.range(0, URL.length).forEach(nextURL -> {
      threads[nextURL] = new Thread(() -> {
        try {
          streamNum(URL[nextURL], num[nextURL]).forEach(
              nextnum -> streamDate(URL[nextURL], date[nextURL]).forEach(nextdate -> {
                try {
                  var path = downloads_dir + label_folder.get(label_URL.indexOf(
                      URL[nextURL]))
                      + System.getProperty("file.separator") + check.URLToName(
                          URL[nextURL].split("@Post:")[0])
                      + (numPat.matcher(URL[nextURL]).find() ? "_" + nextnum : "")
                      + (datePat.matcher(URL[nextURL]).find() ? "_" + nextdate : "") + ".txt";
                  var tmp = new Parse(path, req[nextURL]).data();
                  if (assert_date && datePat.matcher(URL[nextURL]).find()) {
                    data[nextURL] = new ArrayList<>();
                    IntStream.range(0, tmp[0].size())
                        .forEach(next -> data[nextURL].add(nextdate));
                    IntStream.range(0, req[nextURL].size())
                        .forEach(next -> data[nextURL + next + 1] = tmp[next]);
                  } else {
                    IntStream.range(0, req[nextURL].size())
                        .forEach(next -> data[nextURL + next] = tmp[next]);
                  }
                } catch (Exception e) {
                  System.out.println("can't get data " + e);
                }
              }));
        } catch (Exception e) {
          System.out.println("can't select data " + e);
        }
      });
      threads[nextURL].start();
    });
    for (var tmp : threads) {
      tmp.join();
    }
    out = data;
    return out;
  }

  public void export(String pathIn, boolean assertHeadIn) throws Exception {
    var maxNum = 0;
    var path = pathIn.isBlank() ? downloads_dir + "export.csv" : pathIn;
    var output = new FileOutputStream(path);

    new File(path).createNewFile();
    if (assertHeadIn) {
      IntStream.range(0, req.length)
          .forEach(nextReq -> IntStream.range(0, req[nextReq].size()).forEach(nextNum -> {
            var mark = nextReq == req.length - 1 && nextNum == req[nextReq].size() - 1 ? "\n" : ",";
            try {
              if (assert_date && datePat.matcher(URL[nextReq]).find() && nextNum == 0) {
                output.write(("\"日期\",").getBytes());
              }
              output.write(("\"" + req[nextReq].get(nextNum) + "\"" + mark).getBytes());
            } catch (Exception e) {
              System.out.println("head can't export " + e);
            }
          }));
    }
    for (var tmp : data) {
      maxNum = Math.max(maxNum, tmp.size());
    }
    IntStream.range(0, maxNum)
        .forEach(nextNum -> IntStream.range(0, data.length).forEach(nextData -> {
          var mark = nextData == data.length - 1 ? "\n" : ",";
          try {
            String tmp = nextNum >= data[nextData].size() ? ""
                : data[nextData].get(nextNum).isEmpty() ? "null" : data[nextData].get(nextNum);
            output.write(("\"" + tmp + "\"" + mark).getBytes());
          } catch (Exception e) {
            System.out.println("data can't export " + e);
          }
        }));
    output.close();
  }

  public String backTest(String pathIn) throws Exception {
    var tmp = new BackTest(data, pathIn);
    String out = "";
    // out = tmp.getExpecVal();
    return out;
  }

  public ArrayList<String>[] getRequests() {
    var out = req;
    return out;
  }
}