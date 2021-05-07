package com.yibo.common.monitor.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author yibo
 * @date 2021-05-06
 */
public class JITMetricSet implements MetricSet {

    private static final String JIT_COMPILATION_TIME_METRIC = "JVM_JIT_Compilation_Time";

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>(16);
        final CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
        gauges.put(JIT_COMPILATION_TIME_METRIC,  (Gauge<Long>) compilationBean::getTotalCompilationTime);
        return Collections.unmodifiableMap(gauges);
    }
}

