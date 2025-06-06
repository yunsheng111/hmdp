package com.hmdp.controller;

import com.hmdp.common.Result;
import com.hmdp.dto.ProductCreateDTO;
import com.hmdp.dto.ProductQueryDTO;
import com.hmdp.dto.ProductUpdateDTO;
import com.hmdp.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 商品管理控制器
 * 
 * {{CHENGQI:
 * Action: Added
 * Timestamp: [2024-07-30 17:00:00 +08:00]
 * Reason: 创建ProductController实现商品管理API，遵循P3-LD-004任务
 * Principle_Applied: SOLID (Single Responsibility) - 控制器只负责处理商品相关的请求
 * Principle_Applied: KISS - 保持API简单明了，每个方法职责单一
 * }}
 */
@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private IProductService productService;

    /**
     * 创建商品
     * @param createDTO 商品创建数据
     * @return 创建结果
     */
    @PostMapping
    public Result createProduct(@RequestBody ProductCreateDTO createDTO) {
        log.info("创建商品: {}", createDTO);
        return productService.createProduct(createDTO);
    }

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Result getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    /**
     * 更新商品信息
     * @param id 商品ID
     * @param updateDTO 商品更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result updateProduct(@PathVariable("id") Long id, @RequestBody ProductUpdateDTO updateDTO) {
        // 确保ID一致
        updateDTO.setId(id);
        return productService.updateProduct(updateDTO);
    }

    /**
     * 删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteProduct(@PathVariable("id") Long id) {
        return productService.deleteProduct(id);
    }

    /**
     * 获取商品列表
     * @param queryDTO 查询参数
     * @return 商品列表
     */
    @GetMapping("/list")
    public Result listProducts(ProductQueryDTO queryDTO) {
        return productService.listProductsByShop(queryDTO);
    }

    /**
     * 更新商品状态
     * @param id 商品ID
     * @param statusMap 状态数据，格式：{"status": 0} 或 {"status": 1}
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public Result updateProductStatus(@PathVariable("id") Long id, @RequestBody Map<String, Integer> statusMap) {
        Integer status = statusMap.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail("无效的状态值");
        }
        return productService.updateProductStatus(id, status);
    }

    // ========== 用户端API ==========

    /**
     * 获取商铺商品分类（用户端）
     * @param shopId 商铺ID
     * @return 商品分类列表
     */
    @GetMapping("/categories/{shopId}")
    public Result getProductCategories(@PathVariable("shopId") Long shopId) {
        return productService.getProductCategoriesByShop(shopId);
    }

    /**
     * 获取商铺商品列表（用户端，只返回上架商品）
     * @param shopId 商铺ID
     * @param categoryId 分类ID（可选）
     * @param current 页码
     * @param size 每页大小
     * @return 商品列表
     */
    @GetMapping("/shop/{shopId}")
    public Result getShopProducts(
            @PathVariable("shopId") Long shopId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return productService.getShopProducts(shopId, categoryId, current, size);
    }
}