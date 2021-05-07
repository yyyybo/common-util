package com.yibo.common.utils.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取机器的hostname
 *
 * @author yibo
 * @date 2021-05-07
 **/
public class HostNameUtil {

    /**
     * 计算机名
     */
    private static final String COMPUTER_NAME = "COMPUTERNAME";

    /**
     * 从linux获取hostName
     *
     * @return hostName
     */
    private static String getHostNameForLinux() {

        try {
            return (InetAddress.getLocalHost()).getHostName();
        } catch (UnknownHostException uhe) {
            // host = "hostname: hostname"
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
            return "UnknownHost";
        }
    }

    /**
     * 获取hostName,先确认是否存在ComputerName。存在直接获取
     * <p>
     * 不存在从linux取hostName
     *
     * @return hostName
     */
    public static String getHostName() {

        if (System.getenv(COMPUTER_NAME) != null) {
            return System.getenv(COMPUTER_NAME);
        } else {
            return getHostNameForLinux();
        }
    }
}
