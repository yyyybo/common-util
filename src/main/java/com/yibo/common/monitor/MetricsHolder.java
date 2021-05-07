package com.yibo.common.monitor;


import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.*;
import com.yibo.common.monitor.jedis.JedisPoolMetricSet;
import com.yibo.common.monitor.jvm.JITMetricSet;
import com.yibo.common.monitor.jvm.JvmMiscMetricSet;
import com.yibo.common.monitor.tomcat.TomcatMetricSet;
import com.yibo.common.utils.net.HostNameUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 根据名字创建各个监控项的入口
 *
 * @author yibo
 * @date 2021-05-06
 */
public class MetricsHolder {

    private static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    /**
     * 内存相关指标前缀
     */
    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";

    /**
     * 垃圾回收指标前缀
     */
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";

    /**
     * 线程指标前缀
     */
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";

    /**
     * 缓冲池指标前缀
     */
    private static final String PROP_METRIC_REG_JVM_BUFFER_POOL = "jvm.bufferPool";

    /**
     * 文件描述符指标前缀
     */
    private static final String PROP_METRIC_REG_JVM_FILE_DESCRIPTOR = "jvm.fileDescriptor";

    /**
     * tomcat 指标前缀
     */
    private static final String PROP_METRIC_REG_TOMCAT = "tomcat";

    /**
     * JIT相关指标
     */
    private static final String JIT_METRIC = "jvm.jit";

    /**
     * jvm 其他指标
     */
    private static final String JVM_MISC = "jvm.misc";

    /**
     * commons pool2 指标前缀
     */
    public static final String PROP_METRIC_REG_COMMONS_POOL2 = "commons.pool2";

    /**
     * druid 指标前缀
     */
    public static final String PROP_METRIC_REG_DRUID = "druid";

    /**
     * 线上环境的标识
     */
    private static final String PROD_PROFILE = "prod";

    private static final Set<MetricAttribute> FILTER_METRIC_ATTRS = new HashSet<MetricAttribute>() {{

        // 屏蔽掉我们不会用到的指标.
        add(MetricAttribute.M15_RATE);
        add(MetricAttribute.P99);
        add(MetricAttribute.P999);
        add(MetricAttribute.P50);
        add(MetricAttribute.COUNT);
        add(MetricAttribute.STDDEV);
        add(MetricAttribute.MIN);
        add(MetricAttribute.P95);
        add(MetricAttribute.MAX);
        add(MetricAttribute.MEAN_RATE);
    }};

    private static final Graphite GRAPHITE = new Graphite(new InetSocketAddress("test.com", 2003));

    /**
     * 把单机数据上报
     */
    private static final GraphiteReporter HOSTNAME_REPORTER = GraphiteReporter.forRegistry(METRIC_REGISTRY)
            .prefixedWith(BMonitorConfig.getAppCode() + BMonitorConstants.MONITOR_NAME_SEP + BMonitorConfig.getEnvCode()
                    + BMonitorConstants.MONITOR_NAME_SEP + HostNameUtil.getHostName())
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .disabledMetricAttributes(FILTER_METRIC_ATTRS)
            .build(GRAPHITE);


    static {

        if (StringUtils.isNotBlank(BMonitorConfig.getEnvCode()) &&
                (StringUtils.startsWith(BMonitorConfig.getEnvCode(), "beta")
                        || StringUtils.startsWith(BMonitorConfig.getEnvCode(), "dev"))) {
            // 测试环境通过jmx查看监控指标
            final JmxReporter jmxReporter = JmxReporter.forRegistry(METRIC_REGISTRY)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .build();
            jmxReporter.start();
        }

        // 线上环境enableMonitor默认为true,记录监控指标
        // 测试环境enableMonitor默认为false，不记录监控指标
        if (PROD_PROFILE.equalsIgnoreCase(BMonitorConfig.getEnvCode()) || BMonitorConfig.isEnabled()) {
            HOSTNAME_REPORTER.start(60, TimeUnit.SECONDS);

            // 添加一些跟JVM相关的监控.

            // 垃圾回收的次数和时间
            METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());

            // 各区域内存情况
            METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());

            // 线程监控
            METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());

            // 缓冲池
            METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_BUFFER_POOL,
                    new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

            // 文件描述符, 主要是已打开的比例
            METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_FILE_DESCRIPTOR, new FileDescriptorRatioGauge());

            // tomcat指标
            METRIC_REGISTRY.register(PROP_METRIC_REG_TOMCAT, new TomcatMetricSet());

            // commons pool 指标
            METRIC_REGISTRY.register(MetricsHolder.PROP_METRIC_REG_COMMONS_POOL2, new JedisPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

            // jit指标
            METRIC_REGISTRY.register(JIT_METRIC, new JITMetricSet());

            // jvm misc
            METRIC_REGISTRY.register(JVM_MISC, new JvmMiscMetricSet(ManagementFactory.getPlatformMBeanServer()));
        }
    }

    public static Meter meter(String meterName) {

        return METRIC_REGISTRY.meter(meterName);
    }

    public static Timer timer(String timerName) {

        return METRIC_REGISTRY.timer(timerName);
    }

    public static MetricRegistry getMetricRegistry() {

        return METRIC_REGISTRY;
    }
}
