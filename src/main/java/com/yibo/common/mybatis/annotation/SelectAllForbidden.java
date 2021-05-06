package com.yibo.common.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解: SELECT_ALL查询拦截
 *
 * @author 莫问
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectAllForbidden {
    //错误提示信息
    String message() default "can`t invoke this method,because SELECT_ALL IS Forbidden!";
}
