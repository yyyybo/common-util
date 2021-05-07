package com.yibo.common.monitor.druid;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.JmxAttributeGauge;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * 德鲁伊相关指标
 *
 * @author yibo
 * @date 2021-05-06
 */
public class DruidPoolMetricSet implements MetricSet {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferPoolMetricSet.class);

    /**
     * JMX specific attributes
     */
    private static final String NAME_BASE =
            "com.alibaba.druid:type=DruidDataSource,id=*";

    private static final String[] ATTRIBUTES = {"PoolingCount", "RecycleCount", "ConnectErrorCount", "DestroyCount", "ConnectCount", "CloseCount", "RemoveAbandonedCount", "DiscardCount", "ResetCount", "WaitThreadCount", "NotEmptyWaitCount", "ActivePeak", "ActiveCount", "CachedPreparedStatementHitCount", "CachedPreparedStatementMissCount", "StartTransactionCount", "CommitCount", "RollbackCount", "CreateErrorCount", "ConnectionErrorRetryAttempts"};
    private static final String[] NAMES = {"PoolingCount", "RecycleCount", "ConnectErrorCount", "DestroyCount", "ConnectCount", "CloseCount", "RemoveAbandonedCount", "DiscardCount", "ResetCount", "WaitThreadCount", "NotEmptyWaitCount", "ActivePeak", "ActiveCount", "CachedPreparedStatementHitCount", "CachedPreparedStatementMissCount", "StartTransactionCount", "CommitCount", "RollbackCount", "CreateErrorCount", "ConnectionErrorRetryAttempts"};

    private final MBeanServer mBeanServer;

    public DruidPoolMetricSet(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @SneakyThrows
    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();
        final Set<ObjectName> objectNames = mBeanServer.queryNames(new ObjectName(NAME_BASE), null);
        for (ObjectName objectName : objectNames) {
            for (int i = 0; i < ATTRIBUTES.length; i++) {
                final String attribute = ATTRIBUTES[i];
                final String name = NAMES[i];
                final String canonicalName = objectName.getCanonicalName()
                        .replace(":", "_")
                        .replace("=", "_")
                        .replace("-", "_")
                        .replace(",", "_");
                gauges.put(name(canonicalName, name), new JmxAttributeGauge(mBeanServer, objectName, attribute));
            }
        }
        return Collections.unmodifiableMap(gauges);
    }
}
