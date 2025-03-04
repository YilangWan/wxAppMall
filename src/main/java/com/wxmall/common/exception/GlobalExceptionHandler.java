package com.wxmall.common.exception;

import com.wxmall.common.api.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public CommonResult<String> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return CommonResult.failed(e.getMessage());
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public CommonResult<String> handleUnauthorizedException(UnauthorizedException e) {
        log.error("认证异常: {}", e.getMessage(), e);
        return CommonResult.unauthorized(e.getMessage());
    }
    
    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public CommonResult<String> handleForbiddenException(ForbiddenException e) {
        log.error("权限不足: {}", e.getMessage(), e);
        return CommonResult.forbidden(e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, BindException.class})
    public CommonResult<String> handleValidException(Exception e) {
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException) {
            bindingResult = ((MethodArgumentNotValidException) e).getBindingResult();
        } else if (e instanceof BindException) {
            bindingResult = ((BindException) e).getBindingResult();
        }
        
        String message = null;
        if (bindingResult != null && bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        log.error("参数验证异常: {}", message, e);
        return CommonResult.validateFailed(message);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<String> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return CommonResult.failed("系统异常，请联系管理员");
    }
}
