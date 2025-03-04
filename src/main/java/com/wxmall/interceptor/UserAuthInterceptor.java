package com.wxmall.interceptor;

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
 * 用户认证拦截器
 * 用于拦截需要用户登录的请求
 */
@Slf4j
@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("用户认证拦截: {}", request.getRequestURI());
        
        // 获取token
        String token = null == request.getHeader("Authorization") ?
            request.getParameter("token")
            : request.getHeader("Authorization");;
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        
        // 获取当前用户
        UserVO userVO = userService.getCurrentUser(token);
        if (userVO == null) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        
        return true;
    }
}
