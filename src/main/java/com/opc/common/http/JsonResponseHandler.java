/*
 * Copyright (C) 2016-2020 imassbank.com Corporation
 *
 */
package com.opc.common.http;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Response 响应结果 Json解析 Handler
 *
 * @author 莫问
 */
public class JsonResponseHandler {

    private static Logger logger = LoggerFactory.getLogger(JsonResponseHandler.class);

    public static <T> ResponseHandler<T> createResponseHandler(final Class<T> clazz) {
        return new JsonResponseHandlerImpl<>(null, clazz);
    }

    public static class JsonResponseHandlerImpl<T> extends BaseLocalResponseHandler implements ResponseHandler<T> {

        private Class<T> clazz;

        public JsonResponseHandlerImpl(String uriId, Class<T> clazz) {
            this.uriId = uriId;
            this.clazz = clazz;
        }

        @Override
        public T handleResponse(HttpResponse response) throws IOException {
            int status = response.getStatusLine()
                .getStatusCode();
            HttpEntity entity = response.getEntity();
            String str = EntityUtils.toString(entity, "utf-8");
            logger.info("URI【{}】 elapsed time:{} ms RESPONSE DATA STATUS:{} RESPONSE DATA :{}", super.uriId,
                System.currentTimeMillis() - super.startTime, status, str);

            if (StringUtils.isBlank(str)) {
                return null;
            }
            return JSON.parseObject(str, clazz);
        }

    }
}
