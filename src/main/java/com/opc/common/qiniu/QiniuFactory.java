/*
 * Copyright (C) 2016-2020 IMassBank Corporation
 *
 */
package com.opc.common.qiniu;

import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

/**
 * 七牛云存储: 工厂创建
 *
 * @author yuyibo
 */
public class QiniuFactory {

    /**
     * 创建上传空间对象
     *
     * @return 空间对象
     */
    public static UploadManager createUploadManager() {
        return new UploadManager(getConfing());
    }

    /**
     * 配置空间实例
     *
     * @return 配置空间实例
     */
    public static Configuration getConfing() {
        return new Configuration(Zone.zone1());
    }

    /**
     * 使用默认策略，只需要设置上传的空间名就可以了
     *
     * @return 加密token
     */
    public static String getUpToken(String accessKey, String secretKey, String bucketName) {
        return getAuth(accessKey, secretKey).uploadToken(bucketName);
    }

    /**
     * 创建权限
     *
     * @return 权限对象
     */
    public static Auth getAuth(String accessKey, String secretKey) {
        return Auth.create(accessKey, secretKey);
    }

    /**
     * 创建文件列表对象
     */
    public static BucketManager getBucketManager(String accessKey, String secretKey) {
        return new BucketManager(getAuth(accessKey, secretKey), getConfing());
    }

}
