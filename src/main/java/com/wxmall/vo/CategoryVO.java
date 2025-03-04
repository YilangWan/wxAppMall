package com.wxmall.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类VO
 */
@Data
@ApiModel(description = "商品分类VO")
public class CategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("分类ID")
    private Long id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("父分类ID")
    private Long parentId;

    @ApiModelProperty("层级")
    private Integer level;

    @ApiModelProperty("是否显示")
    private Boolean visible;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("描述")
    private String description;
    
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    
    @ApiModelProperty("子分类列表")
    private List<CategoryVO> children;
}
