package com.zst.registrycenter.transport.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpInvoker {
    private CloseableHttpAsyncClient httpAsyncClient = null;
    private HttpInvokerProperties prop;

    public HttpInvoker() {
        this.prop = new HttpInvokerProperties();
    }

    public HttpInvoker(HttpInvokerProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("HttpInvokerProperties is null");
        }
        this.prop = properties;
    }

    public CompletableFuture<HttpResponse> doGet(String url, Map<String, String> header, Map<String, String> params) {
        HttpGet req = null;
        try {
            URIBuilder uriBuilder = new URIBuilder(URI.create(url));
            if (params != null) {
                params.forEach(uriBuilder::addParameter);
            }

            req = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URL地址处理出错", e);
        }

        if (header != null) {
            header.forEach(req::addHeader);
        }

        return execute(req);
    }

    public void close() {
        if (httpAsyncClient != null) {
            try {
                httpAsyncClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            httpAsyncClient = null;
        }
    }

    private CompletableFuture<HttpResponse> execute(HttpUriRequest request) {
        CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        getAsyncClient().execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                future.complete(result);
            }

            @Override
            public void failed(Exception ex) {
                future.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                future.cancel(true);
            }
        });

        return future;
    }

    private HttpAsyncClient getAsyncClient() {
        if (httpAsyncClient != null) {
            return httpAsyncClient;
        }
        synchronized (this) {
            try {
                TrustManager[] tm = new TrustManager[]{new NoTrustManager()};
                SSLContext ssl = SSLContext.getInstance("TLS");
                ssl.init((KeyManager[]) null, tm, new SecureRandom());

                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(prop.getConnectTimeoutMs())
                        .setSocketTimeout(prop.getConnectTimeoutMs())
                        .build();

                CloseableHttpAsyncClient newClient = HttpAsyncClients.custom()
                        .setSSLHostnameVerifier(new NoHostnameVerifier())
                        .setSSLContext(ssl)
                        .setDefaultRequestConfig(requestConfig)
                        .setMaxConnTotal(1000)
                        .setMaxConnPerRoute(1000)
                        .build();
                newClient.start();
                httpAsyncClient = newClient;
            } catch (NoSuchAlgorithmException | KeyManagementException e1) {
                throw new RuntimeException("初始化HTTP客户端时发生错误", e1);
            }
        }
        return httpAsyncClient;
    }

    private static class NoTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class NoHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
