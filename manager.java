package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import java.time.*;

public class manager extends config {

    public manager() throws Exception {
    }

    public void add(ArrayList<String> in) throws Exception {
        label_url.add(in.get(0));
        label_title.add(in.get(1));
        label_folder.add(in.get(2));
        label_tag.add(in.get(3));
        label_status.add(LocalDate.now().minusYears(10).format(uni_date));

        download(in.get(0));
        label_status.set(label_url.lastIndexOf(in.get(0)), LocalDate.now().format(uni_date));
        sync_config();
        System.out.println(in.get(1) + " added");
    }

    public void delete(ArrayList<String> in) throws Exception {
        if (in.get(0).matches("[0-9]+")) {
            in.forEach((in_tmp) -> {
                label_url.remove(in_tmp);
                label_title.remove(in_tmp);
                label_folder.remove(in_tmp);
                label_tag.remove(in_tmp);
                label_status.remove(in_tmp);
            });
        }
        sync_config();
    }

    public void update() {
        label_status.forEach((tmp) -> {
            if (Period.between(LocalDate.parse(tmp, uni_date),
                    LocalDate.now()).getMonths() > 1) {
                try {
                    System.out.println("updating...");
                    download(label_url.get(label_status.indexOf(tmp)));
                    label_status.set(label_status.indexOf(tmp), "\"" + LocalDate.now().format(uni_date) + "\"");
                    sync_config();
                } catch (Exception e) {
                    System.out.println("update has suspend");
                }
            }
        });
        System.out.println("files are updated");
    }

    public void download(String in) throws Exception {
        var path = downloads_dir + label_folder.get(label_url.lastIndexOf(in)) +
                System.getProperty("file.separator") + check.UrlToName(in);
        new File(downloads_dir + label_folder.get(label_url.lastIndexOf(in))).mkdir();

        batch_num(in, "").forEach((num) -> {
            try {
                batch_time(in, "").forEach((time) -> {
                    try {
                        var in_tmp = in
                                .replaceAll("@date", time)
                                .replaceAll("@num", num);
                        var crawl = new crawl(in_tmp);
                        var path_tmp = path;
                        if (in.contains("@num"))
                            path_tmp = path_tmp.concat("_" + num);
                        if (in.contains("@date"))
                            path_tmp = path_tmp.concat("_" + time);
                        crawl.setPath(path_tmp + ".txt");
                        crawl.save();
                        Thread.sleep((long) (Math.random() * 50));
                    } catch (Exception e) {
                        System.out.println("time iterator stopped");
                    }
                });
            } catch (Exception e) {
                System.out.println("number iterator stopped");
            }
        });
    }

    public void sync_config() throws Exception {
        var output = new BufferedWriter(new FileWriter(
                downloads_dir + "config.txt", false));
        var last_ele = label.get(label.size() - 1);

        label.forEach((tmp) -> {
            try {
                output.write("\"" + tmp + "\"" + (tmp.contains(last_ele) ? "\n" : ","));
            } catch (Exception e) {
                System.out.println("label can't output");
            }
        });
        for (var count = 0; count < label_url.size(); count++) {
            output.write("\"" + label_url.get(count) + "\"," +
                    "\"" + label_title.get(count) + "\"," +
                    "\"" + label_folder.get(count) + "\"," +
                    "\"" + label_tag.get(count) + "\"," +
                    "\"" + label_status.get(count) + "\"\n");
        }
        output.close();
    }

    public void reset_config() throws Exception {
        var list = new String[]{"網址", "標題", "資料夾", "標籤", "狀態"};
        var last_ele = list[list.length - 1];
        var output = new BufferedWriter(new FileWriter(
                downloads_dir + "config.txt", false));
        for (var tmp : list)
            output.write("\"" + tmp + "\"" + (tmp.contains(last_ele) ? "\n" : ","));
        output.close();
        System.out.println("config.txt is reset");
    }

}