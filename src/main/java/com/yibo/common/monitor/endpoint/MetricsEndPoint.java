package com.yibo.common.monitor.endpoint;

import com.codahale.metrics.MetricRegistry;
import com.yibo.common.monitor.MetricsHolder;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * 监控指标
 *
 * @author yibo
 * @date 2021-05-06
 */
@Endpoint(id = "bmonitor")
public class MetricsEndPoint {

    @ReadOperation
    public MetricRegistry getAllMetrics() {
        return MetricsHolder.getMetricRegistry();
    }
}

