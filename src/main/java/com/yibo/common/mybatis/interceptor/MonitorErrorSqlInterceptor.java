/*
 * Copyright (c) 2018 Bianlifeng.com. All Rights Reserved.
 */
package com.yibo.common.mybatis.interceptor;

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

/**
 * 异常SQL拦截监控
 *
 * @author 莫问
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        }),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class
        }),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MonitorErrorSqlInterceptor implements Interceptor {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorErrorSqlInterceptor.class);

    /**
     * 埋点Key
     */
    private static final String SQL_EXECUTE_ERROR = "sql.execute.error";

    /**
     * 拦截器
     *
     * @param invocation 拦截实例
     * @return 执行结果
     * @throws Throwable 异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Exception e) {
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            // 埋点监控
//            SQL_EXECUTE_ERROR
//            .tag("sqlType", mappedStatement.getSqlCommandType().name())
//            .tag("methodName", mappedStatement.getId())
//            .tag("exceptionName", e.getClass().getName())
            Object parameter = invocation.getArgs()[1];
            String sql = mappedStatement.getBoundSql(parameter).getSql();
            LOGGER.error("ERROR SQL INFO:method=>{},sql=>{},parameter=>{}", mappedStatement.getId(), sql, parameter);
            throw e;
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}