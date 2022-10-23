package com.mycompany.stockgo;

import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;

public class selecter extends config {
    private final ArrayList<String> queue;
    private final ArrayList[] request, session;
    private ArrayList[] mark_data, mark_time;
    private boolean add_time;

    public selecter(ArrayList<String> in) throws Exception {
        queue = new ArrayList<>(in);
        request = new ArrayList[in.size()];
        session = new ArrayList[in.size()];
        add_time = false;

        for (var count = 0; count < queue.size(); count++) {
            request[count] = new ArrayList<>();
            session[count] = new ArrayList<>();
        }

    }

    public ArrayList<String> select(boolean date_in) {
        var out = new ArrayList<String>();
        add_time = date_in;
        queue.forEach((que) -> {
            var thread = new Thread(() -> {
                var que_tmp = new ArrayList<>(Arrays.asList(que.split("-")));
                var url = new ArrayList<>(List.of(""));
                var date = new ArrayList<>(List.of(""));
                var numbers = new ArrayList<>(List.of(""));
                var request_tmp = new ArrayList<String>();

                url.set(0, que_tmp.get(0).contains("http") ?
                        que_tmp.get(0).trim() : search_title(que_tmp.get(0).trim()).get(0));
                que_tmp.forEach((tmp) -> {
                    if (tmp.trim().matches("date \\d+~\\d+"))
                        date.set(0, tmp.trim().split(" ")[1]);
                    if (tmp.trim().matches("(numbers) \\S*"))
                        numbers.set(0, tmp.trim().split(" ")[1]);
                    if (tmp.trim().matches("(request )\\S*"))
                        request_tmp.addAll(Arrays.asList(tmp.trim().split(" ")[1].split("\\.")));
                });
                var add_time_tmp = url.get(0).contains("@date")?add_time:false;
                request_tmp.forEach((tmp) -> {
                    if (add_time_tmp & request_tmp.indexOf(tmp) == 0)
                        request[queue.indexOf(que)].add("日期");
                    request[queue.indexOf(que)].add(tmp);
                });
                try {
                    batch_num(url.get(0), numbers.get(0)).forEach((num) ->
                            batch_time(url.get(0), date.get(0)).forEach((time) -> {
                                try {
                                    var path = downloads_dir + label_folder.get(label_url.lastIndexOf(url.get(0))) +
                                            System.getProperty("file.separator") + check.UrlToName(url.get(0));
                                    if (url.get(0).contains("@num"))
                                        path = path.concat("_" + num);
                                    if (url.get(0).contains("@date"))
                                        path = path.concat("_" + time);
//                                    System.out.println(path + ".txt");
                                    var data_tmp = new data(path + ".txt", request_tmp).getData();
//                                    System.out.println(data_tmp);
                                    data_tmp.forEach((tmp) -> {
                                        if (add_time_tmp & data_tmp.indexOf(tmp) % request_tmp.size() == 0)
                                            session[queue.indexOf(que)].add(time);
                                        session[queue.indexOf(que)].add(tmp);
                                    });
                                    out.addAll(session[queue.indexOf(que)]);
                                } catch (Exception e) {
                                    System.out.println("data not found");
                                }
                            }));
                } catch (Exception e) {
                    System.out.println("number list not found");
                }
            });
            thread.start();
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
                        var is_last = count_req == request.length - 1 &&
                                (count + 1) % request[count_req].size() == 0 ? "\n" : ",";
                        out_stream.write("\"" + request[count_req].get(count) + "\"" + is_last);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        for (var count_ses = 0; count_ses < session.length; count_ses++) {
            for (var count = 0; count < session[count_ses].size(); count++) {
                try {
                    var is_last = count_ses == session.length - 1 &&
                            (count + 1) % request[count_ses].size() == 0 ? "\n" : ",";
                    out_stream.write("\"" + session[count_ses].get(count) + "\"" + is_last);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        out_stream.close();
        System.out.println("file exported");
    }

    public void setMark(String in) throws Exception {
        var file = new File(check.getStrategy_dir() + in + ".js");
        if (!file.exists()) return;
        var script = "";
        var file_in = new BufferedReader(new FileReader(file));
        for (var tmp = ""; (tmp = file_in.readLine()) != null; ) {
            script = script.concat(tmp);
        }
        var engine = new ScriptEngineManager().getEngineByName("javascript");

        engine.put("in", session);
        engine.put("in_req", request);
        engine.eval(script);
        mark_data = (ArrayList[]) engine.get("out_data");
        mark_time = (ArrayList[]) engine.get("out_time");
    }

    public String[] mark_exp_val() {
        var out = new String[mark_data.length];
        for (var count = 0; count < mark_data.length; count++) {
            var tmp = new expectval(mark_data[count], mark_time[count]);
            out[count] = " day: " + tmp.compare("D") + " week: " + tmp.compare("W") +
                    " month: " + tmp.compare("M") + " half year: " + tmp.compare("HY") +
                    " year: " + tmp.compare("Y");
        }
        return out;
    }

    public String getRequest() {
        var out = "";
        var count = 0;
        for (ArrayList<? extends Object> tmp : request) {
            out = out.concat(count + "." + tmp + " ");
            count++;
        }
        return out;
    }

}