package com.mycompany.stockgo;

import java.io.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class data {
    checksyn check;
    String tag;
    File file;
    Elements head, body;
    ArrayList<String> export_head, export_body;

    public data(String in) throws Exception {
        check = new checksyn();
        tag = Jsoup.parse((file = new File(in)), "UTF-8").select("tag").text();
        var parse = Jsoup.parse(file, check.gettag(tag + "_encode"));
        head = parse.select(check.gettag(tag + "_head"));
        body = parse.select(check.gettag(tag + "_body"));
        export_head = new ArrayList<>();
        export_body = new ArrayList<>();

        if ("mops_twse_com_tw".equals(tag)) {
            head.add(new Element("th").text("備註"));
        }
        if (file.getName().contains("etfDiv")) {
            head = parse.select("thead>tr>th");
        }
    }

    public ArrayList<String> getdata(ArrayList<String> in, boolean timein) {
        var out = new ArrayList<String>();

        if (timein) {
            export_head.add("日期");
        }
        export_head.addAll(in);
        body.forEach((body_tmp) -> {
            if (body_tmp.text().contains("合計") | body_tmp.text().contains("小計") | body_tmp.text().contains("總計")) {
                return;
            }
            if (timein) {
                export_body.add("\"" + check.todate(file.getName()) + "\"");
                out.add(check.todate(file.getName()));
            }
            in.forEach((in_tmp) -> {
                var count = head.eachText().indexOf(in_tmp);
                var tmp = (body_tmp.child(count).select("td").text());

                if (in_tmp.contains("代號")) {
                    tmp = "\"=HYPERLINK(\"\"https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=" +
                            tmp + "\"\",\"\"" + tmp + "" + "\"\")\"";
                    export_body.add(tmp);
                    out.add(tmp);
                } else {
                    if (tmp.isEmpty()) {
                        tmp = "null";
                    }
                    export_body.add("\"" + tmp + "\"");
                    out.add(tmp);
                }
            });
            export_body.add("\n");
            out.add("\n");
        });
        return out;
    }

    public void export(String in, boolean title_in) throws Exception {
        var export_file = new File(in);
        var out_stream = new OutputStreamWriter(new FileOutputStream(in));

        export_file.createNewFile();
        if (title_in) {
            export_head.forEach((tmp) -> {
                try {
                    out_stream.write("\"" + tmp.replace(" ", "") + "\",");
                } catch (IOException e) {
                }
            });
            out_stream.write("\n");
        }
        export_body.forEach((tmp) -> {
            try {
                out_stream.write(tmp);
            } catch (IOException e) {
            }
            if (!tmp.contains("\n")) {
                try {
                    out_stream.write(",");
                } catch (IOException e) {
                }
            }
        });
        out_stream.close();
        System.out.print(file.getName() + " exported\n");
    }
}