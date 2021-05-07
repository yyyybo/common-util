package com.yibo.common.monitor.endpoint;


import com.yibo.common.monitor.MetricsHolder;
import com.yibo.common.monitor.druid.DruidPoolMetricSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.Resource;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

/**
 * @author yibo
 * @date 2021-05-06
 */
@Configuration
public class EndPointAutoConfigure implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private ApplicationContext context;

    @Bean
    public AsyncProfilerEndPoint asyncProfilerEndPoint() {
        return new AsyncProfilerEndPoint();
    }

    @Bean
    public MetricsEndPoint metricsEndPoint() {
        return new MetricsEndPoint();
    }

    @Bean
    public ThreadDumpFlameEndPoint threadDumpFlameEndPoint() {
        return new ThreadDumpFlameEndPoint();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (StringUtils.equalsIgnoreCase(event.getApplicationContext().getId(), context.getId())) {
            // 容器启动时加载
            final MBeanServer platformBeanServer = ManagementFactory.getPlatformMBeanServer();
            MetricsHolder.getMetricRegistry().register(MetricsHolder.PROP_METRIC_REG_DRUID,
                    new DruidPoolMetricSet(platformBeanServer));
        }

    }
}

