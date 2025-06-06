# 产品管理模块架构设计文档

**文档版本:** v0.1  
**创建日期:** [2024-07-30 14:30:00 +08:00]  
**创建者:** AR (架构师)  
**审核者:** LD (首席开发工程师), PM (项目经理)  

## 1. 概述

本文档描述了黑马点评系统产品管理模块的架构设计。产品管理模块是商家端的核心功能之一，允许商家创建、查询、更新和删除产品信息，以及管理产品状态。

### 1.1 设计目标

- 提供完整的产品生命周期管理功能
- 确保商家只能管理自己店铺的产品
- 支持灵活的产品查询和过滤
- 保持与现有系统架构的一致性
- 遵循SOLID、KISS、YAGNI等设计原则

### 1.2 适用范围

本文档适用于产品管理模块的MVP（最小可行产品）版本，包括基本的CRUD操作和状态管理功能。

## 2. 系统架构

### 2.1 整体架构

产品管理模块遵循黑马点评系统的整体架构，采用经典的三层架构：

- **表现层（Controller）:** 处理HTTP请求，参数验证，调用服务层
- **业务层（Service）:** 实现业务逻辑，事务管理，授权验证
- **数据访问层（Mapper）:** 数据库操作，使用MyBatis-Plus框架

### 2.2 模块组件

![产品管理模块组件图](../architecture/images/product_management_components.png)

#### 2.2.1 核心组件

| 组件 | 类型 | 职责 |
|------|------|------|
| ProductController | 控制器 | 处理HTTP请求，参数验证，调用服务层 |
| IProductService | 接口 | 定义产品管理业务方法 |
| ProductServiceImpl | 实现类 | 实现产品管理业务逻辑 |
| ProductMapper | 接口 | 数据库操作接口，继承自BaseMapper |
| Product | 实体类 | 产品数据模型 |
| ProductCreateDTO | DTO | 产品创建数据传输对象 |
| ProductUpdateDTO | DTO | 产品更新数据传输对象 |
| ProductResponseDTO | DTO | 产品响应数据传输对象 |
| ProductQueryDTO | DTO | 产品查询数据传输对象 |

#### 2.2.2 辅助组件

| 组件 | 类型 | 职责 |
|------|------|------|
| MerchantHolder | 工具类 | 获取当前商家信息 |
| Result | 工具类 | 统一响应格式 |

### 2.3 数据模型

#### 2.3.1 Product实体

```java
@Data
@TableName("tb_product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private Long categoryId;
    private String title;
    private String description;
    private String images;
    private Long price;
    private Integer stock;
    private Integer sold;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### 2.3.2 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| shopId | Long | 店铺ID |
| categoryId | Long | 分类ID |
| title | String | 商品标题 |
| description | String | 商品描述 |
| images | String | 商品图片，多个图片用逗号分隔 |
| price | Long | 商品价格，单位为分 |
| stock | Integer | 库存 |
| sold | Integer | 销量 |
| status | Integer | 状态，0-下架，1-上架 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

## 3. 接口设计

### 3.1 API接口

#### 3.1.1 创建商品

- **URL:** `/api/product`
- **Method:** POST
- **请求参数:**
  ```json
  {
    "name": "商品名称",
    "shopId": 1,
    "description": "商品描述",
    "images": "图片1.jpg,图片2.jpg",
    "price": 1000,
    "stock": 100
  }
  ```
- **响应:**
  ```json
  {
    "success": true,
    "data": 1,
    "errorMsg": null
  }
  ```

#### 3.1.2 获取商品详情

- **URL:** `/api/product/{id}`
- **Method:** GET
- **响应:**
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "shopId": 1,
      "categoryId": 1,
      "title": "商品名称",
      "description": "商品描述",
      "images": "图片1.jpg,图片2.jpg",
      "price": 1000,
      "stock": 100,
      "sold": 0,
      "status": 1,
      "createTime": "2024-07-30T14:30:00",
      "updateTime": "2024-07-30T14:30:00"
    },
    "errorMsg": null
  }
  ```

#### 3.1.3 更新商品

- **URL:** `/api/product/{id}`
- **Method:** PUT
- **请求参数:**
  ```json
  {
    "title": "新商品名称",
    "description": "新商品描述",
    "images": "新图片1.jpg,新图片2.jpg",
    "price": 2000,
    "stock": 200
  }
  ```
- **响应:**
  ```json
  {
    "success": true,
    "data": null,
    "errorMsg": null
  }
  ```

#### 3.1.4 删除商品

- **URL:** `/api/product/{id}`
- **Method:** DELETE
- **响应:**
  ```json
  {
    "success": true,
    "data": null,
    "errorMsg": null
  }
  ```

#### 3.1.5 查询商品列表

- **URL:** `/api/product/list`
- **Method:** GET
- **请求参数:**
  ```
  shopId=1&name=商品&status=1&minPrice=1000&maxPrice=2000&page=1&size=10
  ```
- **响应:**
  ```json
  {
    "success": true,
    "data": {
      "records": [
        {
          "id": 1,
          "shopId": 1,
          "categoryId": 1,
          "title": "商品名称",
          "description": "商品描述",
          "images": "图片1.jpg,图片2.jpg",
          "price": 1000,
          "stock": 100,
          "sold": 0,
          "status": 1,
          "createTime": "2024-07-30T14:30:00",
          "updateTime": "2024-07-30T14:30:00"
        }
      ],
      "total": 1,
      "size": 10,
      "current": 1,
      "pages": 1
    },
    "errorMsg": null
  }
  ```

#### 3.1.6 更新商品状态

- **URL:** `/api/product/{id}/status`
- **Method:** PUT
- **请求参数:**
  ```json
  {
    "status": 1
  }
  ```
- **响应:**
  ```json
  {
    "success": true,
    "data": null,
    "errorMsg": null
  }
  ```

### 3.2 服务接口

    ```java
public interface IProductService extends IService<Product> {
    /**
     * 创建商品
     */
    Result createProduct(ProductCreateDTO createDTO);

    /**
     * 获取商品详情
     */
    Result getProductById(Long id);

    /**
     * 更新商品信息
     */
    Result updateProduct(ProductUpdateDTO updateDTO);

    /**
     * 删除商品
     */
    Result deleteProduct(Long id);

    /**
     * 获取商品列表
     */
    Result listProductsByShop(ProductQueryDTO queryDTO);

    /**
     * 更新商品状态
     */
    Result updateProductStatus(Long id, Integer status);
}
```

## 4. 业务流程

### 4.1 商品创建流程

```
客户端 -> ProductController.createProduct -> IProductService.createProduct -> 
验证输入 -> 验证商家权限 -> 创建Product对象 -> 保存到数据库 -> 返回结果
```

### 4.2 商品更新流程

```
客户端 -> ProductController.updateProduct -> IProductService.updateProduct -> 
验证输入 -> 获取商品 -> 验证商品存在 -> 验证商家权限 -> 更新商品信息 -> 保存到数据库 -> 返回结果
```

### 4.3 商品查询流程

```
客户端 -> ProductController.listProducts -> IProductService.listProductsByShop -> 
构建查询条件 -> 执行分页查询 -> 转换为DTO -> 返回结果
```

## 5. 安全设计

### 5.1 授权机制

产品管理模块实现了基于商家身份的授权机制，确保商家只能管理自己店铺的产品。

- 创建商品时，验证shopId是否属于当前商家
- 更新/删除商品时，验证商品是否属于当前商家的店铺
- 查询商品时，只返回当前商家店铺的商品

### 5.2 数据验证

- 所有输入参数都经过验证，确保数据的完整性和有效性
- 特殊字段（如价格、库存）进行额外的验证
- 使用MyBatis-Plus的参数绑定机制，防止SQL注入

## 6. 性能考虑

### 6.1 查询优化

- 使用MyBatis-Plus的分页功能，避免一次性加载大量数据
- 合理使用索引，提高查询效率
- 使用条件构造器，只查询必要的数据

### 6.2 缓存策略

MVP版本暂不实现缓存，后续版本可考虑：

- 使用Redis缓存热门商品数据
- 实现缓存更新策略，确保数据一致性

## 7. 扩展性设计

### 7.1 未来功能扩展

产品管理模块设计考虑了以下扩展点：

- 批量操作：批量创建、更新、删除商品
- 产品分类管理：更灵活的分类方式
- 产品图片管理：多图片上传和预览
- 产品评论和评分：用户反馈功能
- 产品库存和销售统计：数据分析功能

### 7.2 接口扩展

服务接口和控制器设计遵循开闭原则，可以通过以下方式扩展：

- 添加新的服务方法和API端点
- 扩展DTO对象，添加新的字段
- 实现新的查询条件和过滤选项

## 8. 设计原则应用

### 8.1 SOLID原则

- **单一职责原则(SRP):** 每个类和方法都有明确的单一职责
- **开闭原则(OCP):** 通过接口和实现分离，便于扩展
- **里氏替换原则(LSP):** 继承关系合理，子类可替换父类
- **接口隔离原则(ISP):** 接口设计精简，客户端不依赖不使用的方法
- **依赖倒置原则(DIP):** 高层模块通过接口依赖低层模块

### 8.2 其他设计原则

- **KISS原则:** 设计简洁明了，避免不必要的复杂性
- **YAGNI原则:** 只实现当前需要的功能，避免过度设计
- **DRY原则:** 避免代码重复，通过抽象和封装复用逻辑
- **高内聚低耦合:** 模块内部元素紧密相关，模块间依赖最小化

## 9. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 商家权限控制不当 | 商家可能访问或修改其他商家的产品 | 实现严格的授权检查，在服务层验证商家身份 |
| 数据一致性问题 | 并发操作可能导致数据不一致 | 使用事务管理，确保数据操作的原子性 |
| 性能瓶颈 | 大量商品数据可能导致查询性能下降 | 实现分页查询，后续考虑添加缓存机制 |
| 安全漏洞 | SQL注入、XSS等安全风险 | 使用参数化查询，输入验证，遵循安全编码实践 |

## 10. 参考资料

- MyBatis-Plus官方文档: https://baomidou.com/
- Spring Boot官方文档: https://spring.io/projects/spring-boot
- RESTful API设计指南: https://restfulapi.net/

## 更新日志

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|---------|-------|
| v0.1 | [2024-07-30 14:30:00 +08:00] | 初始版本 | AR |
| v0.1 | [2024-07-30 15:00:00 +08:00] | 审核通过 | LD, PM | 