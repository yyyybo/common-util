package com.yibo.common.monitor.tomcat;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * tomcat 指标集合
 *
 * @author yibo
 * @date 2021-05-06
 */
public class TomcatMetricSet implements MetricSet {

    @Override
    public Map<String, Metric> getMetrics() {

        List<TomcatStatistics> tomcatStatisticsList = TomcatStatistics.buildTomcatInformationsList();
        Map<String, Metric> metricMap = Maps.newHashMapWithExpectedSize(tomcatStatisticsList.size());

        tomcatStatisticsList.forEach(tomcatStatistics -> {
            String name = tomcatStatistics.name();
            metricMap.put("max_threads." + name, (Gauge<Integer>) tomcatStatistics::maxThreads);
            metricMap.put("current_thread_count." + name, (Gauge<Integer>) tomcatStatistics::currentThreadCount);
            metricMap.put("current_threads_Busy." + name, (Gauge<Integer>) tomcatStatistics::currentThreadsBusy);
        });

        return metricMap;
    }
}
