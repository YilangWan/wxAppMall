package com.wxmall.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 手机号（脱敏）
     */
    private String phone;
    
    /**
     * 邮箱（脱敏）
     */
    private String email;
    
    /**
     * 用户角色：0-普通用户，1-管理员
     */
    private Integer role;
    
    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 是否为管理员
     */
    public boolean isAdmin() {
        return role != null && role == 1;
    }
}
