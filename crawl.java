package com.mycompany.stockgo;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

public class crawl extends Thread {

    private final checksyn check;
    private final String tag;
    private File file;
    private final HttpURLConnection trans;

    public crawl(String in) throws Exception {
        var in_tmp = Pattern.compile("https").matcher(in).find() ?
                in.replace("https", "http") : in;
        check = new checksyn();
        tag = new URL(in_tmp).getHost().replace(".", "_");
        file = new File(check.getDownloads_dir() + check.UrlToName(in_tmp) + ".txt");
        trans = (HttpURLConnection) new URL(in_tmp).openConnection();
        trans.setRequestProperty("Referer", in_tmp);
        trans.setRequestProperty("User-Agent", check.getUA());
    }

    public void setPath(String in) {
        file = new File(in);
    }

    public void save() throws Exception {
        var trans_input = new InputStreamReader(trans.getInputStream(), check.getTag(tag + "_encode"));
        var file_out = new OutputStreamWriter(new FileOutputStream(file), check.getTag(tag + "_encode"));
        file.createNewFile();

        for (int tmp; (tmp = trans_input.read()) != -1; )
            file_out.write(tmp);
        file_out.write("<tag>" + tag + "</tag>");
        trans_input.close();
        file_out.close();
        System.out.print(file.getName() + " saved\n");
    }

    public void run() {
        try {
            save();
        } catch (Exception e) {
            System.out.println("crawling went wrong ");
        }
    }
}
