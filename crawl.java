package com.mycompany.stockgo;

import java.net.*;
import java.io.*;

public class crawl {

    private final checksyn check;
    private final String tag;
    private File file;
    private final HttpURLConnection trans;

    public crawl(String in) throws Exception {
        check = new checksyn();
        tag = new URL(in).getHost().replace(".", "_");
        file = new File(check.getDownloads_dir() + check.UrlToName(in));
        trans = (HttpURLConnection) new URL(in).openConnection();
        trans.setConnectTimeout(100);
        trans.setRequestProperty("Referer", in);
        trans.setRequestProperty("User-Agent", check.getUA());
    }

    public void setPath(String in) {
        file = new File(in);
    }

    public void save() throws Exception {
        var trans_input = new InputStreamReader(trans.getInputStream(), check.getTag(tag + "_encode"));
        var file_out = new OutputStreamWriter(new FileOutputStream(file), check.getTag(tag + "_encode"));
        file.createNewFile();
        for (int tmp; (tmp = trans_input.read()) != -1; ) {
            file_out.write(tmp);
        }
        file_out.write("<tag>" + tag + "</tag>");
        trans_input.close();
        file_out.close();
        System.out.print(file.getName() + " saved\n");
    }

    public String getTag() {
        var out = tag;
        return out;
    }
}
