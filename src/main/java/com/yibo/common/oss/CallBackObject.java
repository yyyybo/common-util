package com.yibo.common.oss;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 回调参数
 *
 * @author 莫问
 * @date 2018/10/12
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallBackObject implements Serializable {

    /**
     * 回调地址
     */
    private String callbackUrl;

    /**
     * 回调内容(支持自定义参数)
     */
    private String callbackBody;

    /**
     * 回调类型
     */
    private String callbackBodyType;

    /**
     * 回调自定义参数替换值
     */
    private Map<String, String> callbackVar;
}
