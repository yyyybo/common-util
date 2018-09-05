/*
 * Copyright (c) 2014 Qunar.com. All Rights Reserved.
 */
package com.opc.common.utils.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Response处理相关的工具类
 *
 * @author yibo.yu
 */
public class ResponseUtil {

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(ResponseUtil.class);

    /**
     * 格式化
     */
    private static final SimpleDateFormat M_FORMAT_GMT =
        new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", Locale.US);

    /**
     * 常量: 线程池
     */
    private static final ThreadLocal<SimpleDateFormat> UNIQUE_NUM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return M_FORMAT_GMT;
        }
    };

    /**
     * 无缓存机制的响应处理
     *
     * @param response 响应体
     * @param message  响应消息
     */
    public static void sendMessageNoCache(HttpServletResponse response, String message) {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        try {
            response.getWriter()
                .print(message);
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * 有缓存机制的响应处理
     *
     * @param response 响应体
     * @param message  响应消息
     */
    public static void sendMessageWithCache(HttpServletResponse response, String message) {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Expires", UNIQUE_NUM.get()
            .format(new Date(System.currentTimeMillis() + 600000)));
        response.setHeader("Cache-Control", "public");
        response.setHeader("Cache-Control", "max-age=600");
        try {
            response.getWriter()
                .print(message);
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * 无缓存机制的响应处理-JSON格式
     *
     * @param response 响应体
     * @param message  响应消息
     */
    public static void sendJsonNoCache(HttpServletResponse response, String message) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        try {
            response.getWriter()
                .print(message);
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * 有缓存机制的响应处理-JSON格式
     *
     * @param response 响应体
     * @param message  响应消息
     */
    public static void sendJsonWithCache(HttpServletResponse response, String message) {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Expires", UNIQUE_NUM.get()
            .format(new Date(System.currentTimeMillis() + 600000)));
        response.setHeader("Cache-Control", "public");
        response.setHeader("Cache-Control", "max-age=600");
        try {
            response.getWriter()
                .print(message);
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * 无缓存机制的响应处理-XML格式
     *
     * @param response 响应体
     * @param message  响应消息
     */
    public static void sendXmlMessageNoCache(HttpServletResponse response, String message) {
        response.setContentType("application/xml;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-cache");
        getWriter(response, message);
    }

    /**
     * 有缓存机制的响应处理-XML格式
     *
     * @param response 响应体
     * @param message  响应消息
     */
    public static void sendXmlMessageWithCache(HttpServletResponse response, String message) {
        response.setContentType("application/xml;charset=UTF-8");
        response.setHeader("Expires", UNIQUE_NUM.get()
            .format(new Date(System.currentTimeMillis() + 300000)));
        response.setHeader("Cache-Control", "public");
        response.setHeader("Cache-Control", "max-age=300");
        getWriter(response, message);
    }

    /**
     * XML内容拼接
     *
     * @param response 响应体
     * @param message  响应消息
     */
    private static void getWriter(HttpServletResponse response, String message) {
        try {
            StringBuilder sb = new StringBuilder();
            PrintWriter writer = response.getWriter();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sb.append(message);
            writer.print(sb.toString());
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
            ioe.printStackTrace();
        }
    }

}