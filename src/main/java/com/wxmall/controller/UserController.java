package com.wxmall.controller;

import com.wxmall.common.api.CommonResult;
import com.wxmall.dto.UserLoginDTO;
import com.wxmall.dto.UserRegisterDTO;
import com.wxmall.dto.UserUpdateDTO;
import com.wxmall.dto.WxLoginDTO;
import com.wxmall.service.UserService;
import com.wxmall.service.WxService;
import com.wxmall.util.JwtUtils;
import com.wxmall.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(tags = "用户接口")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private WxService wxService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public CommonResult<UserVO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return CommonResult.success(userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public CommonResult<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        UserVO userVO = userService.getCurrentUser(token);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userVO);
        
        return CommonResult.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前用户信息")
    public CommonResult<UserVO> getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        UserVO userVO = userService.getCurrentUser(token);
        if (userVO == null) {
            return CommonResult.unauthorized(null);
        }
        return CommonResult.success(userVO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public CommonResult<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);
        return CommonResult.success("退出成功");
    }
    
    /**
     * 更新当前用户信息
     */
    @PostMapping("/update")
    @ApiOperation("更新当前用户信息")
    public CommonResult<UserVO> updateUserInfo(HttpServletRequest request, @Valid @RequestBody UserUpdateDTO updateDTO) {
        // 获取当前用户ID
        String token = request.getHeader("Authorization");
        Long userId = jwtUtils.getUserIdFromToken(token);
        
        // 更新用户信息
        UserVO userVO = userService.updateUserInfo(userId, updateDTO);
        
        // 更新Redis中的用户信息
        userService.getCurrentUser(token);
        
        return CommonResult.success(userVO);
    }
    
    /**
     * 微信登录
     */
    @PostMapping("/wx-login")
    @ApiOperation("微信登录")
    public CommonResult<Map<String, Object>> wxLogin(@Valid @RequestBody WxLoginDTO wxLoginDTO) {
        Map<String, Object> result = wxService.wxLogin(wxLoginDTO);
        return CommonResult.success(result);
    }
}
