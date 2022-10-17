package com.mycompany.stockgo;

import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;

public class selecter extends config {
    private final ArrayList<String> queue, mark;
    private final ArrayList[] request;
    private final ArrayList[] session;
    private boolean add_time;

    public selecter(ArrayList<String> in) throws Exception {
        queue = new ArrayList<>();
        queue.addAll(in);
        mark = new ArrayList<>();
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
            var que_tmp = new ArrayList<>(Arrays.asList(que.split("-")));
            var date = new ArrayList<String>();
            var request_tmp = new ArrayList<String>();
            date.add("");

            que_tmp.forEach((tmp) -> {
                if (tmp.trim().matches("date \\d+~\\d+"))
                    date.set(0, tmp.split(" ")[1]);
                if (tmp.trim().matches("request \\w.*$"))
                    request_tmp.addAll(Arrays.asList(tmp.split(" ")[1].split(",")));
            });//System.out.println(request_tmp);
            try {
                batch_num(que_tmp.get(0).trim(), "").forEach((num) -> {
                    batch_time(que_tmp.get(0).trim(), date.get(0)).forEach((time) -> {
                        var path = downloads_dir + label_folder.get(label_url.lastIndexOf(que_tmp.get(0).trim())) +
                                System.getProperty("file.separator");
                        try {
                            path = path.concat(check.UrlToName(que_tmp.get(0).trim()));
                            if (que_tmp.get(0).contains("@num"))
                                path = path.concat("_" + num);
                            if (que_tmp.get(0).contains("@date"))
                                path = path.concat("_" + time);//System.out.println(path);
                            var data_tmp = new data(path + ".txt",request_tmp).getData();//System.out.println(data_tmp);

                            if (add_time)
                                request_tmp.forEach((tmp) -> request[queue.indexOf(que)].add(tmp));
                            data_tmp.forEach((tmp) -> {
                                if (date_in & data_tmp.indexOf(tmp) % request_tmp.size() == 0)
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

    public String getRequest() {
        var out="";
        for (var tmp :request){
            out=out.concat(tmp.toString()+",");
        }
        return out;
    }
    public String getMark() {
        var out = String.join(",", mark);
        return out;
    }

}