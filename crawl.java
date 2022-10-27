package com.mycompany.stockgo;

import java.io.*;
import javax.net.ssl.*;
import java.net.URL;
import java.security.cert.X509Certificate;

public class crawl extends Thread {

    private final checksyn check;
    private final String tag;
    private File file;
    private final HttpsURLConnection trans;

    public crawl(String in) throws Exception {
        check = new checksyn();
        tag = new URL(in).getHost().replace(".", "_");
        file = new File(check.getDownloads_dir() + check.UrlToName(in) + ".txt");
        trans = (HttpsURLConnection) new URL(in).openConnection();
        trans.setRequestProperty("Referer", in);
        trans.setRequestProperty("User-Agent", check.getUA());
        var sc = SSLContext.getInstance("TLSv1.2", "SunJSSE");
        sc.init(null, new TrustManager[]{new ssl_cer()}, new java.security.SecureRandom());
        HostnameVerifier ignoreHostnameVerifier = (s, ssl) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
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
            System.out.println("crawling went wrong");
        }
    }
}

class ssl_cer implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] certificates, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate, String s) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}