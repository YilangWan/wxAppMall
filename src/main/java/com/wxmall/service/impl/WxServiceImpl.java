package com.wxmall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wxmall.common.exception.BusinessException;
import com.wxmall.config.WxConfig;
import com.wxmall.dto.WxLoginDTO;
import com.wxmall.mapper.UserMapper;
import com.wxmall.model.User;
import com.wxmall.service.UserService;
import com.wxmall.service.WxService;
import com.wxmall.util.JwtUtils;
import com.wxmall.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信服务实现类
 * 实现微信登录、用户信息解密等功能
 */
@Slf4j
@Service
public class WxServiceImpl implements WxService {

    @Autowired
    private WxConfig wxConfig;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 微信登录
     * 
     * @param wxLoginDTO 微信登录参数
     * @return 登录结果，包含token和用户信息
     */
    @Override
    public Map<String, Object> wxLogin(WxLoginDTO wxLoginDTO) {
        // 1. 获取微信登录凭证
        String code = wxLoginDTO.getCode();
        if (code == null || code.isEmpty()) {
            throw new BusinessException("微信登录凭证不能为空");
        }
        
        // 2. 请求微信服务器获取openid和session_key
        JSONObject sessionInfo = getSessionInfo(code);
        
        String openid = sessionInfo.getString("openid");
        String sessionKey = sessionInfo.getString("session_key");
        String unionid = sessionInfo.getString("unionid"); // 可能为空
        
        if (openid == null || sessionKey == null) {
            log.error("微信登录失败，无法获取openid或session_key: {}", sessionInfo);
            throw new BusinessException("微信登录失败");
        }
        
        log.info("微信登录成功，openid: {}", openid);
        
        // 3. 验证签名（如果有）
        if (wxLoginDTO.getRawData() != null && wxLoginDTO.getSignature() != null) {
            boolean valid = checkSignature(wxLoginDTO.getRawData(), wxLoginDTO.getSignature(), sessionKey);
            if (!valid) {
                log.warn("微信登录签名验证失败");
                throw new BusinessException("签名验证失败");
            }
        }
        
        // 4. 解密用户信息（如果有）
        JSONObject userInfo = null;
        if (wxLoginDTO.getEncryptedData() != null && wxLoginDTO.getIv() != null) {
            try {
                String decryptedData = decryptUserInfo(
                    wxLoginDTO.getEncryptedData(), 
                    sessionKey, 
                    wxLoginDTO.getIv()
                );
                userInfo = JSON.parseObject(decryptedData);
                log.info("解密用户信息成功: {}", userInfo);
            } catch (Exception e) {
                log.error("解密用户信息失败", e);
                // 解密失败不影响登录流程，继续执行
            }
        }
        
        // 5. 查询用户是否存在
        User user = getUserByOpenid(openid);
        
        // 6. 用户不存在则注册，存在则更新
        if (user == null) {
            // 创建新用户
            user = createWxUser(openid, sessionKey, unionid, userInfo);
        } else {
            // 更新用户信息
            updateWxUser(user, sessionKey, userInfo);
        }
        
        // 7. 生成token
        String token = jwtUtils.generateToken(user.getId());
        
        // 8. 获取用户信息
        UserVO userVO = userService.getCurrentUser(token);
        
        // 9. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userVO);
        
        return result;
    }
    
    /**
     * 根据openid查询用户
     * 
     * @param openid 微信openid
     * @return 用户对象，不存在则返回null
     */
    private User getUserByOpenid(String openid) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openid);
        return userMapper.selectOne(queryWrapper);
    }
    
    /**
     * 创建微信用户
     * 
     * @param openid 微信openid
     * @param sessionKey 会话密钥
     * @param unionid 微信unionid
     * @param userInfo 用户信息
     * @return 创建的用户对象
     */
    private User createWxUser(String openid, String sessionKey, String unionid, JSONObject userInfo) {
        User user = new User();
        user.setOpenid(openid);
        user.setSessionKey(sessionKey);
        user.setUnionid(unionid);
        user.setRole(0); // 普通用户
        user.setStatus(1); // 启用状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setLastLoginTime(LocalDateTime.now());
        
        // 设置用户名为openid前8位
        user.setUsername("wx_" + openid.substring(0, Math.min(8, openid.length())));
        
        // 如果有解密的用户信息，则使用微信的昵称和头像
        if (userInfo != null) {
            user.setNickname(userInfo.getString("nickName"));
            user.setAvatar(userInfo.getString("avatarUrl"));
            // 可以添加更多用户信息，如性别、国家、省份、城市等
        } else {
            // 默认昵称
            user.setNickname("微信用户");
        }
        
        userMapper.insert(user);
        log.info("创建微信用户成功: {}", user.getId());
        return user;
    }
    
    /**
     * 更新微信用户信息
     * 
     * @param user 用户对象
     * @param sessionKey 会话密钥
     * @param userInfo 用户信息
     */
    private void updateWxUser(User user, String sessionKey, JSONObject userInfo) {
        user.setSessionKey(sessionKey);
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 如果有新的用户信息，则更新
        if (userInfo != null) {
            // 只有当用户昵称为空或者是默认昵称时才更新
            if (user.getNickname() == null || user.getNickname().startsWith("微信用户")) {
                user.setNickname(userInfo.getString("nickName"));
            }
            
            // 只有当用户头像为空时才更新
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                user.setAvatar(userInfo.getString("avatarUrl"));
            }
        }
        
        userMapper.updateById(user);
        log.info("更新微信用户信息成功: {}", user.getId());
    }
    
    /**
     * 获取微信session信息
     * 
     * @param code 微信临时登录凭证
     * @return 包含openid、session_key等信息的JSONObject
     */
    private JSONObject getSessionInfo(String code) {
        String url = wxConfig.getCodeToSessionUrl() + 
                "?appid=" + wxConfig.getAppid() + 
                "&secret=" + wxConfig.getSecret() + 
                "&js_code=" + code + 
                "&grant_type=authorization_code";
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            log.info("微信登录返回: {}", body);
            
            JSONObject result = JSON.parseObject(body);
            
            // 检查是否有错误
            if (result.getInteger("errcode") != null && result.getInteger("errcode") != 0) {
                log.error("微信登录失败: {}", result);
                throw new BusinessException("微信登录失败: " + result.getString("errmsg"));
            }
            
            return result;
        } catch (Exception e) {
            log.error("请求微信服务器失败", e);
            throw new BusinessException("微信登录失败，请稍后重试");
        }
    }
    
    /**
     * 解密微信用户信息
     * 
     * @param encryptedData 加密数据
     * @param sessionKey 会话密钥
     * @param iv 加密算法的初始向量
     * @return 解密后的用户信息JSON字符串
     */
    @Override
    public String decryptUserInfo(String encryptedData, String sessionKey, String iv) {
        try {
            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(sessionKey);
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
            
            // 设置解密参数
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // 执行解密
            byte[] decrypted = cipher.doFinal(dataBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("微信用户信息解密失败", e);
            throw new BusinessException("微信用户信息解密失败");
        }
    }
    
    /**
     * 校验微信签名
     * 
     * @param rawData 原始数据
     * @param signature 签名
     * @param sessionKey 会话密钥
     * @return 是否验证通过
     */
    @Override
    public boolean checkSignature(String rawData, String signature, String sessionKey) {
        // 签名校验
        String checkSignature = DigestUtils.sha1Hex(rawData + sessionKey);
        return signature.equals(checkSignature);
    }
}
