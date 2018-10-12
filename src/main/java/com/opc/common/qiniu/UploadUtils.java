/*
 * Copyright (C) 2016-2020 IMassBank Corporation
 *
 */
package com.opc.common.qiniu;

import com.opc.common.exception.BizException;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片上传工具类
 *
 * @author 莫问
 */
public class UploadUtils {

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(UploadUtils.class);

    /**
     * 七牛空间名
     */
    public static final String BUCKET_NAME = "xxx";

    /**
     * 上传文件
     *
     * @param filePath 上传文件的路径
     * @param key      上传到七牛后保存的文件名
     */
    public static void uploadByPath(String accessKey, String secretKey, String filePath, String key) {

        try {
            //调用put方法上传
            Response res = QiniuFactory.createUploadManager()
                .put(filePath, key, QiniuFactory.getUpToken(accessKey, secretKey, BUCKET_NAME));
            //打印返回的信息
            logger.error("七牛上传成功", res.bodyString());
        } catch (QiniuException e) {
            Response r = e.response;
            // 请求失败时打印的异常的信息
            logger.error("七牛上传失败", r.toString());
        }
    }

    /**
     * 文件流的形式上传图片
     *
     * @param stream  图片流
     * @param imgName 图片名称
     */
    public static String uploadByStream(String accessKey, String secretKey, String buckName, InputStream stream,
        String imgName) {
        UploadManager uploadManager = QiniuFactory.createUploadManager();

        Response response;
        if (null == stream) {
            logger.error("no input data stream");
            throw new BizException("文件流为空");
        }
        try {
            byte[] byteData = IOUtils.toByteArray(stream);
            response = uploadManager.put(byteData, imgName, QiniuFactory.getUpToken(accessKey, secretKey, buckName));
            return response.bodyString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BizException("上传文件失败 {}", e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 文件字节的形式上传图片
     *
     * @param byteData 字节数组
     * @param imgName  图片名称
     */
    public static String uploadByByte(String accessKey, String secretKey, String buckName, byte[] byteData,
        String imgName) {
        UploadManager uploadManager = QiniuFactory.createUploadManager();

        Response response = null;
        if (null == byteData) {
            logger.error("no input byte[] byteData");
            throw new BizException("no input byte[] byteData");
        }
        try {
            response = uploadManager.put(byteData, imgName, QiniuFactory.getUpToken(accessKey, secretKey, buckName));
            return response.bodyString();
        } catch (Exception e) {
            throw new BizException("上传文件失败 {}", e);
        }
    }

    /**
     * 抓取网络图片上传七牛
     *
     * @param accessKey  你的access_key
     * @param secretKey  你的secret_key
     * @param url        网络上一个资源的url地址
     * @param bucketName 你的空间名
     * @param fileName   空间内文件的key(你要上传的文件名，唯一的)
     */
    public static DefaultPutRet uploadImage(String accessKey, String secretKey, String url, String bucketName,
        String fileName) {

        BucketManager bucketManager = QiniuFactory.getBucketManager(accessKey, secretKey);
        DefaultPutRet fetch;
        try {
            fetch = bucketManager.fetch(url, bucketName, fileName);
        } catch (QiniuException e) {
            logger.warn("七牛上传失败，请检网络地址是否正确【{}】----错误信息{}", url, e);
            throw new BizException("七牛上传失败，请检网络地址是否正确【{}】----错误信息{}", url, e);
        }
        return fetch;
    }

}
