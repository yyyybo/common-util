/*
 * Copyright (c) 2018 Bianlifeng.com. All Rights Reserved.
 */
package com.opc.common.mybatis.interceptor;

import com.google.common.base.Stopwatch;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 慢SQL拦截-监控报警
 *
 * @author 莫问
 */
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
    RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                    RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class MonitorSlowSqlInterceptor implements Interceptor {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorSlowSqlInterceptor.class);

    /**
     * 埋点Key
     */
    private static final String SQL_EXECUTE_MILLIS = "sql.execute.millis";

    /**
     * 慢SQL时间临界点(单位: 毫秒)
     */
    private int slowSqlMillis = 1000;

    /**
     * 拦截器
     *
     * @param invocation 拦截实例
     * @return 执行结果
     * @throws Throwable 异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Object result = invocation.proceed();
        MappedStatement mappedStatement = (MappedStatement)invocation.getArgs()[0];
        long costMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        // 增加监控-执行时间

        if (costMillis > slowSqlMillis) {
            Object parameter = invocation.getArgs()[1];
            String sql = mappedStatement.getBoundSql(parameter)
                .getSql();
            LOGGER.error("SLOW SQL INFO:method=>{},sql=>{},parameter=>{},time=>{}ms", mappedStatement.getId(), sql,
                parameter, costMillis);
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.slowSqlMillis = Integer.parseInt(properties.getProperty("slowSqlMillis", String.valueOf(slowSqlMillis)));
    }

    /**
     * 设置慢SQL时间
     *
     * @param slowSqlMillis 毫秒
     */
    public void setSlowSqlMillis(int slowSqlMillis) {
        this.slowSqlMillis = slowSqlMillis;
    }
}