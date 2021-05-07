package com.yibo.common.monitor;

/**
 * 监控系统客户端要用到的一些常量
 *
 * @author yibo
 * @date 2021-05-06
 */
public final class BMonitorConstants {

    /**
     * 监控名字分隔符
     */
    public static final String MONITOR_NAME_SEP = ".";

    /**
     * 要监控的服务的代码
     */
    public static final String APP_CODE = "appCode";

    /**
     * 用于检查哪些服务没有配置好appCode
     */
    public static final String NULL_APP_CODE = "invalidApp";

    /**
     * 用于检查各个机器的所处环境的key
     */
    public static final String ENV_CODE = "envCode";

    /**
     * 如果没有配置, 那么就是默认的beta的环境
     */
    public static final String DEFAULT_ENV_CODE = "beta";

    /**
     * 在配置文件里面,标记是否允许记录监控
     * 主要用途: 目前的实践是, 线上打监控, beta不记录, 我们需要在beta测试监控的时候, 会在beta打开一下.
     */
    public static final String ENABLE_MONITOR = "enableMonitor";

}
