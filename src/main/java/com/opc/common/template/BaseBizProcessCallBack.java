package com.opc.common.template;

/**
 * 业务处理回调
 *
 * @author yibo.yu
 */
public abstract class BaseBizProcessCallBack<T> {

    /**
     * 参数检查
     */
    public void checkParams() {
    }

    /**
     * 执行待处理操作，比如模型的创建，修改，删除等
     *
     * @return 返回结果
     */
    public abstract T process();

    /**
     * 执行成功的监控
     *
     * @param execTime 执行时长
     */
    public void succMonitor(long execTime) {
    }

    /**
     * 执行失败的监控
     */
    public void failMonitor() {
    }

    /**
     * finally中调用方法
     */
    public void afterProcess() {
    }

}
