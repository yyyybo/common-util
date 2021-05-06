package com.yibo.common.http;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * http client工厂类
 *
 * @author 莫问
 * @date 2019-06-10
 */
class HttpClientFactory {

    private static HttpClient httpClient;

    private HttpClientFactory() {
        // 创建异步HTTP连接池
        httpClient = new HttpClient();
    }

    static HttpClientFactory getInstance() {
        return PluginConfigHolder.HTTP_CLIENT_FACTORY;
    }

    CloseableHttpClient getHttpClient() {
        return httpClient.getHttpClient();
    }

    private static class PluginConfigHolder {
        private final static HttpClientFactory HTTP_CLIENT_FACTORY = new HttpClientFactory();
    }
}