package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 * 
 * @author System Architect
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_order")
public class Order extends BaseEntity {
    
    /**
     * 订单编号，唯一标识
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 运费金额
     */
    private BigDecimal freightAmount;
    
    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 支付方式：1-微信支付
     */
    private Integer payType;
    
    /**
     * 订单来源：1-小程序
     */
    private Integer sourceType;
    
    /**
     * 订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已关闭，5-无效订单
     */
    private Integer status;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;
    
    /**
     * 确认收货时间
     */
    private LocalDateTime receiveTime;
    
    /**
     * 评价时间
     */
    private LocalDateTime commentTime;
    
    /**
     * 收货人姓名
     */
    private String receiverName;
    
    /**
     * 收货人电话
     */
    private String receiverPhone;
    
    /**
     * 收货人详细地址
     */
    private String receiverAddress;
    
    /**
     * 订单备注
     */
    private String note;
    
    /**
     * 是否已删除
     */
    private Integer deleted;
    
    /**
     * 订单项列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<OrderItem> orderItems;
}
