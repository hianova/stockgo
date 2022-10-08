package com.mycompany.stockgo;

import java.io.*;
import java.util.*;
import java.net.*;

public class checksyn {
    private final Random random;
    private final Hashtable<String, String> tag;
    private final String downloads_dir;
    private ArrayList<String> num_stock, num_ETF;

    public checksyn() throws Exception {
        random = new Random();
        tag = new Hashtable<>();
        downloads_dir = System.getProperty("user.dir") + System.getProperty("file.separator") +
                "downloads" + System.getProperty("file.separator");
        num_stock = new ArrayList<>();
        num_ETF = new ArrayList<>();

        var file = new BufferedReader(new FileReader(System.getProperty("user.dir") +
                System.getProperty("file.separator") + "parse_rule.txt"));
        for (var file_tmp = ""; (file_tmp = file.readLine()) != null; ) {
            var tmp = file_tmp.trim().replaceAll("\"", "").split(":");
            tag.put(tmp[0], tmp[1]);
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

    public ArrayList<String> getStock_num() throws Exception {
        var out = new ArrayList<String>();
        if (num_stock.isEmpty())
            num_stock = new data(downloads_dir + "上市股票" + System.getProperty("file.separator") +
                    "isin_C_public.txt").getData(new ArrayList<>(List.of("有價證券代號及名稱")));

        out = num_stock;
        return out;
    }

    public ArrayList<String> getETF_num() throws Exception {
        var out = new ArrayList<String>();
        if (num_ETF.isEmpty())
            num_ETF = new data(downloads_dir + "投資信託基金" + System.getProperty("file.separator") +
                    "isin_C_public.txt").getData(new ArrayList<>(List.of("有價證券代號及名稱")));
        out = num_ETF;
        return out;
    }

    public String getDownloads_dir() {
        var out = downloads_dir;
        return out;
    }

    public String getUA() throws Exception {
        var out = "";
        var file = new BufferedReader(new FileReader(System.getProperty("user.dir") +
                System.getProperty("file.separator") + "useragent.txt"));
        var file_tmp = "";

        for (var tmp = ""; (tmp = file.readLine()) != null; ) {
            file_tmp = file_tmp.concat(tmp + "\n");
        }
        file.close();
        var UA = file_tmp.replaceAll("\"", "").split(",");
        out = UA[random.nextInt(UA.length)].trim();
        return out;
    }
}