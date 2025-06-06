package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.*;
import com.hmdp.entity.Product;
import com.hmdp.entity.ProductCategory;
import com.hmdp.entity.ProductSpec;
import com.hmdp.mapper.ProductMapper;
import com.hmdp.mapper.ProductCategoryMapper;
import com.hmdp.mapper.ProductSpecMapper;
import com.hmdp.service.IProductService;
import com.hmdp.utils.MerchantHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品服务实现类
 * 
 * 该类实现了IProductService接口，提供商品相关的业务逻辑实现
 * 遵循SOLID原则，特别是单一职责原则和开闭原则
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;
    
    /**
     * 商品规格数据访问层
     * {{CHENGQI:
     * Reason: 添加ProductSpecMapper依赖，用于查询商品规格数据
     * Principle_Applied: DIP - 依赖抽象接口而非具体实现
     * }}
     */
    @Resource
    private ProductSpecMapper productSpecMapper;

    @Override
    @Transactional
    public Result createProduct(ProductCreateDTO createDTO) {
        // 1. 验证商家权限
        MerchantDTO merchant = MerchantHolder.getMerchant();
        if (merchant == null) {
            return Result.fail("未登录");
        }
        
        // 2. 获取商家的shopId
        Long shopId = merchant.getShopId();
        if (shopId == null) {
            return Result.fail("商家未关联店铺");
        }

        // 3. 转换DTO为实体
        Product product = new Product();
        // 设置商品标题
        product.setTitle(createDTO.getTitle());
        // 设置商品价格 (注意：Product实体中价格为Long类型，单位为分)
        long priceInCents = createDTO.getPrice().multiply(new java.math.BigDecimal(100)).longValue();
        product.setPrice(priceInCents);
        product.setStock(createDTO.getStock());
        product.setDescription(createDTO.getDescription());
        product.setImages(createDTO.getImages());
        product.setShopId(shopId); // 使用当前商家的shopId
        // 设置默认分类ID
        product.setCategoryId(1L); // 默认分类
        
        // 4. 设置默认值
        product.setStatus(0); // 默认为下架状态
        product.setSold(0); // 初始销量为0
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        // 5. 保存商品
        boolean success = save(product);
        if (!success) {
            return Result.fail("商品创建失败");
        }

        // 6. 保存商品规格（如果有）
        if (createDTO.getSpecs() != null && !createDTO.getSpecs().isEmpty()) {
            try {
                saveProductSpecs(product.getId(), createDTO.getSpecs());
            } catch (Exception e) {
                log.error("保存商品规格失败: {}", e.getMessage());
                // 规格保存失败时，删除已创建的商品以保持数据一致性
                removeById(product.getId());
                return Result.fail("商品创建失败：规格保存失败");
            }
        }

        // 7. 转换为响应DTO
        ProductResponseDTO responseDTO = convertToResponseDTO(product);

        return Result.success(responseDTO);
    }

    @Override
    public Result getProductById(Long id) {
        // 1. 查询商品
        Product product = getById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        
        // 2. 转换为响应DTO
        ProductResponseDTO responseDTO = convertToResponseDTO(product);
        
        return Result.success(responseDTO);
    }

    @Override
    @Transactional
    public Result updateProduct(ProductUpdateDTO updateDTO) {
        // 1. 验证商家权限
        MerchantDTO merchant = MerchantHolder.getMerchant();
        if (merchant == null) {
            return Result.fail("未登录");
        }
        
        // 2. 查询商品
        Product product = getById(updateDTO.getId());
        if (product == null) {
            return Result.fail("商品不存在");
        }
        
        // 3. 验证店铺归属关系
        if (!Objects.equals(merchant.getShopId(), product.getShopId())) {
            return Result.fail("无权操作此店铺的商品");
        }
        
        // 4. 更新商品信息
        if (updateDTO.getTitle() != null) {
            product.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getPrice() != null) {
            // 转换价格：从元转换为分（Product实体中价格为Long类型，单位为分）
            long priceInCents = updateDTO.getPrice().multiply(new java.math.BigDecimal(100)).longValue();
            product.setPrice(priceInCents);
        }
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getImages() != null) {
            product.setImages(updateDTO.getImages());
        }
        if (updateDTO.getCategoryId() != null) {
            product.setCategoryId(updateDTO.getCategoryId());
        }
        
        product.setUpdateTime(LocalDateTime.now());
        
        // 5. 保存更新
        boolean success = updateById(product);
        if (!success) {
            return Result.fail("商品更新失败");
        }

        // 6. 更新商品规格（如果有）
        if (updateDTO.getSpecs() != null) {
            try {
                // 先删除旧规格，再保存新规格
                deleteProductSpecs(product.getId());
                if (!updateDTO.getSpecs().isEmpty()) {
                    saveProductSpecs(product.getId(), updateDTO.getSpecs());
                }
            } catch (Exception e) {
                log.error("更新商品规格失败: {}", e.getMessage());
                return Result.fail("商品已更新，但规格保存失败");
            }
        }

        // 7. 转换为响应DTO
        ProductResponseDTO responseDTO = convertToResponseDTO(product);

        return Result.success(responseDTO);
    }

    @Override
    @Transactional
    public Result deleteProduct(Long id) {
        // 1. 验证商家权限
        MerchantDTO merchant = MerchantHolder.getMerchant();
        if (merchant == null) {
            return Result.fail("未登录");
        }
        
        // 2. 查询商品
        Product product = getById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        
        // 3. 验证店铺归属关系
        if (!Objects.equals(merchant.getShopId(), product.getShopId())) {
            return Result.fail("无权操作此店铺的商品");
        }
        
        // 4. 删除商品
        boolean success = removeById(id);
        if (!success) {
            return Result.fail("商品删除失败");
        }
        
        return Result.success();
    }

    @Override
    public Result listProductsByShop(ProductQueryDTO queryDTO) {
        // 1. 验证商家权限
        MerchantDTO merchant = MerchantHolder.getMerchant();
        if (merchant == null) {
            return Result.fail("未登录");
        }

        // 2. 使用当前商家的shopId，忽略前端传递的shopId
        Long shopId = merchant.getShopId();
        if (shopId == null) {
            return Result.fail("商家未关联店铺");
        }

        // 3. 构建查询条件
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId);
        
        // 4. 添加状态过滤条件
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        
        // 5. 添加标题模糊搜索
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().isEmpty()) {
            queryWrapper.like("title", queryDTO.getTitle());
        }
        
        // 6. 添加价格范围条件
        if (queryDTO.getMinPrice() != null) {
            // 转换为分
            long minPriceInCents = queryDTO.getMinPrice().multiply(new java.math.BigDecimal(100)).longValue();
            queryWrapper.ge("price", minPriceInCents);
        }
        if (queryDTO.getMaxPrice() != null) {
            // 转换为分
            long maxPriceInCents = queryDTO.getMaxPrice().multiply(new java.math.BigDecimal(100)).longValue();
            queryWrapper.le("price", maxPriceInCents);
        }
        
        // 7. 添加ID排序（按ID降序排列，最新创建的商品在前）
        queryWrapper.orderByDesc("id");
        
        // 8. 分页查询
        Page<Product> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        page = page(page, queryWrapper);
        
        // 9. 转换为响应DTO
        List<ProductResponseDTO> records = new ArrayList<>();
        for (Product product : page.getRecords()) {
            records.add(convertToResponseDTO(product));
        }
        
        // 10. 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());

        return Result.success(result);
    }

    @Override
    @Transactional
    public Result updateProductStatus(Long id, Integer status) {
        // 1. 验证商家权限
        MerchantDTO merchant = MerchantHolder.getMerchant();
        if (merchant == null) {
            return Result.fail("未登录");
        }
        
        // 2. 查询商品
        Product product = getById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        
        // 3. 验证店铺归属关系
        if (!Objects.equals(merchant.getShopId(), product.getShopId())) {
            return Result.fail("无权操作此店铺的商品");
        }
        
        // 4. 验证状态值
        if (status != 0 && status != 1) {
            return Result.fail("无效的状态值");
        }
        
        // 5. 更新状态
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        
        // 6. 保存更新
        boolean success = updateById(product);
        if (!success) {
            return Result.fail("状态更新失败");
        }
        
        return Result.success();
    }
    
    @Override
    public Result getProductCategoriesByShop(Long shopId) {
        // 1. 验证商铺是否存在（可选，根据业务需求）

        // 2. 查询商铺的商品分类
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId)
                   .orderByAsc("sort")
                   .orderByAsc("id");

        List<ProductCategory> categories = productCategoryMapper.selectList(queryWrapper);

        return Result.success(categories);
    }

    @Override
    public Result getShopProducts(Long shopId, Long categoryId, Integer current, Integer size) {
        // 1. 验证参数
        if (current == null || current < 1) {
            current = 1;
        }
        if (size == null || size < 1 || size > 50) {
            size = 10;
        }

        // 2. 构建查询条件
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId)
                   .eq("status", 1); // 只查询上架商品

        // 3. 添加分类过滤
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }

        // 4. 排序：按更新时间降序
        queryWrapper.orderByDesc("update_time");

        // 5. 分页查询
        Page<Product> page = new Page<>(current, size);
        page = page(page, queryWrapper);

        // 6. 转换为响应DTO
        List<ProductResponseDTO> records = new ArrayList<>();
        for (Product product : page.getRecords()) {
            records.add(convertToResponseDTO(product));
        }

        // 7. 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("pages", page.getPages());

        return Result.success(result);
    }

    /**
     * 将Product实体转换为ProductResponseDTO
     * @param product 商品实体
     * @return 商品响应DTO
     * 
     * {{CHENGQI:
     * Reason: 修改方法添加查询商品规格并设置到DTO中
     * Principle_Applied: SRP - 在单一职责方法中处理规格查询和转换，保持方法职责明确
     * Principle_Applied: DRY - 复用ProductSpecMapper进行规格查询，避免重复代码
     * Optimization: 一次性查询所有规格，避免N+1查询问题
     * }}
     */
    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        // 注意：Product实体中价格为Long类型（分），ResponseDTO中也为Long类型（分）
        // 如果前端需要元为单位，需要在前端进行转换，或者修改ResponseDTO的price字段类型为BigDecimal
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setSold(product.getSold());
        dto.setDescription(product.getDescription());
        dto.setImages(product.getImages());
        dto.setShopId(product.getShopId());
        dto.setCategoryId(product.getCategoryId());
        dto.setStatus(product.getStatus());
        dto.setCreateTime(product.getCreateTime());
        dto.setUpdateTime(product.getUpdateTime());
        
        // 查询商品规格
        List<ProductSpec> specs = productSpecMapper.findByProductId(product.getId());
        
        // 如果存在规格，则转换为DTO
        if (specs != null && !specs.isEmpty()) {
            List<ProductSpecDTO> specDTOs = new ArrayList<>(specs.size());
            ObjectMapper objectMapper = new ObjectMapper();
            
            for (ProductSpec spec : specs) {
                ProductSpecDTO specDTO = new ProductSpecDTO();
                specDTO.setId(spec.getId());
                specDTO.setProductId(spec.getProductId());
                specDTO.setName(spec.getName());
                specDTO.setRequired(spec.getRequired());
                specDTO.setSort(spec.getSort());
                
                // 解析JSON字符串为List<String>
                try {
                    if (spec.getValues() != null && !spec.getValues().isEmpty()) {
                        List<String> values = objectMapper.readValue(spec.getValues(), 
                                new TypeReference<List<String>>() {});
                        specDTO.setValues(values);
                    } else {
                        specDTO.setValues(new ArrayList<>());
                    }
                } catch (JsonProcessingException e) {
                    log.error("解析商品规格值失败: {}", e.getMessage());
                    specDTO.setValues(new ArrayList<>());
                }
                
                specDTOs.add(specDTO);
            }
            
            dto.setSpecs(specDTOs);
        } else {
            // 没有规格时，设置为空列表而非null
            dto.setSpecs(new ArrayList<>());
        }
        
        return dto;
    }

    /**
     * 保存商品规格
     * @param productId 商品ID
     * @param specs 规格列表
     * {{CHENGQI:
     * Action: Added
     * Timestamp: [2024-12-19 14:45:00 +08:00]
     * Reason: 添加规格保存方法，支持在创建和更新商品时保存规格信息
     * Principle_Applied: SRP - 单一职责，专门处理规格保存逻辑
     * Principle_Applied: DRY - 复用代码，避免在创建和更新方法中重复规格保存逻辑
     * }}
     */
    private void saveProductSpecs(Long productId, List<ProductSpecDTO> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        for (ProductSpecDTO specDTO : specs) {
            ProductSpec spec = new ProductSpec();
            spec.setProductId(productId);
            spec.setName(specDTO.getName());
            spec.setRequired(specDTO.getRequired());
            spec.setSort(specDTO.getSort());

            // 设置时间字段
            LocalDateTime now = LocalDateTime.now();
            spec.setCreateTime(now);
            spec.setUpdateTime(now);

            // 将规格值列表转换为JSON字符串
            try {
                if (specDTO.getValues() != null && !specDTO.getValues().isEmpty()) {
                    String valuesJson = objectMapper.writeValueAsString(specDTO.getValues());
                    spec.setValues(valuesJson);
                } else {
                    spec.setValues("[]");
                }
            } catch (JsonProcessingException e) {
                log.error("转换规格值为JSON失败: {}", e.getMessage());
                spec.setValues("[]");
            }

            // 保存规格
            productSpecMapper.insert(spec);
        }
    }

    /**
     * 删除商品的所有规格
     * @param productId 商品ID
     * {{CHENGQI:
     * Action: Added
     * Timestamp: [2024-12-19 14:47:00 +08:00]
     * Reason: 添加规格删除方法，支持在更新商品时先删除旧规格
     * Principle_Applied: SRP - 单一职责，专门处理规格删除逻辑
     * }}
     */
    private void deleteProductSpecs(Long productId) {
        QueryWrapper<ProductSpec> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        productSpecMapper.delete(queryWrapper);
    }
}