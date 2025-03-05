package com.wxmall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 支付订单数据传输对象
 * 
 * @author System Architect
 */
@Data
public class PayOrderDTO {
    
    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;
    
    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    private BigDecimal payAmount;
    
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
     * 商品描述
     */
    @NotBlank(message = "商品描述不能为空")
    private String body;
    
    /**
     * 附加数据
     */
    private String attach;
    
    /**
     * 支付场景：1-商品购买，2-充值，3-会员购买
     */
    @NotNull(message = "支付场景不能为空")
    private Integer scene;
    
    /**
     * 支付类型：1-JSAPI支付，2-H5支付，3-Native支付
     */
    @NotNull(message = "支付类型不能为空")
    private Integer payType;
    
    /**
     * 客户端IP
     */
    private String clientIp;
    
    /**
     * 备注
     */
    private String remark;
}
