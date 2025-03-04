package com.wxmall.config;

import com.wxmall.interceptor.AdminAuthInterceptor;
import com.wxmall.interceptor.UserAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;
    
    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决Swagger UI的资源映射
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加用户认证拦截器，拦截需要登录的接口
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns(
                        "/api/user/info",
                        "/api/user/logout",
                        "/api/user/update",
                        "/api/order/**"
                )
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/api/user/wx-login"
                );
        
        // 添加管理员权限拦截器，拦截需要管理员权限的接口
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/api/product/create",
                        "/api/product/update/**",
                        "/api/product/delete/**",
                        "/api/category/create",
                        "/api/category/update/**",
                        "/api/category/delete/**"
                );
    }
}
