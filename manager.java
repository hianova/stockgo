package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class manager {
    String downloads_dir;
    checksyn check;
    DateTimeFormatter uni_date;
    ArrayList<String> label, label_url, label_title, label_folder, label_tag, label_status;

    public manager() throws Exception {
        downloads_dir = System.getProperty("user.dir") +
                System.getProperty("file.separator") +
                "downloads" + System.getProperty("file.separator");
        var config = new File(downloads_dir + "config.txt");
        uni_date = DateTimeFormatter.ofPattern("yyyyMMdd");
        check = new checksyn();
        label = new ArrayList<>();
        label_url = new ArrayList<>();
        label_title = new ArrayList<>();
        label_folder = new ArrayList<>();
        label_tag = new ArrayList<>();
        label_status = new ArrayList<>();
        var input = new BufferedReader(new FileReader(config));

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
        new File(downloads_dir + in.get(2)).mkdir();
        download(in.get(0));
        label_status.set(label_url.indexOf(in.get(0)), "\"" +
                LocalDate.now().format(uni_date) + "\"");
        sync_config();
        System.out.println(in.get(1)+" added");
    }

    public void update() {
        label_status.forEach((tmp) -> {
            if (Period.between(LocalDate.parse(tmp, uni_date),
                    LocalDate.now()).getMonths() < 1) {
                try {
                    download(label_url.get(label_status.indexOf(tmp)));
                    label_status.set(label_status.indexOf(tmp), "\"" +
                            LocalDate.now().format(uni_date) + "\"");
                    sync_config();
                } catch (Exception ex) {
                }
            }
        });
    }

    public void sync_config() throws Exception {
        var output = new BufferedWriter(new FileWriter(
                downloads_dir + "config.txt", false));

        for (var count = 0; count < label.size(); count++) {
            output.write("\"" + label.get(count) + "\"");
            if (count == label.size()) {
                output.write("\n");
            } else {
                output.write(",");
            }
        }
        output.write("\n");
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
        var path = new ArrayList<String>();
        path.add(String.valueOf(path.add(downloads_dir +
                label_folder.get(label_url.indexOf(in)) +
                System.getProperty("file.separator") + check.toname(in))));

        batch_num(in).forEach((num) -> {
            try {
                batch_time(in).forEach((time) -> {
                    try {
                        var in_tmp = in
                                .replaceAll("@date:\\w*:[YMWD]", time)
                                .replaceAll("@(ETF|stock)num", num);
                        var crawl = new crawl(in_tmp);
                        if (in.matches("@(ETF|stock)num"))
                            path.add("_" + num);
                        if (in.matches("@date:\\w*:[YMWD]"))
                            path.add("_" + LocalDate.parse(time,uni_date));
                        crawl.setpath(path + ".txt");
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

        if (!in.matches("@date")) {
            out.add("null");
            return out;
        }
        var tmp = Pattern.compile("@date:\\w*:[YMWD]")
                .matcher("in").group().split(":");

        for (var date_tmp = LocalDate.parse(label_status.get(label_url.indexOf(in)));
             date_tmp.isBefore(LocalDate.now()); ) {
            out.add(date_tmp.format(DateTimeFormatter.ofPattern(tmp[1])));
            switch (tmp[2]) {
                case "Y" -> date_tmp=date_tmp.plusYears(1);
                case "M" -> date_tmp=date_tmp.plusMonths(1);
                case "W" -> date_tmp=date_tmp.plusWeeks(1);
                case "D" -> date_tmp=date_tmp.plusDays(1);
            }
        }
        return out;
    }

    public ArrayList<String> batch_num(String in) {
        var out = new ArrayList<String>();

        if (!in.matches("@num")) {
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
}