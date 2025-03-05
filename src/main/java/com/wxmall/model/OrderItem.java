package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单项实体类
 * 
 * @author System Architect
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wx_order_item")
public class OrderItem extends BaseEntity {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品图片
     */
    private String productPic;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品品牌
     */
    private String productBrand;
    
    /**
     * 商品价格
     */
    private BigDecimal productPrice;
    
    /**
     * 购买数量
     */
    private Integer quantity;
    
    /**
     * 商品总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 商品规格属性
     */
    private String productAttr;
}
