package com.wxmall.common;

import lombok.Data;

import java.io.Serializable;

/**
 * API统一返回结果
 * 
 * @author System Architect
 * @param <T> 数据类型
 */
@Data
public class ApiResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功
     */
    public static <T> ApiResult<T> success() {
        return success(null);
    }
    
    /**
     * 成功
     *
     * @param data 数据
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    /**
     * 失败
     *
     * @param message 消息
     */
    public static <T> ApiResult<T> error(String message) {
        return error(500, message);
    }
    
    /**
     * 失败
     *
     * @param code    状态码
     * @param message 消息
     */
    public static <T> ApiResult<T> error(Integer code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
