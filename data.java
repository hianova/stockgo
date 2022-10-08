package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class data {
    private final File file;
    private final checksyn check;
    private final String tag;
    private Elements head, body;

    public data(String in) throws Exception {
        file = new File(in);
        check = new checksyn();
        tag = Jsoup.parse(file, "UTF-8").select("tag").text();
        var parse = Jsoup.parse(file, check.getTag(tag + "_encode"));
        head = parse.select(check.getTag(tag + "_head"));
        body = parse.select(check.getTag(tag + "_body"));

        if ("mops_twse_com_tw".equals(tag)) {
            head.add(new Element("th").text("備註"));
        }
        if (file.getName().contains("etfDiv")) {
            head = parse.select("thead>tr>th");
        }
    }

    public ArrayList<String> getData(ArrayList<String> in) {
        var out = new ArrayList<String>();

        body.forEach((body_tmp) -> {
            if (
                    body_tmp.text().contains("合計") |
                            body_tmp.text().contains("小計") |
                            body_tmp.text().contains("總計"))
                return;

            in.forEach((in_tmp) -> {
                var count = head.eachText().indexOf(in_tmp);
                var tmp = (body_tmp.child(count).select("td").text());

                if (tmp.isEmpty()) {
                    tmp = "null";
                }
            });
        });
        return out;
    }

    public String getTag() {
        var out = tag;
        return out;
    }

    public String getHead() {
        var out = head.stream().toString();
        return out;
    }

    public String getBody() {
        var out = body.select("td").text();
        return out;
    }
}