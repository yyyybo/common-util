package com.yibo.common.template;

import com.yibo.common.exception.BizException;
import com.yibo.common.exception.UnKnowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Biz业务层模板
 *
 * @author 莫问
 * @date 2018/10/12
 */
public abstract class BaseBizTemplate<T> {

    /**
     * 监控key
     */
    protected String monitorKey;

    /**
     * 监控类型
     */
    protected String monitorType = "Service";

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(BaseBizTemplate.class);

    /**
     * 设置监控key
     *
     * @param monitorKey 监控Key
     */
    protected BaseBizTemplate(String monitorKey) {
        this.monitorKey = monitorKey;
    }

    /**
     * 如无特殊情况，请优先使用无参构造
     *
     * @param monitoryKey 监控Key
     * @param monitorType 监控的类型
     */
    protected BaseBizTemplate(String monitoryKey, String monitorType) {
        this.monitorKey = monitoryKey;
        this.monitorType = monitorType;
    }

    /**
     * 没有result的模板方法
     */
    public void executeNoResult() {
        execute();
    }

    /**
     * 有result的模板方法
     */
    public T execute() {

        T result;

        long startTime = System.currentTimeMillis();

        try {

            // 参数校验
            {
                checkParams();
            }

            // 执行业务操作
            {
                result = process();
            }

            // 监控成功结果
            {
                onSuccess(System.currentTimeMillis() - startTime);
            }
        } catch (BizException e) {
            // 监控失败结果-业务异常
            {
                onBizException(e);
            }
            throw e;
        }catch (Exception e) {
            // 监控失败结果-未知异常
            {
                onError(e);
            }
            throw new UnKnowException(e);
        } finally {
            try {

                {
                    afterProcess();
                }

            } catch (Exception e) {
                logger.error("finally中调用方法出现异常！e:" + e.getMessage(), e);
            }

        }
        return result;
    }

    /**
     * 参数检查
     */
    protected abstract void checkParams();

    /**
     * 执行待处理操作，比如模型的创建，修改，删除等
     *
     * @return 返回结果
     */
    protected abstract T process();

    /**
     * 执行成功的监控
     *
     * @param execTime 执行时长
     */
    private void onSuccess(long execTime) {
        
        // 监控执行时长 monitorKey
        // AMonitor.timer(monitorKey, execTime);
    }

    /**
     * 执行业务逻辑出现异常的监控
     */
    private void onBizException(BizException e) {
        
        // 监控

        // 日志
        logger.warn("执行业务逻辑出现异常 monitoryKey={} , msg:{}", monitorKey, e.getMessage(), e);
        throw e;
    }

    /**
     * 执行失败的监控
     */
    private void onError(Exception e) {
        // 监控

        // 日志
        logger.error("执行业务逻辑出现未知异常 monitoryKey={} , msg:{}", monitorKey, e.getMessage(), e);
        throw new UnKnowException(e);
    }

    /**
     * finally中调用方法
     */
    private void afterProcess() {
    }

}