package com.mycompany.stockgo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngineManager;

public class selecter extends config {

  private final ArrayList<String> queue, title;
  private final ArrayList<String>[] request, session;
  private ArrayList<Integer>[] mark;

  public selecter(ArrayList<String> in) throws Exception {
    queue = new ArrayList<>(in);
    title = new ArrayList<>(in.size());
    request = new ArrayList[in.size()];
    session = new ArrayList[in.size()];

    for (var count = 0; count < queue.size(); count++) {
      request[count] = new ArrayList<>();
      session[count] = new ArrayList<>();
    }
  }

  public ArrayList<String> select(boolean date_in) {
    var out = new ArrayList<String>();

    queue.forEach((que) -> {
      var que_tmp = new ArrayList<>(List.of(que.split("-")));
      var url = new ArrayList<>(List.of(""));
      var date = new ArrayList<>(List.of(""));
      var numbers = new ArrayList<>(List.of(""));
      var request_tmp = new ArrayList<String>();

      url.set(0, que_tmp.get(0).contains("http") ? que_tmp.get(0).trim()
          : label_URL.get(label_title.indexOf(que_tmp.get(0).trim())));
      title.add(queue.indexOf(que), label_title.get(label_URL.indexOf(url.get(0))));
      var add_time_tmp = url.get(0).contains("date") && date_in;
      que_tmp.forEach((tmp) -> {
        if (tmp.trim().matches("date \\d+~\\d+")) {
          date.set(0, tmp.trim().split(" ")[1]);
        }
        if (tmp.trim().matches("(numbers) \\S*")) {
          numbers.set(0, tmp.trim().split(" ")[1]);
        }
        if (tmp.trim().matches("(request) \\S*")) {
          request_tmp.addAll(List.of(tmp.trim().split(" ")[1].split("\\.")));
        }
      });
      try {
        batch_num(url.get(0), numbers.get(0)).forEach(
            (num) -> batch_time(url.get(0), date.get(0)).forEach(time -> {
              try {
                var path = downloads_dir + label_folder.get(label_URL.indexOf(url.get(0)))
                    + File.separator + check.UrlToName(url.get(0).split("@Post:")[0]);
                if (url.get(0).contains("@num")) {
                  path = path.concat("_" + num);
                }
                if (url.get(0).contains("@date")) {
                  path = path.concat("_" + time);
                }
                var data = new data(path + ".txt", request_tmp);
                var data_tmp = data.getData();
                if (request_tmp.contains("ALL")) {
                  var tmp = data.getHead();
                  if (!tmp.isEmpty()) {
                    request_tmp.remove(0);
                    request_tmp.addAll(tmp);
                  }
                }
                for (var count = 0; count < data_tmp.size(); count++) {
                  if (add_time_tmp & count % request_tmp.size() == 0) {
                    session[queue.indexOf(que)].add(time);
                  }
                  session[queue.indexOf(que)].add(data_tmp.get(count));
                }
                out.addAll(session[queue.indexOf(que)]);
              } catch (Exception e) {
                System.out.println("data not found " + e);
              }
            }));
      } catch (Exception e) {
        System.out.println("number list not found " + e);
      }
      for (var count = 0; count < request_tmp.size(); count++) {
        if (add_time_tmp & count == 0) {
          request[queue.indexOf(que)].add("日期");
        }
        request[queue.indexOf(que)].add(request_tmp.get(count));
      }
    });
    return out;
  }

  public void export(String in, boolean label_in) throws Exception {
    var path = in.contains("-E") ? downloads_dir + "export.csv" : in;
    var out_stream = new OutputStreamWriter(new FileOutputStream(path));
    new File(path).createNewFile();

    if (label_in) {
      for (var count_req = 0; count_req < request.length; count_req++) {
        for (var count = 0; count < request[count_req].size(); count++) {
          try {
            var is_last =
                count_req == request.length - 1 && (count + 1) % request[count_req].size() == 0
                    ? "\n" : ",";
            out_stream.write("\"" + request[count_req].get(count) + "\"" + is_last);
          } catch (IOException e) {
            System.out.println("label export error " + e);
          }
        }
      }
    }
    for (var count_ses = 0; count_ses < session.length; count_ses++) {
      for (var count = 0; count < session[count_ses].size(); count++) {
        try {
          var is_last =
              count_ses == session.length - 1 && (count + 1) % request[count_ses].size() == 0 ? "\n"
                  : ",";
          out_stream.write("\"" + session[count_ses].get(count) + "\"" + is_last);
        } catch (IOException e) {
          System.out.println("data export error " + e);
        }
      }
    }
    out_stream.close();
    System.out.println("file exported");
  }

  public void setMark(String in) throws Exception {
    var file = new File(strategy_dir + in + ".js");
    if (!file.exists()) {
      System.out.println("strategy not exist");
      return;
    }
    var script = "";
    var file_in = new FileInputStream(file);
    var engine = new ScriptEngineManager().getEngineByName("javascript");

    script = new String(file_in.readAllBytes());
    engine.put("in", session);
    engine.put("in_req", request);
    engine.eval(script);
    mark = (ArrayList<Integer>[]) engine.get("out");
  }

  public String mark_exp_val() {
    var out = "";

    for (var count_ses = 0; count_ses < session.length; count_ses++) {
      for (var count_req = 0; count_req < request[count_ses].size(); count_req++) {
        var tmp = new ArrayList<String>();
        for (var count = 0; count < session[count_ses].size() / (request[count_ses].size() + 1);
            count++) {
          tmp.add(session[count_ses].get(count));
          tmp.add(session[count_ses].get(count + count_req));
        }
        var exp = new expect_val(tmp, mark[count_ses]);
        out =
            "\n" + title.get(count_ses) + " day: " + exp.compare("D") + " week: " + exp.compare("W")
                + " month: " + exp.compare("M") + " half year: " + exp.compare("HY") + " year: "
                + exp.compare("Y") + "\n";
      }
    }
    return out;
  }

  public String getRequest() {
    String out;
    var count = 0;
    var req_tmp = "";

    for (ArrayList<String> tmp : request) {
      req_tmp = req_tmp.concat(count + "." + tmp + " ");
      count++;
    }
    out = req_tmp;
    return out;
  }

}