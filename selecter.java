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
        queue = new ArrayList<>();
        queue.addAll(in);
        request = new ArrayList[in.size()];
        session = new ArrayList[in.size()];
        add_time = false;

        for (var count = 0; count < in.size(); count++) {
            request[count] = new ArrayList<>();
            session[count] = new ArrayList<>();
        }

    }

    public ArrayList<String> select(boolean date_in) {
        var out = new ArrayList<String>();
        add_time = date_in;

        queue.forEach((que) -> {
            var thread = new Thread(() -> {
                var que_tmp = new ArrayList<>(Arrays.asList(que.split("-")));//System.out.println(que_tmp);
                var date = new ArrayList<String>(Arrays.asList(new String[]{""}));
                var numbers = new ArrayList<String>(Arrays.asList(new String[]{""}));
                var request_tmp = new ArrayList<String>();
                date.add("");

                que_tmp.forEach((tmp) -> {
                    if (tmp.trim().matches("date \\d+~\\d+"))
                        date.set(0, tmp.split(" ")[1]);
                    if (tmp.trim().matches("numbers \\w+,*\\w*"))
                        numbers.set(0, tmp.split(" ")[1]);
                    if (tmp.trim().matches("request \\S*"))
                        request_tmp.addAll(Arrays.asList(tmp.split(" ")[1].split(",")));
                });//System.out.println(request_tmp);
                try {
                    batch_num(que_tmp.get(0).trim(), numbers.get(0)).forEach((num) -> {
                        batch_time(que_tmp.get(0).trim(), date.get(0)).forEach((time) -> {
                            var path = downloads_dir + label_folder.get(label_url.lastIndexOf(que_tmp.get(0).trim())) +
                                    System.getProperty("file.separator");
                            try {
                                path = path.concat(check.UrlToName(que_tmp.get(0).trim()));
                                if (que_tmp.get(0).contains("@num"))
                                    path = path.concat("_" + num);
                                if (que_tmp.get(0).contains("@date"))
                                    path = path.concat("_" + time);//System.out.println(path);
                                var data_tmp = new data(path + ".txt", request_tmp).getData();//System.out.println(data_tmp);
                                if (add_time)
                                    request_tmp.forEach((tmp) -> request[queue.indexOf(que)].add(tmp));
                                data_tmp.forEach((tmp) -> {
                                    if (add_time & data_tmp.indexOf(tmp) % request_tmp.size() == 0)
                                        session[queue.indexOf(que)].add(time);
                                    session[queue.indexOf(que)].add(tmp);
                                });
                                out.addAll(session[queue.indexOf(que)]);
                            } catch (Exception e) {
                                System.out.println("data not found");
                            }
                        });
                    });
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
        var req_tmp = Arrays.asList(request);
        var ses_tmp = Arrays.asList(session);
        new File(in).createNewFile();

        if (label_in) {
            req_tmp.forEach((tmp) -> {
                try {
                    var is_last = req_tmp.indexOf(tmp) == (req_tmp.size() - 1) ? "\n" : ",";
                    out_stream.write("\"" + tmp + "\"" + is_last);
                } catch (IOException e) {
                    System.out.println("request can't output");
                }
            });
        }
        ses_tmp.forEach((tmp) -> {
            try {
                var is_last = (ses_tmp.indexOf(tmp) + 1) % ses_tmp.size() == 0 ? "\n" : ",";
                out_stream.write("\"" + tmp + "\"" + is_last);
            } catch (IOException e) {
                System.out.println("session can't output");
            }
        });
        out_stream.close();
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
                    " month: " + tmp.compare("M") + " half year: " + tmp.compare("HY")+
                    " year: "+tmp.compare("Y");
        }
        return out;
    }

    public String getRequest() {
        var out = "";
        var count = 0;
        for (var tmp : request) {
            out = out.concat(count + "." + tmp.toString() + " ");
            count++;
        }
        return out;
    }

}