package com.wxmall.common.exception;

/**
 * 禁止访问异常
 * 用于处理用户权限不足的情况
 */
public class ForbiddenException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
