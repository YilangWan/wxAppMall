package com.wxmall.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 商品查询参数DTO
 */
@Data
public class ProductQueryDTO {
    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为1")
    private Long pageSize = 10L;

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 排序方式：price_asc-价格升序；price_desc-价格降序；sale_desc-销量降序
     */
    private String orderBy;
}
