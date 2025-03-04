package com.wxmall.common.exception;

/**
 * 未授权异常
 * 用于处理用户未登录或登录已过期的情况
 */
public class UnauthorizedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
