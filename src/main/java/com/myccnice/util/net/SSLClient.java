package com.myccnice.util.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

@SuppressWarnings("deprecation")
public class SSLClient extends DefaultHttpClient {
    private static final int DEFAULT_PORT = 443;

    public SSLClient() throws Exception{
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new TrustAnyTrustManager()}, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", DEFAULT_PORT, ssf));
    }

    /**
     * @category 自定义信任管理器
     * @author wangpeng
     * @date 2014年4月12日下午
     */
    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // do nothing
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // do nothing
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }
}

