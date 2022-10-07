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
    private final ArrayList<String> export_head, export_body;

    public data(String in) throws Exception {
        file = new File(in);
        check = new checksyn();
        tag = Jsoup.parse(file, "UTF-8").select("tag").text();
        var parse = Jsoup.parse(file, check.getTag(tag + "_encode"));
        head = parse.select(check.getTag(tag + "_head"));
        body = parse.select(check.getTag(tag + "_body"));
        export_head = new ArrayList<>();
        export_body = new ArrayList<>();

        if ("mops_twse_com_tw".equals(tag)) {
            head.add(new Element("th").text("備註"));
        }
        if (file.getName().contains("etfDiv")) {
            head = parse.select("thead>tr>th");
        }
    }

    public ArrayList<String> getdata(ArrayList<String> in) {
        var out = new ArrayList<String>();

        export_head.addAll(in);
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
                export_body.add(tmp);
            });
        });
        out.addAll(export_body);
        return out;
    }

    public void export(String in, boolean title_in) throws Exception {
        var export_file = new File(in);
        export_file.createNewFile();
        var out_stream = new OutputStreamWriter(new FileOutputStream(in));

        if (title_in) {
            export_head.forEach((tmp) -> {
                try {
                    var is_last = export_head.indexOf(tmp) == (export_head.size() - 1) ? "\n" : ",";
                    out_stream.write("\"" + tmp + "\"" + is_last);
                } catch (IOException e) {
                    System.out.println("head can't output");
                }
            });
        }
        export_body.forEach((tmp) -> {
            try {
                var is_last = (export_body.indexOf(tmp) + 1) % export_head.size() == 0 ? "\n" : ",";
                out_stream.write("\"" + tmp + "\"" + is_last);
            } catch (IOException e) {
                System.out.println("body can't output");
            }
        });
        out_stream.close();
        System.out.print(export_file.getName() + " exported\n");
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