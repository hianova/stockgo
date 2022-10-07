package com.mycompany.stockgo;

import java.io.File;
import java.util.*;

public class selecter extends config {
    private final ArrayList<String> queue, mark;
    private final ArrayList<String>[] session;
    boolean add_time;

    public selecter(ArrayList<String> in) throws Exception {
        queue = new ArrayList<>();
        mark = new ArrayList<>();
        session = new ArrayList[in.size()];
        queue.addAll(in);
        add_time = false;
        for (var count = 0; count < in.size(); count++) {
            session[count] = new ArrayList<String>();
        }
    }

    public ArrayList<String> select(boolean date_in) {
        var out = new ArrayList<String>();
        add_time = date_in;

        queue.forEach((que) -> {
            var que_tmp = new ArrayList<String>(Arrays.asList(que.split("-")));
            var date = new ArrayList<String>() {
                {
                    add("");
                }
            };
            var request = new ArrayList<String>();
            que_tmp.forEach((tmp) -> {
                if (tmp.trim().matches("date \\d*~\\d*"))
                    date.set(0, tmp.split(" ")[1]);
                if (tmp.trim().matches("title ([a-zA-Z]+|[\\u4E00-\\u9FFF]+).*$"))
                    request.addAll(Arrays.asList(tmp.split(" ")[1].split(",")));
            });
            batch_num(que_tmp.get(0).trim()).forEach((num) -> {
                batch_time(que_tmp.get(0).trim(), date.get(0)).forEach((time) -> {
                    var path = downloads_dir + label_folder.get(label_url.lastIndexOf(que_tmp.get(0).trim())) +
                            System.getProperty("file.separator");
                    try {
                        path = path.concat(check.toName(que_tmp.get(0).trim()));
                        if (que_tmp.get(0).contains("@num"))
                            path = path.concat("_" + num);
                        if (que_tmp.get(0).contains("@date"))
                            path = path.concat("_" + time);
                        var data_tmp = new data(path + ".txt").getdata(request);
                        data_tmp.forEach((tmp) -> {
                            if (date_in & data_tmp.indexOf(tmp) % request.size() == 0)
                                session[queue.indexOf(que)].add(time);
                            session[queue.indexOf(que)].add(tmp);
                        });
                        out.addAll(session[queue.indexOf(que)]);
                    } catch (Exception e) {
                        System.out.println("data not found");
                    }
                });
            });
        });
        return out;
    }

    public String getMark() {
        var out = String.join(",", mark);
        return out;
    }

}