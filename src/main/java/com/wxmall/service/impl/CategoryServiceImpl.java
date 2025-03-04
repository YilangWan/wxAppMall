package com.wxmall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxmall.common.exception.BusinessException;
import com.wxmall.mapper.CategoryMapper;
import com.wxmall.model.Category;
import com.wxmall.service.CategoryService;
import com.wxmall.service.RedisService;
import com.wxmall.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类Service实现类
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private RedisService redisService;

    /**
     * Redis缓存key前缀
     */
    private static final String REDIS_KEY_CATEGORY = "category:";

    /**
     * Redis缓存过期时间（秒）
     */
    private static final long REDIS_EXPIRE_TIME = 3600;

    @Override
    public CategoryVO getCategoryById(Long id) {
        log.info("获取分类详情: {}", id);
        
        // 尝试从缓存获取
        String cacheKey = REDIS_KEY_CATEGORY + id;
        CategoryVO cachedCategory = (CategoryVO) redisService.get(cacheKey);
        if (cachedCategory != null) {
            log.info("从缓存获取分类详情成功");
            return cachedCategory;
        }
        
        // 从数据库获取
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 转换为VO
        CategoryVO categoryVO = new CategoryVO();
        BeanUtil.copyProperties(category, categoryVO);
        
        // 获取子分类
        List<Category> children = baseMapper.selectCategoriesByParentId(id);
        if (children != null && !children.isEmpty()) {
            List<CategoryVO> childrenVO = children.stream().map(child -> {
                CategoryVO childVO = new CategoryVO();
                BeanUtil.copyProperties(child, childVO);
                return childVO;
            }).collect(Collectors.toList());
            categoryVO.setChildren(childrenVO);
        }
        
        // 存入缓存
        redisService.set(cacheKey, categoryVO, REDIS_EXPIRE_TIME);
        
        return categoryVO;
    }

    @Override
    public List<CategoryVO> getFirstLevelCategories() {
        log.info("获取所有一级分类");
        
        // 尝试从缓存获取
        String cacheKey = REDIS_KEY_CATEGORY + "first-level";
        List<CategoryVO> cachedCategories = (List<CategoryVO>) redisService.get(cacheKey);
        if (cachedCategories != null) {
            log.info("从缓存获取一级分类列表成功");
            return cachedCategories;
        }
        
        // 从数据库获取
        List<Category> categories = baseMapper.selectCategoriesByLevel(1);
        
        // 转换为VO
        List<CategoryVO> categoryVOList = categories.stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtil.copyProperties(category, categoryVO);
            return categoryVO;
        }).collect(Collectors.toList());
        
        // 存入缓存
        redisService.set(cacheKey, categoryVOList, REDIS_EXPIRE_TIME);
        
        return categoryVOList;
    }

    @Override
    public List<CategoryVO> getChildCategories(Long parentId) {
        log.info("获取子分类列表: {}", parentId);
        
        // 尝试从缓存获取
        String cacheKey = REDIS_KEY_CATEGORY + "children:" + parentId;
        List<CategoryVO> cachedCategories = (List<CategoryVO>) redisService.get(cacheKey);
        if (cachedCategories != null) {
            log.info("从缓存获取子分类列表成功");
            return cachedCategories;
        }
        
        // 从数据库获取
        List<Category> categories = baseMapper.selectCategoriesByParentId(parentId);
        
        // 转换为VO
        List<CategoryVO> categoryVOList = categories.stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtil.copyProperties(category, categoryVO);
            return categoryVO;
        }).collect(Collectors.toList());
        
        // 存入缓存
        redisService.set(cacheKey, categoryVOList, REDIS_EXPIRE_TIME);
        
        return categoryVOList;
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        log.info("获取分类树");
        
        // 尝试从缓存获取
        String cacheKey = REDIS_KEY_CATEGORY + "tree";
        List<CategoryVO> cachedTree = (List<CategoryVO>) redisService.get(cacheKey);
        if (cachedTree != null) {
            log.info("从缓存获取分类树成功");
            return cachedTree;
        }
        
        // 获取所有一级分类
        List<CategoryVO> firstLevelCategories = getFirstLevelCategories();
        
        // 递归获取子分类
        for (CategoryVO categoryVO : firstLevelCategories) {
            buildCategoryTree(categoryVO);
        }
        
        // 存入缓存
        redisService.set(cacheKey, firstLevelCategories, REDIS_EXPIRE_TIME);
        
        return firstLevelCategories;
    }
    
    /**
     * 递归构建分类树
     * @param categoryVO 分类VO
     */
    private void buildCategoryTree(CategoryVO categoryVO) {
        List<CategoryVO> children = getChildCategories(categoryVO.getId());
        if (children != null && !children.isEmpty()) {
            categoryVO.setChildren(children);
            for (CategoryVO child : children) {
                buildCategoryTree(child);
            }
        }
    }
}
