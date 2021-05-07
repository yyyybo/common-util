package com.yibo.common.monitor;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用于监控任务丢弃策略
 *
 * @author yibo
 * @date 2021-05-06
 */
public class InstrumentedAbortPolicy implements RejectedExecutionHandler {

    /**
     * 实际的丢弃策略
     */
    private final RejectedExecutionHandler delegate;

    /**
     * 指标项
     */
    private final Counter abortCount;

    public InstrumentedAbortPolicy(RejectedExecutionHandler delegate, MetricRegistry registry, String name) {

        this.delegate = delegate;
        this.abortCount = registry.counter(MetricRegistry.name(name, "abort_count"));
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        abortCount.inc();
        delegate.rejectedExecution(r, executor);
    }
}
