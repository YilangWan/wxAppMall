package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付日志实体类
 * 
 * @author System Architect
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_pay_log")
public class PayLog extends BaseEntity {
    
    /**
     * 商户订单号
     */
    private String orderNo;
    
    /**
     * 微信支付订单号
     */
    private String transactionId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户微信openid
     */
    private String openid;
    
    /**
     * 操作类型：1-创建订单，2-支付成功，3-支付失败，4-退款申请，5-退款成功，6-退款失败
     */
    private Integer operateType;
    
    /**
     * 操作内容
     */
    private String content;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 响应结果
     */
    private String responseResult;
    
    /**
     * 操作IP
     */
    private String ip;
    
    /**
     * 操作来源：1-小程序，2-H5，3-APP
     */
    private Integer source;
}
