package com.wxmall.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户信息更新DTO
 */
@Data
@ApiModel(description = "用户信息更新DTO")
public class UserUpdateDTO {
    
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", example = "张三")
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;
    
    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱", example = "example@wxmall.com")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 密码（可选，如果要修改密码）
     */
    @ApiModelProperty(value = "新密码（可选）", example = "newpassword123")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String newPassword;
    
    /**
     * 原密码（如果要修改密码，必须提供）
     */
    @ApiModelProperty(value = "原密码（修改密码时必填）", example = "password123")
    private String oldPassword;
}
