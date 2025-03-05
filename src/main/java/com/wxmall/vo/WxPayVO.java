package com.wxmall.vo;

import lombok.Data;

import java.util.Map;

/**
 * 微信支付返回视图对象
 * 
 * @author System Architect
 */
@Data
public class WxPayVO {
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 预支付交易会话标识
     */
    private String prepayId;
    
    /**
     * 支付参数，用于小程序调起支付
     */
    private Map<String, String> payParams;
    
    /**
     * 二维码链接，用于Native支付
     */
    private String codeUrl;
    
    /**
     * H5支付链接，用于H5支付
     */
    private String h5Url;
}
