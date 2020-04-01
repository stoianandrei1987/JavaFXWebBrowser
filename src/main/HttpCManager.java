package main;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpCManager {

    private static PoolingHttpClientConnectionManager cm;

    public static PoolingHttpClientConnectionManager getCm() {
        return cm;
    }

    public static void createCmanager()
    {
        if(cm==null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(20);
        }
    }
}
