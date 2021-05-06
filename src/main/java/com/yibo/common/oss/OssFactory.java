/*
 * Copyright (C) 2016-2020 IMassBank Corporation
 *
 */
package com.yibo.common.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.List;

/**
 * OSS云存储: 工厂创建
 *
 * @author 莫问
 * @date 2018/10/12
 */
public class OssFactory {

    /**
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(OssFactory.class);

    /**
     * OSS地址
     */
    private final static List<String> PUB_KEY_ADDRESS =
        Lists.newArrayList("http://gosspublic.alicdn.com/", "https://gosspublic.alicdn.com/");

    /**
     * 创建OSS客户端
     *
     * @return OSS客户端
     */
    public static OSS getOSSClient(String endpoint, String accessKeyId, String accessKeySecret) {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 获取Policy签名等信息
     *
     * @param oss            OSS客户端
     * @param endpoint       端口
     * @param accessKeyId    访问秘钥ID
     * @param bucket         空间名
     * @param key            存储在bucket的目录
     * @param expiredSeconds 过期时间
     * @return 对象策略
     */
    public static PostObjectPolicy getPostObjectPolicy(OSS oss, String endpoint, String accessKeyId, String bucket,
        String key, long expiredSeconds) {

        long expireEndTime = System.currentTimeMillis() + expiredSeconds * 1000;
        Date expiration = new Date(expireEndTime);
        PolicyConditions conditions = new PolicyConditions();
        conditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
        conditions.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, key);
        String postPolicy = oss.generatePostPolicy(expiration, conditions);
        byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
        String encodedPolicy = BinaryUtil.toBase64String(binaryData);
        String postSignature = oss.calculatePostSignature(postPolicy);

        PostObjectPolicy policy = new PostObjectPolicy();
        policy.setAccessId(accessKeyId);
        policy.setHost("https://" + bucket + "." + endpoint);
        policy.setKey(key);
        policy.setExpire(String.valueOf(expireEndTime / 1000));
        policy.setPolicy(encodedPolicy);
        policy.setSignature(postSignature);
        policy.setUrl(policy.getHost() + "/" + key);
        return policy;
    }

    /**
     * 验证上传回调的Request
     *
     * @param autorizationInput 请求体
     * @param pubKeyInput       请求体
     * @param queryString       请求体
     * @param uri               请求体
     * @param ossCallbackBody   回调参数内容
     * @return 验证是否通过 true: 通过 false: 失败
     * @throws NumberFormatException 数字转换异常
     * @throws IOException           IO异常
     */
    public static boolean verifyOSSCallbackRequest(String autorizationInput, String pubKeyInput, String queryString,
        String uri, String ossCallbackBody) throws NumberFormatException, IOException {

        byte[] authorization = BinaryUtil.fromBase64String(autorizationInput);
        byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput);
        String pubKeyAddr = new String(pubKey);
        if (PUB_KEY_ADDRESS.stream()
            .noneMatch(pubKeyAddr::startsWith)) {
            LOGGER.info("pub key addr must be oss addrss ====-----> pubKeyAddr = 【{}】", pubKeyAddr);
            return Boolean.FALSE;
        }
        String retString = executeGet(pubKeyAddr);
        retString = retString.replace("-----BEGIN PUBLIC KEY-----", "");
        retString = retString.replace("-----END PUBLIC KEY-----", "");

        String authStr = URLDecoder.decode(uri, "UTF-8");
        if (StringUtils.isNotBlank(queryString)) {
            authStr += "?" + queryString;
        }
        authStr += "\n" + ossCallbackBody;

        return doCheck(authStr, authorization, retString);
    }

    /**
     * 获取Post消息体
     *
     * @param is         数据流
     * @param contentLen 消息体长度
     * @return 消息体
     */
    public static String getPostBody(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    // Should not happen.
                    if (readLengthThisTime == -1) {
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return new String(message);
            } catch (IOException e) {
                LOGGER.error("OSS回调消息体获取失败 》》InputStream", e);
            }
        }
        return "";
    }

    /**
     * 获取public key
     *
     * @param url 获取公钥的URL地址
     * @return 公钥
     */
    @SuppressWarnings({"finally"})
    private static String executeGet(String url) {
        BufferedReader in = null;

        String content = null;
        try {
            // 定义HttpClient
            HttpClient client = HttpClientBuilder.create()
                .build();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity()
                .getContent()));
            StringBuffer sb = new StringBuffer();
            String line;
            String nl = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line)
                    .append(nl);
            }
            in.close();
            content = sb.toString();
        } catch (Exception e) {
            LOGGER.error("获取public key 失败--URL【{}】", url, e);
        } finally {
            if (in != null) {
                try {
                    // 最后要关闭BufferedReader
                    in.close();
                } catch (Exception e) {
                    LOGGER.error("获取public key 最后要关闭BufferedReader 出现异常", e);
                }
            }
        }
        return content;
    }

    /**
     * 验证RSA
     *
     * @param content   检查内容
     * @param sign      签名
     * @param publicKey 公钥
     * @return 验签是否成功 true: 成功 false: 失败
     */
    private static boolean doCheck(String content, byte[] sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = BinaryUtil.fromBase64String(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes());
            return signature.verify(sign);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
