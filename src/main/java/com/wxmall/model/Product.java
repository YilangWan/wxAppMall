package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    private Long productId;
    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品原价
     */
    private BigDecimal originalPrice;

    /**
     * 商品库存
     */
    private Integer stock;

    /**
     * 商品图片
     */
    private String pic;

    /**
     * 商品销量
     */
    private Integer sale;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 商品重量，默认为克
     */
    private BigDecimal weight;

    /**
     * 商品关键字
     */
    private String keywords;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 上架状态：0->下架；1->上架
     */
    private Integer publishStatus;
}
