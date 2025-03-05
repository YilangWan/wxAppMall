package com.wxmall.common;

import lombok.Data;

/**
 * 统一返回结果
 *
 * @author wxmall
 * @date 2023-08-01
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 返回数据
     */
    private T data;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 私有构造方法
     */
    private Result() {}
    
    /**
     * 成功返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 成功返回结果
     *
     * @param data 返回数据
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setSuccess(true);
        result.setData(data);
        return result;
    }
    
    /**
     * 成功返回结果
     *
     * @param data 返回数据
     * @param message 返回消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setSuccess(true);
        result.setData(data);
        return result;
    }
    
    /**
     * 失败返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failed() {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage("操作失败");
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 失败返回结果
     *
     * @param message 返回消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failed(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 失败返回结果
     *
     * @param code 状态码
     * @param message 返回消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> failed(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 参数验证失败返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> validateFailed() {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage("参数验证失败");
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 参数验证失败返回结果
     *
     * @param message 返回消息
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> validateFailed(String message) {
        Result<T> result = new Result<>();
        result.setCode(400);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 未登录返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> unauthorized() {
        Result<T> result = new Result<>();
        result.setCode(401);
        result.setMessage("暂未登录或token已经过期");
        result.setSuccess(false);
        return result;
    }
    
    /**
     * 未授权返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> Result<T> forbidden() {
        Result<T> result = new Result<>();
        result.setCode(403);
        result.setMessage("没有相关权限");
        result.setSuccess(false);
        return result;
    }
}
