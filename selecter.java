package com.mycompany.stockgo;

import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class selecter extends config {
    private final ArrayList<String> queue, title;
    private final ArrayList<String>[] request, session;
    private ArrayList<Integer>[] mark;
    private boolean add_time;

    public selecter(ArrayList<String> in) throws Exception {
        queue = new ArrayList<>(in);
        title = new ArrayList<>(in.size());
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
                var add_time_tmp = url.get(0).contains("@date") && add_time;

                url.set(0, que_tmp.get(0).contains("http") ?
                        que_tmp.get(0).trim() : search_title(que_tmp.get(0).trim()).get(0));
                title.set(queue.indexOf(que), label_title.get(label_url.lastIndexOf(url.get(0))));
                que_tmp.forEach((tmp) -> {
                    if (tmp.trim().matches("date \\d+~\\d+"))
                        date.set(0, tmp.trim().split(" ")[1]);
                    if (tmp.trim().matches("(numbers) \\S*"))
                        numbers.set(0, tmp.trim().split(" ")[1]);
                    if (tmp.trim().matches("(request) \\S*"))
                        request_tmp.addAll(Arrays.asList(tmp.trim()
                                .split(" ")[1].split("\\.")));
                });
                for (var count = 0; count < request_tmp.size(); count++) {
                    if (add_time_tmp & count == 0) request[queue.indexOf(que)].add("日期");
                    request[queue.indexOf(que)].add(request_tmp.get(count));
                }
                try {
                    batch_num(url.get(0), numbers.get(0)).forEach((num) ->
                            batch_time(url.get(0), date.get(0)).forEach((time) -> {
                                try {
                                    var path = downloads_dir + label_folder
                                            .get(label_url.lastIndexOf(url.get(0))) +
                                            System.getProperty("file.separator") +
                                            check.UrlToName(url.get(0));
                                    if (url.get(0).contains("@num")) path = path.concat("_" + num);
                                    if (url.get(0).contains("@date")) path = path.concat("_" + time);
//                                    System.out.println(path + ".txt");
                                    var data_tmp = new data(path + ".txt", request_tmp).getData();
//                                    System.out.println(data_tmp);
                                    for (var count = 0; count < data_tmp.size(); count++) {
                                        if (add_time_tmp & count % request_tmp.size() == 0)
                                            session[queue.indexOf(que)].add(time);
                                        session[queue.indexOf(que)].add(data_tmp.get(count));
                                    }
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
        mark = (ArrayList<Integer>[]) engine.get("out");
    }

    public String mark_exp_val() throws Exception{
        var out = "";

        for (var count_ses = 0; count_ses < session.length; count_ses++) {
            for (var count_req = 0; count_req < request[count_ses].size(); count_req++) {
                var tmp = new ArrayList<String>();
                for (var count = 0; count < session[count_ses].size() / (request[count_ses].size() + 1); count++) {
                    tmp.add(session[count_ses].get(count));
                    tmp.add(session[count_ses].get(count + count_req));
                }
                var exp = new expectval(tmp, mark[count_ses]);
                out = "\n" + title.get(count_ses) + " day: " + exp.compare("D") + " week: " + exp.compare("W") +
                        " month: " + exp.compare("M") + " half year: " + exp.compare("HY") +
                        " year: " + exp.compare("Y") + "\n";
            }
        }
        return out;
    }

    public String getRequest() {
        var out = "";
        var count = 0;
        for (ArrayList<String> tmp : request) {
            out = out.concat(count + "." + tmp + " ");
            count++;
        }
        return out;
    }

}