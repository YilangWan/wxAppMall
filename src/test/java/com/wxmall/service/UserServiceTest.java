package com.wxmall.service;

import com.wxmall.common.exception.BusinessException;
import com.wxmall.dto.UserLoginDTO;
import com.wxmall.dto.UserRegisterDTO;
import com.wxmall.mapper.UserMapper;
import com.wxmall.model.User;
import com.wxmall.service.impl.UserServiceImpl;
import com.wxmall.util.JwtUtils;
import com.wxmall.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 */
@SpringBootTest
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试用户注册成功
     */
    @Test
    public void testRegisterSuccess() {
        // 准备测试数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("testuser");
        registerDTO.setPassword("123456");
        registerDTO.setNickname("测试用户");
        registerDTO.setPhone("13800138000");
        registerDTO.setEmail("test@wxmall.com");

        // 模拟依赖行为
        when(userMapper.selectByUsername(anyString())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        UserVO result = userService.register(registerDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        
        // 验证交互
        verify(userMapper, times(1)).selectByUsername("testuser");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    /**
     * 测试用户名已存在的情况
     */
    @Test
    public void testRegisterUsernameExists() {
        // 准备测试数据
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("existinguser");
        registerDTO.setPassword("123456");
        
        // 模拟依赖行为
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        when(userMapper.selectByUsername("existinguser")).thenReturn(existingUser);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerDTO);
        });
        
        assertEquals("用户名已存在", exception.getMessage());
        
        // 验证交互
        verify(userMapper, times(1)).selectByUsername("existinguser");
        verify(userMapper, never()).insert(any(User.class));
    }

    /**
     * 测试用户登录成功
     */
    @Test
    public void testLoginSuccess() {
        // 准备测试数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("123456");
        
        // 模拟依赖行为
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        // 使用BCrypt加密的"123456"密码
        user.setPassword("$2a$10$3.YMjAO7knGjLpYwXbJvUOkt6.1TGa8xp.P9AjfMvZ6PpPg.Yb9Uy");
        user.setNickname("测试用户");
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        when(userMapper.selectByUsername("testuser")).thenReturn(user);
        when(jwtUtils.generateToken(anyLong())).thenReturn("test.jwt.token");
//        when(userMapper.updateLastLoginTime(anyLong(), any(Date.class))).thenReturn(1);
        
        // 执行测试
        String token = userService.login(loginDTO);
        
        // 验证结果
        assertNotNull(token);
        assertEquals("test.jwt.token", token);
        
        // 验证交互
        verify(userMapper, times(1)).selectByUsername("testuser");
//        verify(jwtUtils, times(1)).generateToken(anyString());
//        verify(userMapper, times(1)).updateLastLoginTime(anyLong(), any(Date.class));
    }

    /**
     * 测试用户名不存在的情况
     */
    @Test
    public void testLoginUsernameNotFound() {
        // 准备测试数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("nonexistentuser");
        loginDTO.setPassword("123456");
        
        // 模拟依赖行为
        when(userMapper.selectByUsername("nonexistentuser")).thenReturn(null);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
        
        // 验证交互
        verify(userMapper, times(1)).selectByUsername("nonexistentuser");
//        verify(jwtUtils, never()).generateToken(anyString());
//        verify(userMapper, never()).updateLastLoginTime(anyLong(), any(Date.class));
    }

    /**
     * 测试密码错误的情况
     */
    @Test
    public void testLoginWrongPassword() {
        // 准备测试数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongpassword");
        
        // 模拟依赖行为
        User user = new User();
//        user.setUserId(1L);
        user.setUsername("testuser");
        // 使用BCrypt加密的"123456"密码
        user.setPassword("$2a$10$3.YMjAO7knGjLpYwXbJvUOkt6.1TGa8xp.P9AjfMvZ6PpPg.Yb9Uy");
        user.setStatus(1);
        
        when(userMapper.selectByUsername("testuser")).thenReturn(user);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
        
        assertEquals("用户名或密码错误", exception.getMessage());
        
        // 验证交互
        verify(userMapper, times(1)).selectByUsername("testuser");
//        verify(jwtUtils, never()).generateToken(anyString());
//        verify(userMapper, never()).updateLastLoginTime(anyLong(), any(Date.class));
    }

    /**
     * 测试账号被禁用的情况
     */
    @Test
    public void testLoginAccountDisabled() {
        // 准备测试数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("disableduser");
        loginDTO.setPassword("123456");
        
        // 模拟依赖行为
        User user = new User();
//        user.setUserId(1L);
        user.setUsername("disableduser");
        // 使用BCrypt加密的"123456"密码
        user.setPassword("$2a$10$3.YMjAO7knGjLpYwXbJvUOkt6.1TGa8xp.P9AjfMvZ6PpPg.Yb9Uy");
        user.setStatus(0); // 禁用状态
        
        when(userMapper.selectByUsername("disableduser")).thenReturn(user);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginDTO);
        });
        
        assertEquals("账号已被禁用", exception.getMessage());
        
        // 验证交互
        verify(userMapper, times(1)).selectByUsername("disableduser");
//        verify(jwtUtils, never()).generateToken(anyString());
//        verify(userMapper, never()).updateLastLoginTime(anyLong(), any(Date.class));
    }
}
