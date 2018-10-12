package com.opc.common.oss;

import lombok.Data;

import java.util.Map;

/**
 * 对象策略
 *
 * @author 莫问
 * @date 2018/10/12
 */
@Data
public class PostObjectPolicy {

    /**
     * 访问秘钥ID
     */
    private String accessId;

    /**
     * 访问域名
     */
    private String host;

    /**
     * 存储目录(上传文件的object名称。如果名称包含路径，如a/b/c/b.jpg， 则OSS会自动创建相应的文件夹)
     * 文件的唯一标识
     */
    private String key;

    /**
     * 策略
     */
    private String policy;

    /**
     * 有效时间
     */
    private String expire;

    /**
     * 签名
     */
    private String signature;

    /**
     * 文件的URL地址
     */
    private String url;

    /**
     * 回调参数
     */
    private String callBack;

    /**
     * 回调参数替换值
     */
    private Map<String, String> callbackVar;
}
