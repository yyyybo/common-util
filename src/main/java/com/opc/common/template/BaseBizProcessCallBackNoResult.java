package com.opc.common.template;

/**
 * 业务处理回调
 *
 * @author 莫问
 */
public abstract class BaseBizProcessCallBackNoResult extends BaseBizProcessCallBack<Void> {

    @Override
    public Void process() {
        processNoResult();
        return null;
    }

    /**
     * 执行待处理操作且无返回值
     */
    protected abstract void processNoResult();
}
