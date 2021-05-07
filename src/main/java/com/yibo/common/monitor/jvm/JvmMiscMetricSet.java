package com.yibo.common.monitor.jvm;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.google.common.collect.Maps;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 *
 * @author yibo
 * @date 2021-05-06
 */
public class JvmMiscMetricSet implements MetricSet {

    private static final String METASPACE_METRIC = "metaspace";
    private static final String[] METASPACE_METRIC_ATTRIBUTES = {"Usage"};
    private static final String[] METASPACE_METRIC_ATTRIBUTES_FIELD = {"committed", "init", "max", "used"};

    private static final String CODE_CACHE_METRIC = "codecache";
    private static final String[] CODE_CACHE_METRIC_ATTRIBUTES = {"Usage"};
    private static final String[] CODE_CACHE_METRIC_ATTRIBUTES_FIELD = {"committed", "init", "max", "used"};


    private final MBeanServer mBeanServer;

    public JvmMiscMetricSet(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, Metric> metricMap = Maps.newHashMap();
        setMetricGaugeWithCompositeType(metricMap, METASPACE_METRIC, "java.lang:type=MemoryPool,name=Metaspace", METASPACE_METRIC_ATTRIBUTES, METASPACE_METRIC_ATTRIBUTES_FIELD);
        setMetricGaugeWithCompositeType(metricMap, CODE_CACHE_METRIC, "java.lang:type=MemoryPool,name=Code Cache", CODE_CACHE_METRIC_ATTRIBUTES, CODE_CACHE_METRIC_ATTRIBUTES_FIELD);
        return metricMap;
    }

    private void setMetricGaugeWithCompositeType(Map<String, Metric> gauges, String prefix, String objectName,
                                                 String[] attributes, String[] fields) {
        try {
            final ObjectName on = new ObjectName(objectName);
            mBeanServer.getMBeanInfo(on);
            for (String attribute : attributes) {
                for (String field : fields) {
                    gauges.put(name(prefix, attribute, field), ((Gauge<Object>) () -> {
                        try {
                            final Object serverAttribute = mBeanServer.getAttribute(on, attribute);
                            if (!(serverAttribute instanceof CompositeData)) {
                                return null;
                            }
                            CompositeData compositeData = (CompositeData) serverAttribute;
                            return compositeData.get(field);
                        } catch (JMException e) {
                            return null;
                        }
                    }));
                }
            }
        } catch (Throwable ignore) {
        }
    }

}