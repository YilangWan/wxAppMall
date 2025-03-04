package com.wxmall.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 微信登录参数DTO
 * 用于接收微信小程序登录时传递的参数
 */
@Data
@ApiModel(description = "微信登录参数")
public class WxLoginDTO {
    
    /**
     * 微信临时登录凭证
     */
    @ApiModelProperty(value = "微信临时登录凭证", required = true)
    private String code;
    
    /**
     * 用户信息加密数据
     */
    @ApiModelProperty("用户信息加密数据")
    private String encryptedData;
    
    /**
     * 加密算法的初始向量
     */
    @ApiModelProperty("加密算法的初始向量")
    private String iv;
    
    /**
     * 用户非敏感信息
     */
    @ApiModelProperty("用户非敏感信息")
    private String rawData;
    
    /**
     * 签名
     */
    @ApiModelProperty("签名")
    private String signature;
}
