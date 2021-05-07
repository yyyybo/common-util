package com.yibo.common.monitor.tomcat;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * tomcat 相关数据
 *
 * @author yehui
 * @date 2018/8/30
 */
public class TomcatStatistics {

    private static final Logger logger = LoggerFactory.getLogger(TomcatStatistics.class);

    /**
     * 是否使用tomcat
     */
    private static final boolean IS_TOMCAT_ENVIRONMENT = System.getProperty("catalina.home") != null;

    /**
     * 线程池数据对象
     */
    private static final List<ObjectName> THREAD_POOLS = Lists.newArrayList();

    /**
     * tomcat 数据 bean
     */
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

    /**
     * 线程池数据对象
     */
    private final ObjectName threadPool;

    static Set<ObjectName> getTomcatThreadPools() throws MalformedObjectNameException {

        return SERVER.queryNames(new ObjectName("Catalina:type=ThreadPool,*"), null);
    }

    private Object getAttribute(ObjectName name, String attribute) {

        try {
            return SERVER.getAttribute(name, attribute);
        } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
            if (logger.isDebugEnabled()) {
                logger.error("获取tomcat数据失败. name={}, attribute={}", name, attribute, e);
            }
            return null;
        }
    }

    static MBeanInfo getBeanInfo(ObjectName name) throws JMException {

        return SERVER.getMBeanInfo(name);
    }

    private TomcatStatistics(ObjectName threadPool) {

        this.threadPool = threadPool;
    }

    /**
     * 获取 tomcat 的数据, 每次重新获取一下
     *
     * @return tomcat 的数据
     */
    public static List<TomcatStatistics> buildTomcatInformationsList() {

        if (!IS_TOMCAT_ENVIRONMENT) {
            return Collections.emptyList();
        } else {
            try {
                synchronized (THREAD_POOLS) {
                    if (THREAD_POOLS.isEmpty()) {
                        initBeans();
                    }
                }

                List<TomcatStatistics> tomcatStatisticsList = Lists.newArrayListWithExpectedSize(THREAD_POOLS.size());

                for (ObjectName threadPool : THREAD_POOLS) {
                    tomcatStatisticsList.add(new TomcatStatistics(threadPool));
                }

                return tomcatStatisticsList;
            } catch (JMException e) {
                logger.error("获取tomcat实时数据失败", e);
                return Collections.emptyList();
            }
        }
    }

    /**
     * 初始化数据承载的实体
     *
     */
    static void initBeans() throws MalformedObjectNameException {

        THREAD_POOLS.clear();
        THREAD_POOLS.addAll(getTomcatThreadPools());
    }

    /**
     * 线程池名称
     */
    public String name() {

        final String name = threadPool.getKeyProperty("name");
        final String subType = threadPool.getKeyProperty("subType");
        if (StringUtils.isEmpty(subType)) {
            return name;
        }

        return name + "-" + subType;
    }

    /**
     * 最大线程数
     */
    public int maxThreads() {

        Integer maxThreads = (Integer)getAttribute(threadPool, "maxThreads");
        return maxThreads == null ? 0 : maxThreads;
    }

    /**
     * 当前线程数
     */
    public int currentThreadCount() {

        Integer currentThreadCount = (Integer)getAttribute(threadPool, "currentThreadCount");
        return currentThreadCount == null ? 0 : currentThreadCount;
    }

    /**
     * 活跃的线程数
     */
    public int currentThreadsBusy() {

        Integer currentThreadsBusy = (Integer)getAttribute(threadPool, "currentThreadsBusy");
        return currentThreadsBusy == null ? 0 : currentThreadsBusy;
    }
}

