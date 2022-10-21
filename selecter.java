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
                var url = new ArrayList<String>(List.of(""));
                var date = new ArrayList<String>(List.of(""));
                var numbers = new ArrayList<String>(List.of(""));
                var request_tmp = new ArrayList<String>();

                url.set(0, que_tmp.get(0).contains("http") ?
                        que_tmp.get(0).trim() : search_title(que_tmp.get(0).trim()).get(0));
                que_tmp.forEach((tmp) -> {
                    if (tmp.trim().matches("date \\d+~\\d+"))
                        date.set(0, tmp.trim().split(" ")[1]);
                    if (tmp.trim().matches("numbers \\w+,*\\w*"))
                        numbers.set(0, tmp.trim().split(" ")[1]);
                    if (tmp.trim().matches("(request )\\S*"))
                        request_tmp.addAll(Arrays.asList(tmp.trim().split(" ")[1].split("\\.")));
                });
                request_tmp.forEach((tmp) -> {
                    if (add_time & request_tmp.indexOf(tmp) == 0)
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
                                        if (add_time & data_tmp.indexOf(tmp) % request_tmp.size() == 0)
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
        var out_stream = new OutputStreamWriter(new FileOutputStream(in));
        new File(in).createNewFile();
        var pointer = 0;

        for (var count = 0; count < queue.size(); count++) {
            var count_tmp = count;
            if (label_in) {
                request[count].forEach((tmp) -> {
                    try {
                        var is_last = request[count_tmp].indexOf(tmp) == (request[count_tmp].size() - 1) ? "\n" : ",";
                        out_stream.write("\"" + tmp + "\"" + is_last);
                    } catch (IOException e) {
                        System.out.println("label can't output");
                    }
                });
            }
            session[count].forEach((tmp) -> {
                try {
                    var is_last = session[count_tmp].indexOf(tmp) % request[count_tmp].size() == 0 ? "\n" : ",";
                    out_stream.write("\"" + tmp + "\"\n" + is_last);
                } catch (IOException e) {
                    System.out.println("session can't output");
                }
            });
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
        for (var tmp : request) {
            out = out.concat(count + "." + tmp + " ");
            count++;
        }
        return out;
    }

}