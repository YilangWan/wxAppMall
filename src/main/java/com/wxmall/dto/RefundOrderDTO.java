package com.wxmall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 退款订单数据传输对象
 * 
 * @author System Architect
 */
@Data
public class RefundOrderDTO {
    
    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;
    
    /**
     * 微信支付订单号
     */
    private String transactionId;
    
    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;
    
    /**
     * 订单总金额
     */
    @NotNull(message = "订单总金额不能为空")
    private BigDecimal totalAmount;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 用户openid
     */
    @NotBlank(message = "用户openid不能为空")
    private String openid;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 退款场景：1-订单取消，2-售后退款
     */
    @NotNull(message = "退款场景不能为空")
    private Integer scene;
    
    /**
     * 备注
     */
    private String remark;
}
