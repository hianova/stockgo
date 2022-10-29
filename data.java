package com.mycompany.stockgo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class data {
    private final ArrayList<Integer> request;
    private final ArrayList<String>[] req_tag;
    private Elements head, body;

    public data(String in, ArrayList<String> request_in) throws Exception {
        var tag = Jsoup.parse(new File(in), "UTF-8").select("tag").text();
        var check = new checksyn();
        var parse = Jsoup.parse(new File(in), check.getTag(tag + "_encode"));
        request = new ArrayList<>();
        req_tag = new ArrayList[request_in.size()];
        head = parse.select(check.getTag(tag + "_head"));
        body = parse.select(check.getTag(tag + "_body"));

        request_in.forEach((tmp) -> {
            request.add(head.eachText().indexOf(tmp.split("#")[0]));
            if (tmp.contains("#")) {
                req_tag[request_in.indexOf(tmp)] = new ArrayList<>();
                for (var count = 1; count < tmp.split("#").length; count++) {
                    req_tag[request_in.indexOf(tmp)].add(tmp.split("#")[count]);
                }
            } else {
                req_tag[request_in.indexOf(tmp)] = new ArrayList<>() {{
                    add("");
                }};
            }
        });
        if ("mops_twse_com_tw".equals(tag)) head.add(new Element("th").text("備註"));
        if (in.contains("etfDiv")) head = parse.select("thead>tr>th");
    }

    public ArrayList<String> getData() {
        var out = new ArrayList<String>();
        var skip_list = Pattern.compile("(合計｜小計｜總計)");

        body.forEach((body_tmp) -> {
            var line = new ArrayList<String>();
            var line_check = false;

            if (skip_list.matcher(body_tmp.text()).find()) return;
            request.forEach((request_tmp) -> {
                var tmp = body_tmp.child(request_tmp).select("td").text();
                line.add(tmp.isEmpty() ? "null" : tmp);
            });
            if (Arrays.stream(req_tag).allMatch(ArrayList::isEmpty)) {
                for (var count_line = 0; count_line < line.size(); count_line++) {
                    for (var count = 0; count < req_tag[count_line].size(); count++) {
                        if (req_tag[count_line].get(count).isBlank()) break;
                        if (line.get(count_line).contains(req_tag[count_line].get(count))) line_check = true;
                    }
                }
            } else line_check = true;
            if (line_check) out.addAll(line);
        });
        return out;
    }

    public ArrayList<String> getHead() {
        var out = (ArrayList<String>) head.eachText();
        return out;
    }

    public ArrayList<String> getBody() {
        var out = (ArrayList<String>) body.eachText();
        return out;
    }

}


//            for (var count = 0; count < req_tag.length; count++) {
//                if (req_tag[count] == null) {
//                    line_check = true;
//                    break;
//                }
//                if (req_tag[count].get(0).isBlank()) continue;
//                for (var count_tag = 0; count_tag < req_tag[count].size(); count_tag++) {
//                    if (!line.get(count).contains(req_tag[count].get(count_tag))) {
//                        line_check = true;
//                        break;
//                    }
//                }
//            }
//            if (!line_check) return;