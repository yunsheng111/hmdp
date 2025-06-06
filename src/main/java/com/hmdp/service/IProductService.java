package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.common.Result;
import com.hmdp.dto.ProductCreateDTO;
import com.hmdp.dto.ProductQueryDTO;
import com.hmdp.dto.ProductResponseDTO;
import com.hmdp.dto.ProductUpdateDTO;
import com.hmdp.dto.ScrollResult;
import com.hmdp.entity.Product;

/**
 * 商品服务接口
 * 
 * {{CHENGQI:
 * Action: Added
 * Timestamp: [2024-07-30 16:30:00 +08:00]
 * Reason: 创建IProductService接口定义商品管理的业务逻辑契约，遵循P3-AR-002任务
 * Principle_Applied: SOLID (Interface Segregation) - 接口定义清晰明确，只包含商品管理相关的方法
 * Principle_Applied: SOLID (Single Responsibility) - 每个方法都有明确的单一职责
 * }}
 */
public interface IProductService extends IService<Product> {
    
    /**
     * 创建商品
     * @param createDTO 商品创建数据
     * @return 创建结果，包含商品信息
     */
    Result createProduct(ProductCreateDTO createDTO);
    
    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    Result getProductById(Long id);
    
    /**
     * 更新商品信息
     * @param updateDTO 商品更新数据
     * @return 更新结果，包含更新后的商品信息
     */
    Result updateProduct(ProductUpdateDTO updateDTO);
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    Result deleteProduct(Long id);
    
    /**
     * 获取当前商家的商品列表
     * @param queryDTO 查询参数
     * @return 商品列表
     */
    Result listProductsByShop(ProductQueryDTO queryDTO);
    
    /**
     * 更新商品状态（上架/下架）
     * @param id 商品ID
     * @param status 状态：0-下架，1-上架
     * @return 更新结果
     */
    Result updateProductStatus(Long id, Integer status);

    // ========== 用户端API ==========

    /**
     * 获取商铺商品分类
     * @param shopId 商铺ID
     * @return 商品分类列表
     */
    Result getProductCategoriesByShop(Long shopId);

    /**
     * 获取商铺商品列表（用户端）
     * @param shopId 商铺ID
     * @param categoryId 分类ID（可选）
     * @param current 页码
     * @param size 每页大小
     * @return 商品列表
     */
    Result getShopProducts(Long shopId, Long categoryId, Integer current, Integer size);
}