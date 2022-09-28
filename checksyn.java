package com.mycompany.stockgo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;

public class checksyn {

    Dictionary<String, String[]> tag = new Hashtable<>();
    String[] stocknum, ETFnum;

    public checksyn() {
        tag.put("www_twse_com_tw", new String[]{"div>table"});
        tag.put("www_twse_com_tw_G.P", new String[]{"GET"});
        tag.put("www_twse_com_tw_encode", new String[]{"UTF-8"});
        tag.put("www_twse_com_tw_head", new String[]{"thead>tr:gt(0)>td"});
        tag.put("www_twse_com_tw_body", new String[]{"tbody>tr"});

        tag.put("mops_twse_com_tw", new String[]{"body>center>center"});
        tag.put("mops_twse_com_tw_G.P", new String[]{"POST"});
        tag.put("mops_twse_com_tw_encode", new String[]{"big5"});
        tag.put("mops_twse_com_tw_head", new String[]{"td>table[border=0]:eq(1) tbody>tr:eq(1)>th.tt"});
        tag.put("mops_twse_com_tw_body", new String[]{"td>table>tbody>tr:gt(1)"});

        tag.put("pchome_megatime_com_tw", new String[]{"div#bttb>table"});
        tag.put("pchome_megatime_com_tw_G.P", new String[]{"GET"});
        tag.put("pchome_megatime_com_tw_encode", new String[]{"UTF-8"});
        tag.put("pchome_megatime_com_tw_head", new String[]{"div#bttb>table>tbody>tr>th"});
        tag.put("pchome_megatime_com_tw_body", new String[]{"div#bttb>table>tbody"});

        stocknum = "".split(",");
        ETFnum = "".split(",");
    }

    public String gettag(String in) {
        var out = tag.get(in);
        return out[new Random().nextInt(out.length)];
    }

    public String totag(String in) throws Exception {
        var out = new URL(in).getHost().replace(".", "_");
        return out;
    }

    public ArrayList<String> getstocknum() {
        var out = new ArrayList<String>();
        Collections.addAll(out, stocknum);
        return out;
    }

    public ArrayList<String> getETFnum() {
        var out = new ArrayList<String>();
        Collections.addAll(out, ETFnum);
        return out;
    }
    public String todate(String in) {
        Matcher tmp = Pattern.compile("\\d{8}").matcher(in);
        var out = tmp.group();
        return out;
    }

    public String toname(String in) throws Exception {
        var url = new URL(in).getPath();
        var in_tmp = url.split("/");
        var out = in_tmp[in_tmp.length - 2] + "_" + in_tmp[in_tmp.length - 1]
                .concat(".txt");
        return out;
    }
}
