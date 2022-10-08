package com.mycompany.stockgo;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.*;

public class checksyn {
    private final Random random;
    private final Dictionary<String, String> tag;
    private final String downloads_dir;
    private final DateTimeFormatter uni_date;
    private final String[] num_stock;
    private final String[] num_ETF;

    public checksyn() {
        random = new Random();
        tag = new Hashtable<>();
        tag.put("www_twse_com_tw", "div>table");
        tag.put("www_twse_com_tw_G.P", "GET");
        tag.put("www_twse_com_tw_encode", "UTF-8");
        tag.put("www_twse_com_tw_head", "thead>tr:gt(0)>td");
        tag.put("www_twse_com_tw_body", "tbody>tr");

        tag.put("mops_twse_com_tw", "body>center>center");
        tag.put("mops_twse_com_tw_G.P", "POST");
        tag.put("mops_twse_com_tw_encode", "big5");
        tag.put("mops_twse_com_tw_head", "td>table[border=0]:eq(1) tbody>tr:eq(1)>th.tt");
        tag.put("mops_twse_com_tw_body", "td>table>tbody>tr:gt(1)");

        tag.put("pchome_megatime_com_tw", "div#bttb>table");
        tag.put("pchome_megatime_com_tw_G.P", "GET");
        tag.put("pchome_megatime_com_tw_encode", "UTF-8");
        tag.put("pchome_megatime_com_tw_head", "div#bttb>table>tbody>tr>th");
        tag.put("pchome_megatime_com_tw_body", "div#bttb>table>tbody");

        num_stock = "".split(",");
        num_ETF = "".split(",");
        downloads_dir = System.getProperty("user.dir") +
                System.getProperty("file.separator") +
                "downloads" + System.getProperty("file.separator");
        uni_date = DateTimeFormatter.ofPattern("yyyyMMdd");
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

    public ArrayList<String> getStock_num() {
        var out = new ArrayList<String>();
        Collections.addAll(out, num_stock);
        return out;
    }

    public ArrayList<String> getETF_num() {
        var out = new ArrayList<String>();
        Collections.addAll(out, num_ETF);
        return out;
    }

    public String getDownloads_dir() {
        var out = downloads_dir;
        return out;
    }

    public DateTimeFormatter getUni_date() {
        var out = uni_date;
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


