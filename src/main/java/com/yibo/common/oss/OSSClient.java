package com.yibo.common.oss;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * AliYunOSSClient
 *
 * @author 莫问
 * @date 2018/10/12
 */
@Component
public class OSSClient {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.callbackUrl}")
    private String callbackUrl;

    /**
     * OSS操作
     */
    private OSS oss;

    /**
     * 空参构造器
     */
    public OSSClient() {
    }

    /**
     * 前置初始化方法
     */
    @PostConstruct
    void init() {
        if (this.oss == null) {
            this.oss = OssFactory.getOSSClient(endpoint, accessKeyId, accessKeySecret);
        }
    }

    /**
     * 获取Policy签名等信息
     *
     * @param key            存储在bucket的目录
     * @param expiredSeconds 过期时间
     * @param createUserId   创建人
     * @param updateUserId   更新人
     * @param thirdPartId    第三方唯一标识
     * @return 对象策略
     */
    public PostObjectPolicy getPostObjectPolicy(String key, long expiredSeconds, String createUserId,
        String updateUserId, String thirdPartId) {

        CallBackObject callback = new CallBackObject();
        callback.setCallbackUrl(callbackUrl);
        callback.setCallbackBodyType("application/json");

        // 设置回调内容
        String callbackBody =
            "{\"bucket\":${bucket},\"object\":${object},\"etag\":${etag},\"size\":${size},\"mimeType\":${mimeType},\"mimeHeight\":${imageInfo.height},\"mimeWidth\":${imageInfo.width},\"mimeFormat\":${imageInfo.format},\"createUserId\":${x:create_user_id},\"updateUserId\":${x:update_user_id},\"thirdPartId\":${x:third_part_id},\"path\":${x:path}}";

        callback.setCallbackBody(callbackBody);

        byte[] bytes = Base64.encodeBase64(JSON.toJSONBytes(callback));

        PostObjectPolicy policy =
            OssFactory.getPostObjectPolicy(oss, endpoint, accessKeyId, bucket, key, expiredSeconds);

        // 设置自定义参数替换值
        Map<String, String> callbackVar = Maps.newHashMap();
        callbackVar.put("x:create_user_id", createUserId);
        callbackVar.put("x:update_user_id", updateUserId);
        callbackVar.put("x:third_part_id", thirdPartId);
        callbackVar.put("x:path", policy.getUrl());
        callback.setCallbackVar(callbackVar);
        policy.setCallbackVar(callbackVar);

        // 回调内容
        policy.setCallBack(new String(bytes));

        return policy;
    }
}
