package com.wxmall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxmall.common.exception.BusinessException;
import com.wxmall.dto.ProductCreateDTO;
import com.wxmall.dto.ProductQueryDTO;
import com.wxmall.mapper.ProductMapper;
import com.wxmall.model.Product;
import com.wxmall.service.CategoryService;
import com.wxmall.service.ProductService;
import com.wxmall.service.RedisService;
import com.wxmall.util.SnowflakeIdGenerator;
import com.wxmall.vo.CategoryVO;
import com.wxmall.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 商品Service实现类
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private RedisService redisService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * Redis缓存key前缀
     */
    private static final String REDIS_KEY_PRODUCT = "product:";
    
    /**
     * Redis缓存过期时间（秒）
     */
    private static final long REDIS_EXPIRE_TIME = 3600;

    @Override
    public ProductVO createProduct(ProductCreateDTO productCreateDTO) {
        log.info("创建商品: {}", productCreateDTO);
        
        // 创建商品实体
        Product product = new Product();
        BeanUtil.copyProperties(productCreateDTO, product);

        // 使用雪花算法生成ID
        long id = snowflakeIdGenerator.nextId();
        product.setProductId(id);
        
        // 设置初始销量为0
        product.setSale(0);
        
        // 设置分类ID
        product.setCategoryId(productCreateDTO.getCategoryId());
        
        // 保存商品
        boolean success = save(product);
        if (!success) {
            throw new BusinessException("商品创建失败");
        }
        
        // 转换为VO
        ProductVO productVO = new ProductVO();
        BeanUtil.copyProperties(product, productVO);
        
        // 设置分类信息
        CategoryVO categoryVO = categoryService.getCategoryById(product.getCategoryId());
        productVO.setCategory(categoryVO);
        
        // 清除相关缓存
        clearProductCache(productVO.getId());
        
        return productVO;
    }

    @Override
    public IPage<ProductVO> listProducts(ProductQueryDTO queryDTO) {
        log.info("查询商品列表: {}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            queryWrapper.like(Product::getName, queryDTO.getKeyword())
                    .or()
                    .like(Product::getDescription, queryDTO.getKeyword())
                    .or()
                    .like(Product::getKeywords, queryDTO.getKeyword());
        }
        
        // 分类过滤
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }
        
        // 排序
        if (queryDTO.getOrderBy() != null) {
            switch (queryDTO.getOrderBy()) {
                case "price_asc":
                    queryWrapper.orderByAsc(Product::getPrice);
                    break;
                case "price_desc":
                    queryWrapper.orderByDesc(Product::getPrice);
                    break;
                case "sale_desc":
                    queryWrapper.orderByDesc(Product::getSale);
                    break;
                default:
                    queryWrapper.orderByDesc(Product::getCreateTime);
                    break;
            }
        } else {
            queryWrapper.orderByDesc(Product::getCreateTime);
        }
        
        // 分页查询
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Product> productPage = page(page, queryWrapper);
        
        // 转换为VO
        IPage<ProductVO> productVOPage = productPage.convert(product -> {
            ProductVO productVO = new ProductVO();
            BeanUtil.copyProperties(product, productVO);
            
            // 设置分类信息
            CategoryVO categoryVO = categoryService.getCategoryById(product.getCategoryId());
            productVO.setCategory(categoryVO);
            
            return productVO;
        });
        
        return productVOPage;
    }

    @Override
    public ProductVO getProductById(Long id) {
        log.info("获取商品详情: {}", id);
        
        // 尝试从缓存获取
        String cacheKey = REDIS_KEY_PRODUCT + id;
        ProductVO cachedProduct = (ProductVO) redisService.get(cacheKey);
        if (cachedProduct != null) {
            log.info("从缓存获取商品详情成功");
            return cachedProduct;
        }
        
        // 从数据库获取
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 转换为VO
        ProductVO productVO = new ProductVO();
        BeanUtil.copyProperties(product, productVO);
        
        // 设置分类信息
        CategoryVO categoryVO = categoryService.getCategoryById(product.getCategoryId());
        productVO.setCategory(categoryVO);

        // 存入缓存
        redisService.set(cacheKey, productVO, REDIS_EXPIRE_TIME);
        
        return productVO;
    }
    
    /**
     * 清除商品相关缓存
     * @param productId 商品ID
     */
    private void clearProductCache(Long productId) {
        redisService.del(REDIS_KEY_PRODUCT + productId);
    }
}
