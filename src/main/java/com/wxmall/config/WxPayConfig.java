package com.wxmall.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * 微信支付配置类
 * 
 * @author System Architect
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayConfig {
    
    /**
     * 小程序appId
     */
    private String appId;
    
    /**
     * 商户号
     */
    private String mchId;
    
    /**
     * 商户API密钥
     */
    private String mchKey;
    
    /**
     * 商户证书路径
     */
    private String keyPath;
    
    /**
     * 支付结果通知地址
     */
    private String notifyUrl;
    
    /**
     * 退款结果通知地址
     */
    private String refundNotifyUrl;
    
    /**
     * 交易类型
     */
    private String tradeType;
    
    /**
     * 签名类型
     */
    private String signType;
    
    /**
     * 是否使用沙箱环境
     */
    private Boolean sandbox;
    
    /**
     * API V3密钥
     */
    private String apiV3Key;
    
    /**
     * 私钥路径
     */
    private String privateKeyPath;
    
    /**
     * 证书序列号
     */
    private String certSerialNo;
    
    /**
     * 创建微信支付HTTP客户端
     */
    @Bean
    public CloseableHttpClient wxPayClient() {
        try {
            // 检查私钥路径是否配置
            if (privateKeyPath == null || privateKeyPath.trim().isEmpty()) {
                log.warn("微信支付私钥路径未配置，将使用模拟的HttpClient");
                return HttpClients.createDefault();
            }
            
            // 尝试加载商户私钥
            ClassPathResource resource = new ClassPathResource(privateKeyPath);
            if (!resource.exists()) {
                log.warn("微信支付私钥文件不存在：{}，将使用模拟的HttpClient", privateKeyPath);
                return HttpClients.createDefault();
            }
            
            PrivateKey privateKey = PemUtil.loadPrivateKey(resource.getInputStream());
            
            // 检查证书序列号是否配置
            if (certSerialNo == null || certSerialNo.trim().isEmpty()) {
                log.warn("微信支付证书序列号未配置，将使用模拟的HttpClient");
                return HttpClients.createDefault();
            }
            
            // 检查API V3密钥是否配置
            if (apiV3Key == null || apiV3Key.trim().isEmpty()) {
                log.warn("微信支付API V3密钥未配置，将使用模拟的HttpClient");
                return HttpClients.createDefault();
            }
            
            // 创建签名器
            PrivateKeySigner signer = new PrivateKeySigner(certSerialNo, privateKey);
            
            // 创建认证对象
            WechatPay2Credentials credentials = new WechatPay2Credentials(mchId, signer);
            
            // 创建证书自动更新验证器
            AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                    credentials, apiV3Key.getBytes(StandardCharsets.UTF_8));
            
            // 创建HttpClient构建器
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(mchId, certSerialNo, privateKey)
                    .withValidator(new WechatPay2Validator(verifier));
            
            // 创建HttpClient
            return builder.build();
        } catch (Exception e) {
            log.error("微信支付客户端初始化失败", e);
            log.warn("将使用模拟的HttpClient替代");
            return HttpClients.createDefault();
        }
    }
}
