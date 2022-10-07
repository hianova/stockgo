package com.mycompany.stockgo;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.*;

public class checksyn {

    private final Dictionary<String, String> tag;
    private final String downloads_dir;
    private final DateTimeFormatter uni_date;
    private final String[] num_stock;
    private final String[] num_ETF;

    public checksyn() {
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


    public String toName(String in) throws Exception {
        String[] list = {"\\.\\w+", "_@num", "_@date", "@num", "@date"};
        var url = new URL(in).getPath();
        for (var tmp : list) {
            url = url.replaceAll(tmp, "");
        }
        var in_tmp = url.split("/");
        var out = in_tmp[in_tmp.length - 2] + "_" + in_tmp[in_tmp.length - 1];
        return out;
    }

    public void renew_list() throws Exception {
        String[] list = new String[]{
                "https://isin.twse.com.tw/isin/C_public.jsp?strMode=2",
                "https://isin.twse.com.tw/isin/C_public.jsp?strMode=4",
                "https://isin.twse.com.tw/isin/C_public.jsp?strMode=7",};

        for (var tmp : list) {
            var crawl = new crawl(tmp);
            crawl.setPath(System.getProperty("user.dir") + System.getProperty("file.separator") + toName(tmp) + ".txt");
            crawl.save();
        }
    }

    public String getTag(String in) {
        var out = tag.get(in);
        return out;
    }

    public ArrayList<String> getstocknum() {
        var out = new ArrayList<String>();
        Collections.addAll(out, num_stock);
        return out;
    }

    public ArrayList<String> getETFnum() {
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
}


