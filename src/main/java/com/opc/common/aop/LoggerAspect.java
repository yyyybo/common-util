package com.opc.common.aop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opc.common.exception.BizException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Modifier;

/**
 * 环绕通知: 日志
 *
 * @author 莫问
 * @date 2018/10/12
 */
@Aspect
@Component
public class LoggerAspect {

    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(LoggerAspect.class);

    /**
     * 配置忽略策略
     */
    private static Gson gson =
        new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE,
            Modifier.PROTECTED)
            .create();

    /**
     * 辅助方法: 环绕通知-请求日志及监控
     * <p>
     * 例: execution(public * com.opc.common.controller.*.*(..))
     *
     * @param joinPoint 切入点
     * @return 响应结果
     * @throws Throwable 异常信息
     */
    @Around("")
    public Object addLog(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = null;
        ServletRequestAttributes requestAttributes =
            (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            request = requestAttributes.getRequest();
        }

        StringBuffer log = new StringBuffer();

        log.append("===============请求内容===============\n");
        if (request != null) {
            log.append("请求地址:")
                .append(request.getRequestURL()
                    .toString())
                .append("\n");
            log.append("请求方式:")
                .append(request.getMethod())
                .append("\n");
        }
        log.append("请求类方法:")
            .append(joinPoint.getSignature())
            .append("\n");
        log.append("请求类方法参数:")
            .append(gson.toJson(joinPoint.getArgs()))
            .append("\n");
        log.append("===============请求内容===============\n");

        long time;
        Object result;

        try {
            time = System.currentTimeMillis();
            result = joinPoint.proceed();
            time = System.currentTimeMillis() - time;
            log.append("请求时长(milliseconds):")
                .append(time)
                .append("\n");
            // 请加入时长监控信息

            log.append("===============返回内容===============\n");
            String body = gson.toJson(result);
            if (body != null && body.length() > 0) {
                log.append("Response内容:")
                    .append(body)
                    .append("\n");
            }
            log.append("===============返回内容===============\n");

            LOGGER.info(log.toString());
        } catch (Exception e) {
            LOGGER.error("出错:" + joinPoint.getSignature(), e);

            // 请加入监控信息

            result = new Error(e.getMessage());

            log.append("===============返回内容===============\n");
            String body = gson.toJson(result);
            if (body != null && body.length() > 0) {
                log.append("Response内容:")
                    .append(body)
                    .append("\n");
            }
            log.append("===============返回内容===============\n");

            LOGGER.info(log.toString());

            if (e instanceof BizException) {
                throw new BizException(e.getMessage());
            } else {
                throw e;
            }
        }

        return result;
    }
}
