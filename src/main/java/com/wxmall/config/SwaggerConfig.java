package com.wxmall.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger配置类
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.wxmall.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("微信商城小程序API文档")
                .description("微信商城小程序后端API接口文档")
                .contact(new Contact("WxMall", "http://www.wxmall.com", "admin@wxmall.com"))
                .version("1.0")
                .build();
    }
    
    /**
     * 设置授权信息
     */
    private List<ApiKey> securitySchemes() {
        List<ApiKey> result = new ArrayList<>();
        // 添加JWT token认证信息
        result.add(new ApiKey("Authorization", "Authorization", "header"));
        return result;
    }

    /**
     * 设置需要授权的路径
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> result = new ArrayList<>();
        result.add(getContextByPath("/api/user/info.*"));
        result.add(getContextByPath("/api/user/logout.*"));
        result.add(getContextByPath("/api/user/update.*"));
        result.add(getContextByPath("/api/product/create.*"));
        result.add(getContextByPath("/api/product/update.*"));
        result.add(getContextByPath("/api/product/delete.*"));
        result.add(getContextByPath("/api/category/create.*"));
        result.add(getContextByPath("/api/category/update.*"));
        result.add(getContextByPath("/api/category/delete.*"));
        return result;
    }

    /**
     * 根据路径获取上下文
     */
    private SecurityContext getContextByPath(String pathRegex) {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(pathRegex))
                .build();
    }

    /**
     * 默认的全局鉴权策略
     */
    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> result = new ArrayList<>();
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        result.add(new SecurityReference("Authorization", authorizationScopes));
        return result;
    }
}
