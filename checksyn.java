package com.mycompany.stockgo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class checksyn {
    private final Random random;
    private final Hashtable<String, String> tag;
    private final String downloads_dir, strategy_dir;
    private final ArrayList<String> num_stock;
    private final ArrayList<String> num_ETF;

    public checksyn() throws Exception {
        random = new Random();
        tag = new Hashtable<>();
        downloads_dir = System.getProperty("user.dir") + System.getProperty("file.separator") +
                "downloads" + System.getProperty("file.separator");
        strategy_dir = System.getProperty("user.dir") + System.getProperty("file.separator") +
                "strategy" + System.getProperty("file.separator");
        num_stock = new ArrayList<>();
        num_ETF = new ArrayList<>();
        var parse_rule = new BufferedReader(new FileReader(System.getProperty("user.dir") +
                System.getProperty("file.separator") + "parse_rule.txt"));
        for (var parse_tmp = ""; (parse_tmp = parse_rule.readLine()) != null; ) {
            var tmp = parse_tmp.split("\":\"");
            tag.put(tmp[0].replaceAll("\"", ""), tmp[1].replaceAll("\"", ""));
        }
    }

    public String UrlToName(String in) throws Exception {
        String[] list = {"\\.\\w+", "_@num", "_@date", "@num", "@date"};
        var url = new URL(in).getPath();
        for (var tmp : list) {
            url = url.replaceAll(tmp, "");
        }
        var in_tmp = url.split("/");
        var out = in_tmp[in_tmp.length - 2] + "_" + in_tmp[in_tmp.length - 1];
        return out;
    }

    public String getTag(String in) {
        var out = tag.get(in);
        return out;
    }

    public ArrayList<String> getNum(String in) throws Exception {
        var out = new ArrayList<String>();
        switch (in) {
            case "stock" -> out.addAll(new checksyn().getStock_num());
            case "ETF" -> out.addAll(new checksyn().getETF_num());
        }
        return out;
    }

    public ArrayList<String> getStock_num() throws Exception {
        var out = new ArrayList<String>();
        var pattern = Pattern.compile("^[0-9]{4}　");

        if (num_stock.isEmpty()) {
            new data(downloads_dir + "上市證券代號" + System.getProperty("file.separator") +
                    "isin_C_public.txt", new ArrayList<>(List.of("有價證券代號及名稱"))).getData().forEach((tmp) -> {
                if (pattern.matcher(tmp).find())
                    num_stock.add(tmp.split("　")[0]);
            });
            new data(downloads_dir + "上櫃證券代號" + System.getProperty("file.separator") +
                    "isin_C_public.txt", new ArrayList<>(List.of("有價證券代號及名稱"))).getData().forEach((tmp) -> {
                if (pattern.matcher(tmp).find())
                    num_stock.add(tmp.split("　")[0]);
            });
        }
        out = num_stock;
        return out;
    }

    public ArrayList<String> getETF_num() throws Exception {
        var out = new ArrayList<String>();
        var pattern = Pattern.compile("^T[0-9]+\\w");

        if (num_ETF.isEmpty())
            new data(downloads_dir + "基金＿國際證券代號" + System.getProperty("file.separator") +
                    "isin_C_public.txt", new ArrayList<>(List.of("有價證券代號及名稱"))).getData().forEach((tmp) -> {
                if (pattern.matcher(tmp).find())
                    num_ETF.add(tmp.split("　")[0]);
            });
        out = num_ETF;
        return out;
    }

    public String getDownloads_dir() {
        var out = downloads_dir;
        return out;
    }

    public String getStrategy_dir() {
        var out = strategy_dir;
        return out;
    }

    public String getUA() throws Exception {
        var out = "";
        var file = new BufferedReader(new FileReader(System.getProperty("user.dir") +
                System.getProperty("file.separator") + "useragent.txt"));
        var UA = new ArrayList<String>();
        for (var tmp = ""; (tmp = file.readLine()) != null; ) {
            UA.add(tmp);
        }
        out = UA.get(random.nextInt(UA.size()));
        return out;
    }

}