package com.opc.common.template;

import com.opc.common.exception.CusException;
import com.opc.common.exception.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Biz层模板
 *
 * @author yibo.yu
 */
public class BizTemplate {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(BizTemplate.class);

    /**
     * 无参构造
     */
    private BizTemplate() {
    }

    /**
     * 没有result的模板方法
     *
     * @param action 操作回调接口
     */
    public static void executeNoResult(BaseBizProcessCallBackNoResult action) {
        execute(action);
    }

    /**
     * 有result的模板方法
     *
     * @param action 操作回调接口
     */
    public static <T> T execute(BaseBizProcessCallBack<T> action) {

        T result;

        long startTime = System.currentTimeMillis();

        try {

            // 参数校验
            {
                action.checkParams();
            }

            // 执行业务操作
            {
                result = action.process();
            }

            // 监控成功结果
            {
                action.succMonitor(System.currentTimeMillis() - startTime);
            }
        } catch (CusException e) {
            // 监控失败结果
            {
                action.failMonitor();
            }

            // 增加监控

            logger.error("系统异常! {}", e.getMessage(), ExceptionUtil.getStackTrace(e));
            throw e;
        } catch (Exception e) {
            // 监控失败结果
            {
                action.failMonitor();
            }

            logger.error("系统未知异常! {}", e.getMessage(), ExceptionUtil.getStackTrace(e));
            throw new CusException(e);
        } finally {
            try {

                {
                    action.afterProcess();
                }

            } catch (Exception e) {
                logger.error("finally中调用方法出现异常！e:" + e.getMessage(), ExceptionUtil.getStackTrace(e));
            }

        }
        return result;
    }

}