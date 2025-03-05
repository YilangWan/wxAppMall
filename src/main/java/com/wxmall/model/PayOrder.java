package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体类
 * 
 * @author System Architect
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_pay_order")
public class PayOrder extends BaseEntity {
    
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
     * 支付金额（单位：分）
     */
    private BigDecimal payAmount;
    
    /**
     * 支付状态：0-未支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer payStatus;
    
    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;
    
    /**
     * 支付类型：1-JSAPI支付，2-H5支付，3-Native支付
     */
    private Integer payType;
    
    /**
     * 商品描述
     */
    private String body;
    
    /**
     * 附加数据
     */
    private String attach;
    
    /**
     * 支付失败原因
     */
    private String failReason;
    
    /**
     * 预支付交易会话标识
     */
    private String prepayId;
    
    /**
     * 交易起始时间
     */
    private LocalDateTime timeStart;
    
    /**
     * 交易结束时间
     */
    private LocalDateTime timeExpire;
    
    /**
     * 通知地址
     */
    private String notifyUrl;
    
    /**
     * 支付场景：1-商品购买，2-充值，3-会员购买
     */
    private Integer scene;
    
    /**
     * 备注
     */
    private String remark;
}
