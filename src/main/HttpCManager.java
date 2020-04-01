package main;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpCManager {

    private static PoolingHttpClientConnectionManager cm;
    private static HttpClient client;

    public static PoolingHttpClientConnectionManager getCm() {
        return cm;
    }

    public static HttpClient getClient(){
        return client;
    }

    public static void initialize()
    {
        if(cm==null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(20);
        }

        if(client==null) {
            client = HttpClients.custom().setConnectionManager(cm).build();
        }
    }
}
