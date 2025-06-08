# 管理员商品审核API设计文档

**[2024-07-30 17:30:00 +08:00]**

## 1. 需求背景

黑马点评平台需要实现管理员对商家上传的商品进行审核的功能，以确保平台商品内容的合规性和质量。管理员可以查看待审核的商品列表，并对每个商品进行审核通过或拒绝的操作。

## 2. API概览

本文档定义了管理员商品审核系统所需的后端API接口规范，主要包括：
1. 获取待审核商品列表
2. 审核商品（通过/拒绝）
3. 查看商品审核历史

## 3. 数据模型

### 3.1 商品审核状态枚举
```java
public enum ProductAuditStatus {
    PENDING_APPROVAL("待审核", 2),
    APPROVED("已通过", 1),
    REJECTED("已拒绝", 0);
    
    private final String description;
    private final int value;
    
    // 构造函数、getter等...
}
```

### 3.2 商品审核记录表结构
```sql
CREATE TABLE `tb_product_audit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `admin_id` bigint(20) NOT NULL COMMENT '审核管理员ID',
  `admin_name` varchar(64) NOT NULL COMMENT '审核管理员名称',
  `status` tinyint(1) NOT NULL COMMENT '审核状态：0-拒绝，1-通过，2-待审核',
  `reason` varchar(255) DEFAULT NULL COMMENT '审核意见（拒绝原因）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品审核记录表';
```

### 3.3 商品表结构变更
在现有的商品表（`tb_product`）中新增字段：
```sql
ALTER TABLE `tb_product` 
ADD COLUMN `audit_status` tinyint(1) NOT NULL DEFAULT '2' COMMENT '审核状态：0-拒绝，1-通过，2-待审核',
ADD COLUMN `audit_time` datetime DEFAULT NULL COMMENT '最近审核时间',
ADD COLUMN `audit_reason` varchar(255) DEFAULT NULL COMMENT '审核意见（拒绝原因）',
ADD COLUMN `submit_time` datetime DEFAULT NULL COMMENT '提交审核时间';
```

## 4. API详细设计

### 4.1 获取待审核商品列表

**接口说明**：管理员获取需要审核的商品列表，支持分页和多条件筛选

**请求方法**：GET

**请求路径**：`/admin/content/products/pending`

**请求参数**：

| 参数名 | 类型 | 是否必须 | 说明 |
| ----- | ---- | ------- | ---- |
| page | Integer | 否 | 页码，默认为1 |
| size | Integer | 否 | 每页记录数，默认为10 |
| merchantId | Long | 否 | 商户ID筛选 |
| shopId | Long | 否 | 店铺ID筛选 |
| statusFilter | Integer | 否 | 审核状态筛选：0-拒绝，1-通过，2-待审核 |

**响应参数**：

```json
{
  "success": true,
  "data": {
    "total": 100,
    "list": [
      {
        "productId": 10001,
        "productName": "示例商品1",
        "shopId": 2001,
        "shopName": "示例店铺1",
        "merchantId": 3001,
        "merchantName": "示例商户1",
        "price": 99.99,
        "categoryId": 5001,
        "categoryName": "示例分类",
        "status": "PENDING_APPROVAL",
        "submitTime": "2024-07-30 15:30:00",
        "description": "示例商品描述",
        "images": ["image1.jpg", "image2.jpg"]
      },
      // 更多商品记录...
    ]
  },
  "msg": "获取商品列表成功"
}
```

### 4.2 审核商品

**接口说明**：管理员对商品进行审核操作（通过或拒绝）

**请求方法**：PUT

**请求路径**：`/admin/content/products/{productId}/audit`

**路径参数**：

| 参数名 | 类型 | 是否必须 | 说明 |
| ----- | ---- | ------- | ---- |
| productId | Long | 是 | 商品ID |

**请求体**：

```json
{
  "status": "APPROVED", // 或 "REJECTED"
  "reason": "商品内容不符合平台规范" // 拒绝时必填，通过时可选
}
```

**响应参数**：

```json
{
  "success": true,
  "data": {
    "productId": 10001,
    "productName": "示例商品1",
    "auditStatus": "APPROVED", // 或 "REJECTED"
    "auditTime": "2024-07-30 17:45:00"
  },
  "msg": "商品审核成功"
}
```

### 4.3 获取商品审核历史记录

**接口说明**：查询指定商品的审核历史记录

**请求方法**：GET

**请求路径**：`/admin/content/products/{productId}/audit-history`

**路径参数**：

| 参数名 | 类型 | 是否必须 | 说明 |
| ----- | ---- | ------- | ---- |
| productId | Long | 是 | 商品ID |

**响应参数**：

```json
{
  "success": true,
  "data": [
    {
      "id": 1001,
      "productId": 10001,
      "adminId": 5001,
      "adminName": "管理员A",
      "status": "REJECTED",
      "reason": "图片不清晰，请重新上传",
      "createTime": "2024-07-29 14:30:00"
    },
    {
      "id": 1002,
      "productId": 10001,
      "adminId": 5002,
      "adminName": "管理员B",
      "status": "APPROVED",
      "reason": "",
      "createTime": "2024-07-30 17:45:00"
    }
  ],
  "msg": "获取审核历史成功"
}
```

### 4.4 获取商品审核统计信息

**接口说明**：获取商品审核的统计数据，用于管理员仪表盘

**请求方法**：GET

**请求路径**：`/admin/content/products/audit-statistics`

**请求参数**：

| 参数名 | 类型 | 是否必须 | 说明 |
| ----- | ---- | ------- | ---- |
| startDate | String | 否 | 统计开始日期，格式为yyyy-MM-dd |
| endDate | String | 否 | 统计结束日期，格式为yyyy-MM-dd |

**响应参数**：

```json
{
  "success": true,
  "data": {
    "totalPending": 56,
    "totalApproved": 245,
    "totalRejected": 23,
    "dailyStatistics": [
      {
        "date": "2024-07-28",
        "pending": 15,
        "approved": 35,
        "rejected": 5
      },
      {
        "date": "2024-07-29",
        "pending": 20,
        "approved": 40,
        "rejected": 8
      },
      {
        "date": "2024-07-30",
        "pending": 21,
        "approved": 45,
        "rejected": 10
      }
    ]
  },
  "msg": "获取审核统计成功"
}
```

## 5. 实现建议

### 5.1 新增AdminProductAuditController

建议创建专门的管理员商品审核控制器，以区分普通商品管理API：

```java
@RestController
@RequestMapping("/admin/content/products")
public class AdminProductAuditController {

    @Autowired
    private IProductAuditService productAuditService;
    
    @GetMapping("/pending")
    public Result getPendingProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Integer statusFilter) {
        // 实现获取待审核商品列表的逻辑
    }
    
    @PutMapping("/{productId}/audit")
    public Result auditProduct(
            @PathVariable Long productId,
            @RequestBody ProductAuditDTO auditDTO) {
        // 实现商品审核逻辑
    }
    
    @GetMapping("/{productId}/audit-history")
    public Result getProductAuditHistory(@PathVariable Long productId) {
        // 实现获取商品审核历史的逻辑
    }
    
    @GetMapping("/audit-statistics")
    public Result getAuditStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // 实现获取审核统计数据的逻辑
    }
}
```

### 5.2 创建IProductAuditService接口

```java
public interface IProductAuditService {
    
    /**
     * 获取待审核商品列表
     * @param page 页码
     * @param size 每页大小
     * @param merchantId 商户ID（可选）
     * @param shopId 店铺ID（可选）
     * @param status 审核状态（可选）
     * @return 商品列表
     */
    Result getPendingProducts(Integer page, Integer size, Long merchantId, Long shopId, Integer status);
    
    /**
     * 审核商品
     * @param productId 商品ID
     * @param auditDTO 审核数据
     * @return 审核结果
     */
    Result auditProduct(Long productId, ProductAuditDTO auditDTO);
    
    /**
     * 获取商品审核历史
     * @param productId 商品ID
     * @return 审核历史记录
     */
    Result getProductAuditHistory(Long productId);
    
    /**
     * 获取商品审核统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Result getAuditStatistics(String startDate, String endDate);
}
```

### 5.3 定义数据传输对象（DTO）

```java
public class ProductAuditDTO {
    private String status; // "APPROVED" 或 "REJECTED"
    private String reason; // 审核意见或拒绝原因
    
    // getter和setter方法
}
```

## 6. 安全考虑

1. 确保只有管理员角色用户可以访问这些API
2. 实现审核操作的日志记录，记录谁在什么时间审核了哪个商品及结果
3. 对敏感操作（如批量审核）添加二次确认机制
4. 对审核意见内容进行过滤，防止XSS攻击

## 7. 数据库表关系

- `tb_product`: 存储商品基本信息，包括审核状态
- `tb_product_audit`: 存储商品审核历史记录，与`tb_product`表通过`product_id`关联
- `tb_admin_user`: 管理员用户表，与`tb_product_audit`表通过`admin_id`关联

## 8. 后续优化建议

1. 实现商品审核的批量操作功能
2. 增加审核时间统计，监控审核效率
3. 增加智能审核推荐系统，基于历史审核结果预判商品是否合规
4. 为重复提交的相似商品提供快速审核建议