package com.wxmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wxmall.model.Category;
import com.wxmall.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类Service
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryVO getCategoryById(Long id);
    
    /**
     * 获取所有一级分类
     * @return 一级分类列表
     */
    List<CategoryVO> getFirstLevelCategories();
    
    /**
     * 获取指定父分类的子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CategoryVO> getChildCategories(Long parentId);
    
    /**
     * 获取分类树
     * @return 分类树
     */
    List<CategoryVO> getCategoryTree();
}
