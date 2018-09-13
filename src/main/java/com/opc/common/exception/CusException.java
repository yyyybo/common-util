package com.opc.common.exception;

/**
 * 自定义异常
 *
 * @author 莫问
 */
public class CusException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public CusException() {
    }

    public CusException(String message) {
        super(message);
    }

    public CusException(String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args));
    }

    public CusException(String message, Throwable cause) {
        super(message, cause);
    }

    public CusException(Throwable cause, String msgTemplate, Object... args) {
        super(String.format(msgTemplate, args), cause);
    }

    public CusException(Throwable cause) {
        super(cause);
    }

}