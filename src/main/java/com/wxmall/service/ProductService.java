package com.wxmall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxmall.dto.ProductCreateDTO;
import com.wxmall.dto.ProductQueryDTO;
import com.wxmall.model.Product;
import com.wxmall.vo.ProductVO;

/**
 * 商品Service接口
 */
public interface ProductService extends IService<Product> {
    
    /**
     * 创建商品
     * @param productCreateDTO 商品创建DTO
     * @return 商品VO
     */
    ProductVO createProduct(ProductCreateDTO productCreateDTO);
    
    /**
     * 分页查询商品列表
     * @param queryDTO 查询参数
     * @return 商品分页列表
     */
    IPage<ProductVO> listProducts(ProductQueryDTO queryDTO);
    
    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品VO
     */
    ProductVO getProductById(Long id);
}
