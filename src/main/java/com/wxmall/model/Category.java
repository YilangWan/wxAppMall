package com.wxmall.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class Category extends BaseEntity {
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 层级
     */
    private Integer level;
    
    /**
     * 是否显示
     */
    private Boolean visible;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 描述
     */
    private String description;
}
