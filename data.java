package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class data extends Thread {
    private final ArrayList<String> request, export_date;
    private Elements head, body;

    public data(String in, ArrayList<String> request_in) throws Exception {
        var file = new File(in);
        var tag = Jsoup.parse(file, "UTF-8").select("tag").text();
        var check = new checksyn();
        var parse = Jsoup.parse(file, check.getTag(tag + "_encode"));
        request = request_in;
        export_date = new ArrayList<String>();
        head = parse.select(check.getTag(tag + "_head"));
        body = parse.select(check.getTag(tag + "_body"));

        if ("mops_twse_com_tw".equals(tag)) {
            head.add(new Element("th").text("備註"));
        }
        if (file.getName().contains("etfDiv")) {
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
        export_date.addAll(out);
        return out;
    }

    public void run() {
        getData();
    }

    public ArrayList<String> getExport_data() {
        var out = export_date;
        return out;
    }

    public ArrayList<Element> getHead() {
        var out = head;
        return out;
    }

    public ArrayList<Element> getBody() {
        var out = body;
        return out;
    }

}