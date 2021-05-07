package com.yibo.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * tomcat 操作相关工具
 *
 * @author yibo
 * @date 2021-05-07
 */
public class TomcatUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatUtil.class);

    private static final String KEY_CATALINA_HOME = "catalina.home";

    private static final String KEY_CATALINA_BASE = "catalina.base";

    private static final List<String> PROTOCOL_LIST = Arrays.asList("org.apache.coyote.http11.Http11NioProtocol", "org.apache.coyote.http11.Http11Nio2Protocol");

    public static String getTomcatPath() {

        String tomcatPath = System.getProperty(KEY_CATALINA_BASE);
        if (StringUtils.isEmpty(tomcatPath)) {
            tomcatPath = System.getProperty(KEY_CATALINA_HOME);
        }
        return tomcatPath;
    }

    public static int getTomcatPort() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        try {
            Set<ObjectName> names = server.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
            for (ObjectName name : names) {
                String protocol = server.getAttribute(name, "protocol").toString();
                if (protocol != null && (protocol.startsWith("HTTP/")
                            || PROTOCOL_LIST.contains(protocol))) {
                    return Integer.parseInt(server.getAttribute(name, "port").toString());
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("failed to get server port", e);
        }
        LOGGER.warn("服务端口探测失败，当前MBean配置如下");
        for (ObjectName object : server.queryNames(null, null)) {
            try {
                MBeanAttributeInfo[] attrs = server.getMBeanInfo(object).getAttributes();
                for (MBeanAttributeInfo attr : attrs) {
                    try {
                        String name = attr.getName();
                        Object value = server.getAttribute(object, name);
                        LOGGER.warn("name={}, type={}, value={}", name, attr.getType(), value);
                    } catch (Throwable t) {
                        LOGGER.warn(object.getCanonicalName(), t);
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn(object.getCanonicalName(), e);
            }
        }
        return -1;
    }

    public static long getPid() {
        String processName =
                ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }
}
