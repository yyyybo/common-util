/*
 * Copyright (C) 2016-2020 imassbank.com Corporation
 *
 */
package com.opc.common.http;

/**
 * 解析时帮助
 *
 * @author 莫问
 */
public abstract class BaseLocalResponseHandler {

    String uriId;

    long startTime = System.currentTimeMillis();

    public String getUriId() {
        return uriId;
    }

    void setUriId(String uriId) {
        this.uriId = uriId;
    }
}
