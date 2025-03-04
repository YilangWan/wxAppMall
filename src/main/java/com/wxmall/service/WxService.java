package com.wxmall.service;

import com.wxmall.dto.WxLoginDTO;
import com.wxmall.vo.UserVO;

import java.util.Map;

/**
 * 微信服务接口
 * 处理微信登录和用户信息解密等功能
 */
public interface WxService {
    /**
     * 微信登录
     * 
     * @param wxLoginDTO 微信登录参数
     * @return 登录结果，包含token和用户信息
     */
    Map<String, Object> wxLogin(WxLoginDTO wxLoginDTO);
    
    /**
     * 解密微信用户信息
     * 
     * @param encryptedData 加密数据
     * @param sessionKey 会话密钥
     * @param iv 加密算法的初始向量
     * @return 解密后的用户信息JSON字符串
     */
    String decryptUserInfo(String encryptedData, String sessionKey, String iv);
    
    /**
     * 校验微信签名
     * 
     * @param rawData 原始数据
     * @param signature 签名
     * @param sessionKey 会话密钥
     * @return 是否验证通过
     */
    boolean checkSignature(String rawData, String signature, String sessionKey);
}
