/*
 * Copyright (c) 2014 Qunar.com. All Rights Reserved.
 */
package com.opc.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 异常处理相关的工具类。
 * <p>
 * log.error("error-message", ExceptionUtil.getStackTrace(e));
 *
 * @author 莫问
 */
public final class ExceptionUtil {

    /**
     * 针对ServletException堆栈追踪信息获取方式的不同，做单独处理。
     */
    private static final String SERVLET_EXCEPTION_NAME = "javax.servlet.ServletException";

    /**
     * 发生ServletException异常时使用的方法名-用来获取发生Servlet异常的原因。
     */
    private static final String GET_ROOT_CAUSE = "getRootCause";

    /**
     * 日志类。
     */
    private static Logger log = LoggerFactory.getLogger(ExceptionUtil.class);

    /**
     * 获取指定异常的堆栈追踪信息。
     * <p/>
     * <p>
     * 如果能够获取该异常的产生原因，递归获取异常的堆栈信息。
     * 针对ServletException时，需使用getRootCause()方法。
     * </p>
     *
     * @param throwable 异常
     * @return 异常堆栈追踪信息
     */
    public static String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (throwable != null) {
            byteArrayOutputStream.reset();
            throwable.printStackTrace(new PrintStream(byteArrayOutputStream));
            sb.append(byteArrayOutputStream.toString());

            // 获取throwable的Class对象(添加合适的泛型来修改throwableClass的声明)
            Class<?> throwableClass = throwable.getClass();

            // 异常为ServletException时使用getRootCause方法
            if (SERVLET_EXCEPTION_NAME.equals(throwableClass.getName())) {
                try {
                    // 执行Class的指定方法
                    Method method = throwableClass.getMethod(GET_ROOT_CAUSE);
                    throwable = (Throwable)method.invoke(throwable);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    // 未找到对应的方法
                    log.error(e.getMessage());
                    throwable = null;
                }
            } else {
                throwable = throwable.getCause();
            }
        }
        return sb.toString();
    }
}