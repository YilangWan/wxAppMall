package com.wxmall.service;

import com.wxmall.dto.UserLoginDTO;
import com.wxmall.dto.UserRegisterDTO;
import com.wxmall.dto.UserUpdateDTO;
import com.wxmall.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 用户信息和token
     */
    String login(UserLoginDTO loginDTO);
    
    /**
     * 获取当前登录用户信息
     * @param token 用户token
     * @return 用户信息
     */
    UserVO getCurrentUser(String token);
    
    /**
     * 退出登录
     * @param token 用户token
     */
    void logout(String token);
    
    /**
     * 检查用户是否为管理员
     * @param id 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long id);
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    UserVO updateUserInfo(Long userId, UserUpdateDTO updateDTO);
}
