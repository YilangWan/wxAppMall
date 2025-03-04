package com.wxmall.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 商品创建DTO
 */
@Data
@ApiModel(description = "商品创建参数")
public class ProductCreateDTO {

    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "商品价格", required = true)
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @ApiModelProperty(value = "商品原价")
    private BigDecimal originalPrice;

    @ApiModelProperty(value = "商品库存", required = true)
    @NotNull(message = "商品库存不能为空")
    private Integer stock;

    @ApiModelProperty(value = "商品图片")
    private String pic;

    @ApiModelProperty(value = "商品单位")
    private String unit;

    @ApiModelProperty(value = "商品重量，默认为克")
    private BigDecimal weight;

    @ApiModelProperty(value = "商品关键字")
    private String keywords;

    @ApiModelProperty(value = "商品详情")
    private String detail;

    @ApiModelProperty(value = "商品分类ID", required = true)
    @NotNull(message = "商品分类ID不能为空")
    private Long categoryId;

    @ApiModelProperty(value = "上架状态：0->下架；1->上架", required = true)
    @NotNull(message = "上架状态不能为空")
    private Integer publishStatus;
}