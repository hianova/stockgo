package com.mycompany.stockgo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class data {
    private Elements head, body;
    private final ArrayList<Integer> request;
    private final ArrayList<String>[] req_tag;
    private final boolean has_tag;

    public data(String in, ArrayList<String> request_in) throws Exception {
        var tag = Jsoup.parse(new File(in), "UTF-8").select("tag").text();
        var check = new checksyn();
        var parse = Jsoup.parse(new File(in), check.getTag(tag + "_encode"));
        head = parse.select(check.getTag(tag + "_head"));
        body = parse.select(check.getTag(tag + "_body"));
        request = new ArrayList<>();
        req_tag = new ArrayList[request_in.size()];
        has_tag = request_in.toString().contains("#");

        request_in.forEach((tmp) -> {
            request.add(head.eachText().indexOf(tmp.split("#")[0]));
            if (has_tag) {
                req_tag[request_in.indexOf(tmp)] = new ArrayList<>();
                if (tmp.contains("#")) {
                    for (var count = 1; count < tmp.split("#").length; count++)
                        req_tag[request_in.indexOf(tmp)].add(tmp.split("#")[count]);
                } else
                    req_tag[request_in.indexOf(tmp)].add("");
            }
        });
        if ("mops_twse_com_tw".equals(tag)) head.add(new Element("th").text("備註"));
        if (in.contains("etfDiv")) head = parse.select("thead>tr>th");
    }

    public ArrayList<String> getData() {
        var out = new ArrayList<String>();
        var skip_list = Pattern.compile("(合計｜小計｜總計)");

        body.forEach((body_tmp) -> {
            if (skip_list.matcher(body_tmp.text()).find()) return;
            var line = new ArrayList<String>();
            var line_check = true;

            request.forEach((request_tmp) -> {
                var tmp = body_tmp.child(request_tmp).select("td").text();
                line.add(tmp.isEmpty() ? "null" : tmp);
            });
            if (has_tag) {
                for (var count = 0; count < req_tag.length; count++) {
                    for (var count_tag = 0; count_tag < req_tag[count].size(); count_tag++) {
                        if (req_tag[count].get(count_tag).equals(line.get(count))) {
                            out.addAll(line);
                            return;
                        }
                    }
                }
                line_check = false;
            }
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