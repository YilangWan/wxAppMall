package com.wxmall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxmall.common.exception.BusinessException;
import com.wxmall.dto.UserLoginDTO;
import com.wxmall.dto.UserRegisterDTO;
import com.wxmall.dto.UserUpdateDTO;
import com.wxmall.mapper.UserMapper;
import com.wxmall.model.User;
import com.wxmall.service.RedisService;
import com.wxmall.service.UserService;
import com.wxmall.util.JwtUtils;
import com.wxmall.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisService redisService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * Redis缓存key前缀
     */
    private static final String REDIS_KEY_USER = "user:";
    
    /**
     * Redis缓存过期时间（秒）
     */
    private static final long REDIS_EXPIRE_TIME = 86400;

    @Override
    public UserVO register(UserRegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        
        // 检查用户名是否已存在
        User existUser = baseMapper.selectByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户实体
        User user = new User();
        BeanUtil.copyProperties(registerDTO, user);
        
        // 密码加密
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        
        // 设置默认值
        user.setRole(0); // 默认为普通用户
        user.setStatus(1); // 默认启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 保存用户
        boolean success = save(user);
        if (!success) {
            throw new BusinessException("用户注册失败");
        }
        
        // 转换为VO
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        userVO.setId(user.getId());
        
        // 脱敏处理
        maskSensitiveInfo(userVO);
        
        return userVO;
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        
        // 查询用户
        User user = baseMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);
        
        // 生成token
        String token = jwtUtils.generateToken(user.getId());
        
        // 转换为VO
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        userVO.setId(user.getId());
        
        // 脱敏处理
        maskSensitiveInfo(userVO);
        
        // 存入Redis
        redisService.set(REDIS_KEY_USER + token, userVO, REDIS_EXPIRE_TIME);
        
        return token;
    }

    @Override
    public UserVO getCurrentUser(String token) {
        log.info("获取当前用户信息");
        
        if (StrUtil.isBlank(token)) {
            return null;
        }
        
        // 验证token
        if (!jwtUtils.validateToken(token)) {
            return null;
        }
        
        // 从Redis获取用户信息
        UserVO userVO = (UserVO) redisService.get(REDIS_KEY_USER + token);
        if (userVO != null) {
            return userVO;
        }
        
        // Redis中不存在，从数据库获取
        Long id = jwtUtils.getUserIdFromToken(token);
        if (id == null) {
            return null;
        }
        
        User user = getById(id);
        if (user == null) {
            return null;
        }
        
        // 转换为VO
        userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        userVO.setId(user.getId());
        
        // 脱敏处理
        maskSensitiveInfo(userVO);
        
        // 存入Redis
        redisService.set(REDIS_KEY_USER + token, userVO, REDIS_EXPIRE_TIME);
        
        return userVO;
    }

    @Override
    public void logout(String token) {
        log.info("用户退出登录");
        
        if (StrUtil.isBlank(token)) {
            return;
        }
        
        // 清除Redis中的用户信息
        redisService.del(REDIS_KEY_USER + token);
    }
    
    @Override
    public boolean isAdmin(Long id) {
        User user = getById(id);
        return user != null && user.getRole() != null && user.getRole() == 1;
    }
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        log.info("更新用户信息: userId={}", userId);
        
        // 获取用户信息
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果要修改密码，需要验证原密码
        if (StrUtil.isNotBlank(updateDTO.getNewPassword())) {
            if (StrUtil.isBlank(updateDTO.getOldPassword())) {
                throw new BusinessException("修改密码时必须提供原密码");
            }
            
            // 验证原密码
            boolean passwordMatch = BCrypt.checkpw(updateDTO.getOldPassword(), user.getPassword());
            if (!passwordMatch) {
                throw new BusinessException("原密码不正确");
            }
            
            // 设置新密码
            user.setPassword(BCrypt.hashpw(updateDTO.getNewPassword()));
        }
        
        // 更新用户信息
        if (StrUtil.isNotBlank(updateDTO.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }
        
        if (StrUtil.isNotBlank(updateDTO.getAvatar())) {
            user.setAvatar(updateDTO.getAvatar());
        }
        
        if (StrUtil.isNotBlank(updateDTO.getPhone())) {
            user.setPhone(updateDTO.getPhone());
        }
        
        if (StrUtil.isNotBlank(updateDTO.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        
        // 保存更新
        updateById(user);
        
        // 转换为VO并返回
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        
        // 脱敏处理
        maskSensitiveInfo(userVO);
        
        return userVO;
    }
    
    /**
     * 脱敏处理
     * @param userVO 用户VO
     */
    private void maskSensitiveInfo(UserVO userVO) {
        // 手机号脱敏
        if (StrUtil.isNotBlank(userVO.getPhone())) {
            userVO.setPhone(userVO.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        
        // 邮箱脱敏
        if (StrUtil.isNotBlank(userVO.getEmail())) {
            String email = userVO.getEmail();
            int index = email.indexOf("@");
            if (index > 1) {
                String prefix = email.substring(0, Math.min(3, index));
                String suffix = email.substring(index);
                userVO.setEmail(prefix + "****" + suffix);
            }
        }
    }
}
