package com.wxmall.interceptor;

import com.wxmall.common.exception.ForbiddenException;
import com.wxmall.common.exception.UnauthorizedException;
import com.wxmall.service.UserService;
import com.wxmall.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器
 * 用于拦截需要管理员权限的请求
 */
@Slf4j
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("管理员权限拦截: {}", request.getRequestURI());
        
        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        
        // 获取当前用户
        UserVO userVO = userService.getCurrentUser(token);
        if (userVO == null) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        
        // 检查是否为管理员
        if (!userVO.isAdmin()) {
            throw new ForbiddenException("没有管理员权限");
        }
        
        return true;
    }
}
