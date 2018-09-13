/*
 * Copyright (C) 2016-2020 IMassBank Corporation
 *
 */
package com.opc.common.utils.ref;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Spring反射获取Bean工具类
 *
 * @author 莫问
 */
@Component
public class SpringContextsUtil implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 根据类名获取Bean
     *
     * @param beanName 类名(Bean名字)
     * @return Bean实体
     */
    public Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 根据类名获取Bean
     *
     * @param beanName 类名(Bean名字)
     * @param clazs    泛型
     * @return Bean实体
     */
    public <T> T getBean(String beanName, Class<T> clazs) {
        return clazs.cast(getBean(beanName));
    }

    /**
     * 获取Spring启动的Context
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取method
     *
     * @param className  类名 要符合spring的默认规则
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @return 方法实体
     */
    public Method findMethod(String className, String methodName, Class<?>... paramTypes) {
        return ReflectionUtils.findMethod(getBean(className).getClass(), methodName, paramTypes);
    }

    /**
     * 执行该方法
     *
     * @param method 方法实体
     * @param target 对应类的Bean
     * @param args   参数
     * @return 返回结果
     */
    public Object invokeMethod(Method method, String target, Object... args) {
        return ReflectionUtils.invokeMethod(method, getBean(target), args);
    }
}
