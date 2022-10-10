package com.mycompany.stockgo;

import java.net.*;
import java.io.*;
import java.net.URL;
import java.security.cert.*;
import javax.net.ssl.*;

public class crawl extends Thread {

    private final checksyn check;
    private final String tag;
    private File file;
    private final HttpsURLConnection trans;

    public crawl(String in) throws Exception {
        check = new checksyn();
        tag = new URL(in).getHost().replace(".", "_");
        file = new File(check.getDownloads_dir() + check.UrlToName(in) + ".txt");
        trans = (HttpsURLConnection) new URL(in)
                .openConnection();//new Proxy(Proxy.Type.HTTP, check.getProxy()));
        trans.setConnectTimeout(300);
        trans.setRequestProperty("Referer", in);
        trans.setRequestProperty("User-Agent", check.getUA());
        var sc = SSLContext.getInstance("TLSv1.2", "SunJSSE");
        sc.init(null, new TrustManager[]{new TrustManager()},
                new java.security.SecureRandom());
        HostnameVerifier ignoreHostnameVerifier = (s, sslsession) -> true;
        trans.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        trans.setDefaultSSLSocketFactory(sc.getSocketFactory());
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

    public void run() {
        try {
            save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTag() {
        var out = tag;
        return out;
    }

    public HttpURLConnection getTrans() {
        var out = trans;
        return out;
    }

}

class TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate certificates[],
                                   String authType) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate,
                                   String s) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}