package com.opc.common.http;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.SSLContext;
import java.nio.charset.CodingErrorAction;
import java.security.NoSuchAlgorithmException;

/**
 * 异步HTTP连接池
 *
 * @author 莫问
 * @date 2019-06-10
 */
@Slf4j
public class HttpClient {

    /**
     * 设置等待数据超时时间3秒钟(接口太慢可以设置大一点)
     */
    private static final int SOCKET_TIMEOUT = 3000;

    /**
     * 连接超时
     */
    private static final int CONNECT_TIMEOUT = 3000;

    /**
     * 连接请求超时
     */
    private static final int CONNECT_REQUEST_TIMEOUT = 3000;

    /**
     * 连接池最大连接数
     */
    private static final int POOL_SIZE = 2000;

    /**
     * 每个主机的最大并发数
     */
    private static final int MAX_PER_ROUTE = 2000;

    /**
     * HTTP客户端
     */
    @Getter
    private CloseableHttpClient httpClient;

    /**
     * 构造器
     */
    public HttpClient() {
        try {
            this.httpClient = getClient(getConnManager());
        } catch (Exception e) {
            log.error("初始化异步http客户端异常", e);
        }

    }

    /**
     * 创建异步的httpClient对象
     *
     * @param connManager 连接管理器可以调用本类的getConnManager生成
     * @return 异步的httpClient对象
     */
    private static CloseableHttpClient getClient(PoolingHttpClientConnectionManager connManager) {
        if (null == connManager) {
            return null;
        }
        // 设置连接参数
        RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(SOCKET_TIMEOUT)
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setConnectionRequestTimeout(CONNECT_REQUEST_TIMEOUT)
            .build();

        Lookup<AuthSchemeProvider> authSchemeRegistry =
            RegistryBuilder.<AuthSchemeProvider>create().register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build();

        // 创建自定义的httpclient对象
        return HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setDefaultAuthSchemeRegistry(authSchemeRegistry)
            .setConnectionManager(connManager)
            .build();
    }

    /**
     * 初始化连接管理器
     *
     * @return 连接管理器
     */
    private static PoolingHttpClientConnectionManager getConnManager() {

        try {
            // 证书验证，处理https请求
            SSLContext sslcontext = SSLContext.getDefault();
            SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.getDefaultHostnameVerifier());

            // 设置协议http和https对应的处理socket链接工厂的对象
            Registry<ConnectionSocketFactory> sessionStrategyRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create().register("http",
                    PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();

            PoolingHttpClientConnectionManager connManager =
                new PoolingHttpClientConnectionManager(sessionStrategyRegistry);
            connManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
            connManager.setMaxTotal(POOL_SIZE);

            //连接相关配置
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .build();
            connManager.setDefaultConnectionConfig(connectionConfig);

            return connManager;
        } catch (NoSuchAlgorithmException e) {
            log.error("I/O调度工作异常终止", e);
        } catch (Exception e) {
            log.error("创建http客户端出现未知异常", e);
        }
        return null;
    }
}