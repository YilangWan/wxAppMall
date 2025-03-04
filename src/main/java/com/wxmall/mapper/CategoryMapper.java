package com.wxmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxmall.model.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 获取指定层级的分类列表
     * @param level 层级
     * @return 分类列表
     */
    List<Category> selectCategoriesByLevel(@Param("level") Integer level);
    
    /**
     * 获取指定父分类的子分类列表
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> selectCategoriesByParentId(@Param("parentId") Long parentId);
}
