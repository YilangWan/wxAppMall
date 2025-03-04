package com.wxmall.controller;

import com.wxmall.common.api.CommonResult;
import com.wxmall.service.CategoryService;
import com.wxmall.vo.CategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@Api(tags = "商品分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public CommonResult<CategoryVO> getCategory(@PathVariable Long id) {
        CategoryVO category = categoryService.getCategoryById(id);
        return CommonResult.success(category);
    }

    @ApiOperation("获取所有一级分类")
    @GetMapping("/first-level")
    public CommonResult<List<CategoryVO>> getFirstLevelCategories() {
        List<CategoryVO> categories = categoryService.getFirstLevelCategories();
        return CommonResult.success(categories);
    }

    @ApiOperation("获取子分类")
    @GetMapping("/{id}/children")
    public CommonResult<List<CategoryVO>> getChildCategories(@PathVariable Long id) {
        List<CategoryVO> categories = categoryService.getChildCategories(id);
        return CommonResult.success(categories);
    }

    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public CommonResult<List<CategoryVO>> getCategoryTree() {
        List<CategoryVO> categoryTree = categoryService.getCategoryTree();
        return CommonResult.success(categoryTree);
    }
}
