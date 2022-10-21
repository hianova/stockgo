package com.mycompany.stockgo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class data {
    private final ArrayList<String> request;
    private Elements head;
    private final Elements body;

    public data(String in, ArrayList<String> request_in) throws Exception {
        var tag = Jsoup.parse(new File(in), "UTF-8").select("tag").text();
        var check = new checksyn();
        var parse = Jsoup.parse(new File(in), check.getTag(tag + "_encode"));
        request = request_in;
        head = parse.select(check.getTag(tag + "_head"));
        body = parse.select(check.getTag(tag + "_body"));

        if ("mops_twse_com_tw".equals(tag)) {
            head.add(new Element("th").text("備註"));
        }
        if (in.contains("etfDiv")) {
            head = parse.select("thead>tr>th");
        }
    }

    public ArrayList<String> getData() {
        var out = new ArrayList<String>();
        var skip_list = Pattern.compile("(合計｜小計｜總計)");

        body.forEach((body_tmp) -> {
            if (skip_list.matcher(body_tmp.text()).find())
                return;
            request.forEach((request_tmp) -> {
                var count = head.eachText().indexOf(request_tmp);
                var tmp = body_tmp.child(count).select("td").text();
                out.add(tmp.isEmpty() ? "null" : tmp);
            });
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