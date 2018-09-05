/*
 * Copyright (c) 2014 Qunar.com. All Rights Reserved.
 */
package com.opc.common.utils.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Request处理相关的工具类
 *
 * @author yibo.yu
 */
public final class RequestUtil {

    /**
     * 从ServletRequest中获取URI中应用上下文部分以后的pathInfo
     *
     * @param request 请求体
     * @return 不包含应用上下文的路径。参数request为null时返回null，应用上下文后无内容时返回空字符串。
     */
    public static String getPathInfo(ServletRequest request) {
        if (request == null) {
            return null;
        }
        // 取得pathInfo
        return ((HttpServletRequest)request).getRequestURI()
            .replaceFirst(((HttpServletRequest)request).getContextPath(), "");
    }

    /**
     * 获取Servlet上下文
     *
     * @param request 请求体
     * @return ServletContext。参数request为null时返回null。
     */
    public static ServletContext getServletContext(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 取得ServletContext
        return request.getSession(true)
            .getServletContext();
    }

    /**
     * Dump请求参数及属性。
     *
     * @param req 请求体
     * @return Dump出的字符串
     */
    public static String dumpRequest(HttpServletRequest req) {
        return dumpRequestParameters(req) + " , " + dumpRequestAttributes(req);
    }

    /**
     * Dump请求属性
     *
     * @param req 请求体
     * @return Dump出的字符串。参数request为null时返回null。
     */
    public static String dumpRequestAttributes(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(" RequestAttributes {");
        Enumeration<String> enumeration = req.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            Object value = req.getAttribute(key);
            builder.append(key);
            builder.append(" = ");
            builder.append(value);
            if (enumeration.hasMoreElements()) {
                builder.append(" , ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * Dump请求参数
     *
     * @param req 请求体
     * @return Dump出的字符串。参数request为null时返回null。
     */
    public static String dumpRequestParameters(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(" RequestParameters {");
        Enumeration<String> enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            String[] values = req.getParameterValues(key);
            for (int i = 0; i < values.length; i++) {
                builder.append(key);
                builder.append("[");
                builder.append(i);
                builder.append("] = ");
                builder.append(values[i]);
                if (i < values.length - 1) {
                    builder.append(" , ");
                }
            }
            if (enumeration.hasMoreElements()) {
                builder.append(" , ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * 消除url中指定key的参数，并返回结果
     * <p>
     * 例:  消除前：test.do?no=2&r=8331352040140757427&no=1 消除后：test.do?no=2&no=1
     *
     * @param url URL
     * @param key 需消除的参数key
     * @return 消除后的URL
     */
    public static String deleteUrlParam(String url, String key) {

        // url,key为NULL时，直接返回URL
        if (url == null || "".equals(url) || key == null || "".equals(key)) {
            return url;
        }

        // 取得参数开始位置'?'的index
        int start = url.indexOf("?");

        // 用于返回数据的StringBuilder
        StringBuilder returnUrl = new StringBuilder(url);

        // 存在参数时
        if (start >= 0) {

            // 取得参数列表
            String tmp = url.substring(start + 1);

            // 保存非参数部分
            returnUrl = new StringBuilder(url.substring(0, start));

            // 转换为参数数组
            String[] params = tmp.split("&");

            // 遍历参数数组，并保存指定key以外参数
            for (String param : params) {
                // 是否为指定key
                if (!param.startsWith(key + "=")) {

                    // 首个参数时追加字符'?'
                    // 非首个参数时追加字符'&'
                    if (returnUrl.indexOf("?") < 0 && !returnUrl.toString()
                        .endsWith("?")) {
                        returnUrl.append("?");
                    } else if (!returnUrl.toString()
                        .endsWith("&")) {
                        returnUrl.append("&");
                    }

                    // 保存参数
                    returnUrl.append(param);
                }
            }
        }

        // 返回过滤后的URL
        return returnUrl.toString();
    }

    /**
     * 获得真实IP地址
     *
     * @param request 请求体
     * @return 真实IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}