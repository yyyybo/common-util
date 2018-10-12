package com.opc.common.exception;

/**
 * 自定义异常-未知异常
 *
 * @author 莫问
 * @date 2018/10/12
 */
public class UnKnowException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public UnKnowException() {
    }

    public UnKnowException(String message) {
        super(message);
    }

    public UnKnowException(String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args));
    }

    public UnKnowException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnKnowException(Throwable cause, String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args), cause);
    }

    public UnKnowException(Throwable cause) {
        super(cause);
    }

}