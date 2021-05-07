package com.yibo.common.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 读取appcode等需要配置的信息
 *
 * @author yibo
 * @date 2021-05-06
 */
public final class BMonitorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BMonitorConfig.class);

    private static ResourceBundle rb = null;

    static {
        try {

            rb = ResourceBundle.getBundle("bmonitor");
        } catch (Exception e) {
            LOGGER.error("can not find bmonitor.properties config file!");
        }
    }

    /**
     * 读取当前服务的appCode
     *
     * @return appCode
     */
    public static String getAppCode() {

        String value;
        if (rb != null) {
            try {
                value = rb.getString(BMonitorConstants.APP_CODE);
            } catch (MissingResourceException e) {
                value = BMonitorConstants.NULL_APP_CODE;
            }
        } else {
            value = BMonitorConstants.NULL_APP_CODE;
        }
        return value;
    }

    public static String getEnvCode() {

        String value;
        if (rb != null) {
            try {
                value = rb.getString(BMonitorConstants.ENV_CODE);
            } catch (MissingResourceException e) {
                value = BMonitorConstants.DEFAULT_ENV_CODE;
            }
        } else {
            value = BMonitorConstants.DEFAULT_ENV_CODE;
        }
        return value;
    }

    /**
     * 用来控制某个profile环境下面, 是否允许打监控数据
     * 主要用于beta测试的时候, 临时放开监控
     *
     * @return true 允许 false 不允许
     */
    public static boolean isEnabled() {

        String value;
        if (rb != null) {
            try {
                value = rb.getString(BMonitorConstants.ENABLE_MONITOR);
            } catch (MissingResourceException e) {
                return false;
            }
        } else {
            return false;
        }
        return Boolean.parseBoolean(value);

    }
}
