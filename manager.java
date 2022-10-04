package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class manager {
    checksyn check;
    String downloads_dir;
    DateTimeFormatter uni_date;
    ArrayList<String> label, label_url, label_title, label_folder, label_tag, label_status;

    public manager() throws Exception {
        check = new checksyn();
        downloads_dir = check.getDownloads_dir();
        uni_date = check.getuni_date();
        label = new ArrayList<>();
        label_url = new ArrayList<>();
        label_title = new ArrayList<>();
        label_folder = new ArrayList<>();
        label_tag = new ArrayList<>();
        label_status = new ArrayList<>();
        var input = new BufferedReader(new FileReader(downloads_dir + "config.txt"));

        for (String input_tmp; (input_tmp = input.readLine()) != null; ) {
            var tmp = input_tmp.replace("\"", "").split(",");
            if (label.isEmpty()) {
                label.addAll(Arrays.asList(tmp));
                continue;
            }
            label_url.add(tmp[0]);
            label_title.add(tmp[1]);
            label_folder.add(tmp[2]);
            label_tag.add(tmp[3]);
            label_status.add(tmp[4]);
        }
        input.close();
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

    public void update() {
        label_status.forEach((tmp) -> {
            if (Period.between(LocalDate.parse(tmp, uni_date),
                    LocalDate.now()).getMonths() > 1) {
                try {
                    System.out.println("updating...");
                    download(label_url.get(label_status.indexOf(tmp)));
                    label_status.set(label_status.indexOf(tmp), "\"" + LocalDate.now().format(uni_date) + "\"");
                    sync_config();
                } catch (Exception ex) {
                }
            }
        });
        System.out.println("files are updated");
    }

    public void sync_config() throws Exception {
        var output = new BufferedWriter(new FileWriter(
                downloads_dir + "config.txt", false));
        var last_ele = label.get(label.size() - 1);

        label.forEach((tmp) -> {
            try {
                output.write("\"" + tmp + "\"" + (tmp.contains(last_ele) ? "\n" : ","));
            } catch (Exception e) {
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

    public void download(String in) throws Exception {
        var path = downloads_dir + label_folder.get(label_url.lastIndexOf(in)) +
                System.getProperty("file.separator") + check.toname(in);
        new File(downloads_dir + label_folder.get(label_url.indexOf(in))).mkdir();

        batch_num(in).forEach((num) -> {
            try {
                batch_time(in).forEach((time) -> {
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
                        crawl.setpath(path_tmp + ".txt");
                        crawl.save();
                    } catch (Exception e) {
                    }
                });
            } catch (Exception e) {
            }
        });
    }

    public ArrayList<String> batch_time(String in) {
        var out = new ArrayList<String>();
        if (!in.contains("@date")) {
            out.add("null");
            return out;
        }

        var tag_tmp = label_tag.get(label_url.lastIndexOf(in)).split("@");
        for (var tmp : tag_tmp) {
            if (tmp.contains("date")) {
                tag_tmp = tmp.split(":");
                break;
            }
        }
        var is_ROC = Pattern.compile("yyy").matcher(tag_tmp[1]).find();
        for (var date_tmp = LocalDate.parse(label_status.get(label_url.lastIndexOf(in)), uni_date);
             date_tmp.isBefore(LocalDate.now()); ) {
            var tmp = is_ROC ? date_tmp.minusYears(1911) : date_tmp;
            out.add(tmp.format(DateTimeFormatter.ofPattern(tag_tmp[1])));
            switch (tag_tmp[2]) {
                case "Y" -> date_tmp = date_tmp.plusYears(1);
                case "M" -> date_tmp = date_tmp.plusMonths(1);
                case "W" -> date_tmp = date_tmp.plusWeeks(1);
                case "D" -> date_tmp = date_tmp.plusDays(1);
            }
        }
        return out;
    }

    public ArrayList<String> batch_num(String in) {
        var out = new ArrayList<String>();
        if (!in.contains("@num")) {
            out.add("null");
            return out;
        }
        var tmp = Pattern.compile("@num:\\w*")
                .matcher("in").group().split(":");

        switch (tmp[1]) {
            case "stock" -> out = check.getstocknum();
            case "ETF" -> out = check.getETFnum();
        }
        return out;
    }

    public void reset_config() throws Exception {
        var list = new String[]{"網址", "標題", "資料夾", "標籤", "狀態"};
        var last_ele = list[list.length - 1];
        var output = new BufferedWriter(new FileWriter(
                downloads_dir + "config.txt", false));
        for (var tmp : list)
            output.write("\"" + tmp + "\"" + (tmp.contains(last_ele) ? "\n" : ","));
        output.close();
        System.out.println("config.txt resetted");
    }
}