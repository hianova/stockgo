package com.mycompany.stockgo;

import java.net.*;
import java.io.*;

public class crawl {

    checksyn check;
    String tag;
    File file;
    HttpURLConnection trans;

    public crawl(String in) throws Exception {
        check = new checksyn();
        tag = check.totag(in);
        trans = (HttpURLConnection) new URL(in).openConnection();
        trans.setRequestProperty("Referer", in);
        trans.setRequestProperty("User-Agent", new randomUA().getUA());
    }

    public void setpath(String in) {
        file = new File(in);
    }

    public void save() throws Exception {
        var trans_input = new InputStreamReader(trans.getInputStream(), check.gettag(tag + "_encode"));
        var file_out = new OutputStreamWriter(new FileOutputStream(file), check.gettag(tag + "_encode"));
        file.createNewFile();
        for (int tmp; (tmp = trans_input.read()) != -1; ) {
            file_out.write(tmp);
        }
        file_out.write("<tag>" + tag + "</tag>");
        trans_input.close();
        file_out.close();
        System.out.print(file.getName() + " saved\n");
    }
}
