package com.yibo.common.monitor.jedis;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.JmxAttributeGauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Jedis连接池指标
 *
 * @author yibo
 * @date 2021-05-06
 */
public class JedisPoolMetricSet implements MetricSet {

    /**
     * JMX specific attributes
     */
    private static final String O_NAME_BASE =
            "org.apache.commons.pool2:type=GenericObjectPool,name=";

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferPoolMetricSet.class);
    private static final String[] ATTRIBUTES = {"NumActive", "NumIdle", "NumWaiters", "BorrowedCount", "ReturnedCount", "CreatedCount", "DestroyedCount", "DestroyedByBorrowValidationCount", "DestroyedByEvictorCount", "MeanActiveTimeMillis", "MeanIdleTimeMillis", "MeanBorrowWaitTimeMillis", "MaxBorrowWaitTimeMillis"};
    private static final String[] NAMES = {"NumActive", "NumIdle", "NumWaiters", "BorrowedCount", "ReturnedCount", "CreatedCount", "DestroyedCount", "DestroyedByBorrowValidationCount", "DestroyedByEvictorCount", "MeanActiveTimeMillis", "MeanIdleTimeMillis", "MeanBorrowWaitTimeMillis", "MaxBorrowWaitTimeMillis"};
    private static final String[] POOLS = {"pool", "pool1", "pool2"};

    private final MBeanServer mBeanServer;

    public JedisPoolMetricSet(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<>();
        for (String pool : POOLS) {
            for (int i = 0; i < ATTRIBUTES.length; i++) {
                final String attribute = ATTRIBUTES[i];
                final String name = NAMES[i];
                try {
                    final ObjectName on = new ObjectName(O_NAME_BASE + pool);
                    gauges.put(name(pool, name), new JmxAttributeGauge(mBeanServer, on, attribute));
                } catch (JMException ignored) {
                    LOGGER.debug("Unable to load buffer pool MBeans, possibly running on Java 6");
                }
            }
        }
        return Collections.unmodifiableMap(gauges);
    }
}

