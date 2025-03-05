package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款订单实体类
 * 
 * @author System Architect
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_refund_order")
public class RefundOrder extends BaseEntity {
    
    /**
     * 商户订单号
     */
    private String orderNo;
    
    /**
     * 商户退款单号
     */
    private String refundNo;
    
    /**
     * 微信支付订单号
     */
    private String transactionId;
    
    /**
     * 微信退款单号
     */
    private String refundId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户微信openid
     */
    private String openid;
    
    /**
     * 订单金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 退款状态：0-退款中，1-退款成功，2-退款失败
     */
    private Integer refundStatus;
    
    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;
    
    /**
     * 退款失败原因
     */
    private String failReason;
    
    /**
     * 退款资金来源：1-可用余额退款，2-未结算资金退款
     */
    private Integer refundAccount;
    
    /**
     * 退款入账账户
     */
    private String refundRecvAccount;
    
    /**
     * 退款场景：1-订单取消，2-售后退款
     */
    private Integer scene;
    
    /**
     * 备注
     */
    private String remark;
}
