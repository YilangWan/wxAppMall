package com.wxmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 微信小程序配置
 * 从application.yml中读取微信小程序相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxConfig {
    /**
     * 微信小程序appid
     */
    private String appid;
    
    /**
     * 微信小程序app secret
     */
    private String secret;
    
    /**
     * 微信小程序登录接口
     */
    private String codeToSessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
    
    /**
     * 配置RestTemplate，用于调用微信接口
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间
        factory.setConnectTimeout(5000);
        // 设置读取超时时间
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }
}
