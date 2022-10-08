package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class config {
    checksyn check;
    String downloads_dir;
    DateTimeFormatter uni_date;
    ArrayList<String> label, label_url, label_title, label_folder, label_tag, label_status;

    public config() throws Exception {

        check = new checksyn();
        downloads_dir = check.getDownloads_dir();
        uni_date = check.getUni_date();
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

    public ArrayList<String> batch_time(String in, String st_ed) {
        var out = new ArrayList<String>();
        var session = label_url.lastIndexOf(in);
        if (!in.contains("@date")) {
            out.add("null");
            return out;
        }
        var tag_tmp = label_tag.get(session).split("@");
        for (var tmp : tag_tmp) {
            if (tmp.contains("date")) {
                tag_tmp = tmp.split(":");
                break;
            }
        }
        var is_DC = Pattern.compile("yyyy").matcher(tag_tmp[1]).find();
        var st_ed_tmp = (st_ed.matches("\\d*~\\d*") ? st_ed :
                label_status.get(session) + "~" + LocalDate.now().format(uni_date)).split("~");

        for (var date_tmp = LocalDate.parse(st_ed_tmp[0], uni_date);
             date_tmp.isBefore(LocalDate.parse(st_ed_tmp[1], uni_date)); ) {
            var tmp = is_DC ? date_tmp : date_tmp.minusYears(1911);
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

    public ArrayList<String> batch_num(String in, String select_num_in) {
        var out = new ArrayList<String>();
        if (!in.contains("@num")) {
            out.add("null");
            return out;
        }
        if (select_num_in.matches("(\\w+|\\w+(,\\w+)+)")) {
            var tmp = Pattern.compile("\\w+").matcher(select_num_in);
            for (int count = 0; count < tmp.groupCount(); count++) {
                out.add(tmp.group(count));
            }
            return out;
        }

        var tmp = Pattern.compile("@num:\\w*")
                .matcher("in").group().split(":");

        switch (tmp[1]) {
            case "stock" -> out = check.getStock_num();
            case "ETF" -> out = check.getETF_num();
        }
        return out;
    }


}
