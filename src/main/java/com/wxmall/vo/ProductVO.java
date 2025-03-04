package com.wxmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品VO
 */
@Data
public class ProductVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

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
     * 商品分类
     */
    private CategoryVO category;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
