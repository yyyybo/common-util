package com.yibo.common.exception;

/**
 * 自定义异常-业务异常
 *
 * @author 莫问
 */
public class BizException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public BizException() {
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args));
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause, String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args), cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

}