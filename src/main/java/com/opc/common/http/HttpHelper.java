package com.opc.common.http;

import com.alibaba.fastjson.JSON;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;

import java.nio.charset.Charset;

/**
 * 帮助类: http客户端
 *
 * @author 莫问
 * @date 2019-06-10
 */
public class HttpHelper {

    /**
     * 传输类型: JSON
     */
    private static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * 编码: UTF-8
     */
    private static final String CHARSET_UTF = "UTF-8";

    /**
     * POST：JSON请求方式
     *
     * @param uri  请求http地址
     * @param json 请求参数JSON对象
     * @return 解析后的对象
     */
    public static <T> T doPostJson(String uri, Object json, Class<T> clazz) {

        // 设置HttpClient请求信息
        RequestBuilder requestBuilder = RequestBuilder.post()
            .setCharset(Charset.forName(CHARSET_UTF))
            .setHeader("Content-type", CONTENT_TYPE_JSON)
            .setHeader("accept", CONTENT_TYPE_JSON)
            .setEntity(new StringEntity(JSON.toJSONString(json), CHARSET_UTF))
            .setUri(uri);

        // 发送请求(转换响应结果)
        return LocalHttpClient.executeJsonResult(requestBuilder.build(), clazz);
    }
}
