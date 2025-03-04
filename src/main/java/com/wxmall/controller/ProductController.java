package com.wxmall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wxmall.common.api.CommonPage;
import com.wxmall.common.api.CommonResult;
import com.wxmall.dto.ProductCreateDTO;
import com.wxmall.dto.ProductQueryDTO;
import com.wxmall.service.ProductService;
import com.wxmall.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@Api(tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("分页查询商品列表")
    @GetMapping
    public CommonResult<CommonPage<ProductVO>> listProducts(@Validated ProductQueryDTO queryDTO) {
        log.info("分页查询商品列表: {}", queryDTO);
        IPage<ProductVO> productPage = productService.listProducts(queryDTO);
        return CommonResult.success(CommonPage.restPage(productPage));
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public CommonResult<ProductVO> getProduct(@PathVariable Long id) {
        log.info("获取商品详情: {}", id);
        ProductVO product = productService.getProductById(id);
        return CommonResult.success(product);
    }

    @ApiOperation("创建商品")
    @PostMapping
    public CommonResult<ProductVO> createProduct(@Validated @RequestBody ProductCreateDTO productCreateDTO) {
        log.info("创建商品: {}", productCreateDTO);
        ProductVO productVO = productService.createProduct(productCreateDTO);
        return CommonResult.success(productVO);
    }
}
