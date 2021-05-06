package com.yibo.common.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.UUID;

/**
 * 根据请求响应结果以及将响应结果转换Json
 *
 * @author 莫问
 */
@Slf4j
public class LocalHttpClient {

    protected static CloseableHttpClient httpClient = HttpClientFactory.getInstance()
        .getHttpClient();

    /**
     * 根据 请求返回 response 响应
     *
     * @param request 请求参数
     * @return 响应结果
     */
    public static CloseableHttpResponse execute(HttpUriRequest request) {
        loggerRequest(request);
        try {
            return httpClient.execute(request, HttpClientContext.create());
        } catch (Exception e) {
            log.error("http执行请求出现未知异常 【{}】", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 自动解析Json对象时执行的http请求 (用于 executeJsonResult 方法调用)
     *
     * @param request         请求参数
     * @param responseHandler 响应结果解析Handler
     * @param <T>             泛型
     * @return 解析结果
     */
    public static <T> T execute(HttpUriRequest request, ResponseHandler<T> responseHandler) {
        String uriId = loggerRequest(request);
        if (responseHandler instanceof BaseLocalResponseHandler) {
            BaseLocalResponseHandler lrh = (BaseLocalResponseHandler)responseHandler;
            lrh.setUriId(uriId);
        }
        try {
            return httpClient.execute(request, responseHandler, HttpClientContext.create());
        } catch (Exception e) {
            // 监控
            log.error("http执行请求出现未知异常 【{}】", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 数据返回自动JSON对象解析
     *
     * @param request request
     * @param clazz   clazz
     * @return result
     */
    public static <T> T executeJsonResult(HttpUriRequest request, Class<T> clazz) {
        return execute(request, JsonResponseHandler.createResponseHandler(clazz));
    }

    /**
     * 日志记录（解析时用）
     *
     * @param request request
     * @return log request id
     */
    private static String loggerRequest(HttpUriRequest request) {
        String id = UUID.randomUUID()
            .toString();
        if (log.isInfoEnabled() || log.isDebugEnabled()) {
            if (request instanceof HttpEntityEnclosingRequestBase) {
                HttpEntityEnclosingRequestBase requestBase = (HttpEntityEnclosingRequestBase)request;
                HttpEntity entity = requestBase.getEntity();
                String content;
                //MULTIPART_FORM_DATA 请求类型判断
                if (!entity.getContentType()
                    .toString()
                    .contains(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                    try {
                        content = EntityUtils.toString(entity);
                        log.info("URI【{}】 {} {} ContentLength:{} Request-Content:{}", id, request.getURI()
                            .toString(), entity.getContentType(), entity.getContentLength(), content);
                        return id;
                    } catch (Exception e) {
                        log.error("http解析出现未知异常 【{}】", e.getMessage(), e);
                    }
                }
                log.info("URI【{}】 {} {} ContentLength:{} Content:{}", id, request.getURI()
                    .toString(), entity.getContentType(), entity.getContentLength(), "multipart_form_data");
            } else {
                log.info("URI【{}】request is not instanceof HttpEntityEnclosingRequestBase {}", id, request.getURI()
                    .toString());
            }
        }
        return id;
    }
}

