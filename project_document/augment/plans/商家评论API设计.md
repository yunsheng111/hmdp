# 商家评论API设计方案

**任务名称：** 商家评论API设计  
**创建时间：** 2024-12-23  
**设计阶段：** 后端设计  

## 一、设计概述

### 1.1 设计目标
- 为商家提供完整的评论管理功能
- 与现有用户端评论功能分离，职责清晰
- 利用现有的商家认证体系和数据模型
- 遵循RESTful API设计规范

### 1.2 设计原则
- **单一职责：** 专门处理商家端评论管理
- **数据权限：** 商家只能管理自己店铺的评论
- **安全性：** 所有接口需要商家登录认证
- **一致性：** 与现有商家端API保持风格一致

### 1.3 技术架构
- **控制器：** `MerchantCommentController`
- **路径前缀：** `/merchant/comment`
- **权限控制：** 利用现有`MerchantLoginInterceptor`
- **数据模型：** 基于现有`ShopComment`实体

## 二、API接口详细设计

### 2.1 评论列表查询

**接口地址：** `GET /merchant/comment/list`  
**功能描述：** 获取当前商家店铺的评论列表，支持分页、排序、筛选

**请求参数：**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页大小 |
| rating | Integer | 否 | - | 按评分筛选(1-5) |
| status | Integer | 否 | - | 按状态筛选(0-正常,1-用户隐藏,2-管理员隐藏) |
| hasReply | Boolean | 否 | - | 是否已回复筛选 |
| sortBy | String | 否 | time | 排序字段("time","rating") |
| order | String | 否 | desc | 排序方向("asc","desc") |

**响应格式：**
```json
{
  "success": true,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "userNickname": "用户昵称",
        "userIcon": "头像URL",
        "orderId": 456,
        "rating": 5,
        "content": "评论内容",
        "reply": "商家回复内容",
        "status": 0,
        "createTime": "2024-01-01T10:00:00",
        "replyTime": "2024-01-01T11:00:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

### 2.2 商家回复评论

**接口地址：** `POST /merchant/comment/{commentId}/reply`  
**功能描述：** 商家对指定评论进行回复

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

**请求体：**
```json
{
  "content": "感谢您的评价，我们会继续努力提供更好的服务！"
}
```

**响应格式：**
```json
{
  "success": true,
  "data": {
    "commentId": 1,
    "reply": "感谢您的评价，我们会继续努力提供更好的服务！",
    "replyTime": "2024-01-01T11:00:00"
  }
}
```

### 2.3 修改商家回复

**接口地址：** `PUT /merchant/comment/{commentId}/reply`  
**功能描述：** 修改已有的商家回复

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

**请求体：**
```json
{
  "content": "修改后的回复内容"
}
```

**响应格式：**
```json
{
  "success": true,
  "data": {
    "commentId": 1,
    "reply": "修改后的回复内容",
    "replyTime": "2024-01-01T12:00:00"
  }
}
```

### 2.4 删除商家回复

**接口地址：** `DELETE /merchant/comment/{commentId}/reply`  
**功能描述：** 删除商家回复

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

**响应格式：**
```json
{
  "success": true,
  "data": null
}
```

### 2.5 评论统计数据

**接口地址：** `GET /merchant/comment/statistics`  
**功能描述：** 获取当前商家店铺的评论统计信息

**响应格式：**
```json
{
  "success": true,
  "data": {
    "totalComments": 150,
    "averageRating": 4.2,
    "ratingDistribution": {
      "5": 80,
      "4": 40,
      "3": 20,
      "2": 8,
      "1": 2
    },
    "pendingReplyCount": 5,
    "replyRate": 85.5
  }
}
```

### 2.6 举报不当评论

**接口地址：** `POST /merchant/comment/{commentId}/report`  
**功能描述：** 商家举报不当评论

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

**请求体：**
```json
{
  "reason": "评论内容不实，恶意差评"
}
```

**响应格式：**
```json
{
  "success": true,
  "data": {
    "reportId": 123,
    "status": "已提交举报，等待管理员处理"
  }
}
```

## 三、数据传输对象设计

### 3.1 MerchantCommentListDTO
```java
public class MerchantCommentListDTO {
    private Long id;                    // 评论ID
    private Long userId;                // 用户ID
    private String userNickname;        // 用户昵称
    private String userIcon;            // 用户头像
    private Long orderId;               // 订单ID
    private Integer rating;             // 评分(1-5)
    private String content;             // 评论内容
    private String reply;               // 商家回复
    private Integer status;             // 状态
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime replyTime;    // 回复时间
}
```

### 3.2 MerchantCommentReplyDTO
```java
public class MerchantCommentReplyDTO {
    @NotBlank(message = "回复内容不能为空")
    @Length(max = 500, message = "回复内容不能超过500字")
    private String content;             // 回复内容
}
```

### 3.3 MerchantCommentStatisticsDTO
```java
public class MerchantCommentStatisticsDTO {
    private Long totalComments;                     // 总评论数
    private Double averageRating;                   // 平均评分
    private Map<Integer, Long> ratingDistribution;  // 评分分布
    private Long pendingReplyCount;                 // 待回复数量
    private Double replyRate;                       // 回复率
}
```

### 3.4 MerchantCommentReportDTO
```java
public class MerchantCommentReportDTO {
    @NotBlank(message = "举报原因不能为空")
    @Length(max = 200, message = "举报原因不能超过200字")
    private String reason;              // 举报原因
}
```

### 3.5 MerchantCommentQueryDTO
```java
public class MerchantCommentQueryDTO {
    private Integer current = 1;        // 页码
    private Integer size = 10;          // 每页大小
    private Integer rating;             // 评分筛选
    private Integer status;             // 状态筛选
    private Boolean hasReply;           // 是否已回复
    private String sortBy = "time";     // 排序字段
    private String order = "desc";      // 排序方向
}
```

## 四、权限控制方案

### 4.1 认证控制
- **拦截器：** 利用现有的`MerchantLoginInterceptor`
- **拦截路径：** `/merchant/comment/**`
- **认证方式：** 通过`merchant-token`请求头验证商家身份

### 4.2 数据权限控制
```java
// 在Service层进行数据权限校验
Long currentShopId = MerchantHolder.getMerchant().getShopId();

// 验证评论是否属于当前商家的店铺
ShopComment comment = shopCommentMapper.selectById(commentId);
if (comment == null || !comment.getShopId().equals(currentShopId)) {
    throw new CommentException("评论不存在或无权限操作");
}
```

### 4.3 权限校验流程
1. **请求拦截：** `MerchantLoginInterceptor`验证商家登录状态
2. **身份获取：** 通过`MerchantHolder.getMerchant()`获取当前商家信息
3. **数据权限：** 在Service层验证操作对象是否属于当前商家
4. **操作执行：** 权限验证通过后执行具体业务逻辑

## 五、错误处理规范

### 5.1 错误响应格式
根据用户偏好，不使用错误码，直接使用错误信息字符串：

```json
{
  "success": false,
  "errorMsg": "具体的错误信息"
}
```

### 5.2 常见错误信息
| 错误场景 | 错误信息 |
|----------|----------|
| 评论不存在或无权限 | "评论不存在或无权限操作" |
| 回复内容为空 | "回复内容不能为空" |
| 回复内容过长 | "回复内容不能超过500字" |
| 重复回复 | "该评论已有回复，请使用修改接口" |
| 删除不存在的回复 | "该评论尚未回复，无法删除" |
| 举报原因为空 | "举报原因不能为空" |
| 举报原因过长 | "举报原因不能超过200字" |
| 参数验证失败 | "参数验证失败：{具体字段错误}" |

## 六、实施计划

### 6.1 当前阶段：后端设计 ✅
- [x] 完成API接口设计规范
- [x] 定义数据传输对象结构  
- [x] 确定权限控制方案
- [x] 制定错误处理规范
- [x] 编写设计文档

### 6.2 下一阶段：后端实现
1. **创建控制器层**
   - 创建`MerchantCommentController`
   - 实现所有API接口方法
   - 添加参数验证和异常处理

2. **创建服务层**
   - 创建`IMerchantCommentService`接口
   - 实现`MerchantCommentServiceImpl`
   - 实现业务逻辑和权限校验

3. **创建DTO类**
   - `MerchantCommentListDTO`
   - `MerchantCommentReplyDTO`
   - `MerchantCommentStatisticsDTO`
   - `MerchantCommentReportDTO`
   - `MerchantCommentQueryDTO`

4. **配置拦截器**
   - 确保`/merchant/comment/**`路径被`MerchantLoginInterceptor`拦截

### 6.3 最后阶段：前端对接
1. 前端页面调用新的API接口
2. 完整功能测试
3. 用户体验优化

## 七、技术依赖

### 7.1 现有依赖
- `ShopComment`实体类
- `CommentReport`实体类  
- `IShopCommentService`服务
- `MerchantHolder`工具类
- `MerchantLoginInterceptor`拦截器

### 7.2 新增依赖
- 无需新增外部依赖
- 复用现有的MyBatis-Plus、Spring Boot等框架

## 八、注意事项

1. **数据一致性：** 确保商家回复操作的原子性
2. **性能优化：** 评论列表查询需要考虑分页和索引优化
3. **安全性：** 防止SQL注入和XSS攻击
4. **用户体验：** 回复内容支持富文本但需要过滤危险标签
5. **扩展性：** 预留接口扩展空间，如批量操作等功能

---

**文档版本：** v1.0  
**最后更新：** 2024-12-23  
**设计者：** Claude 4.0 Sonnet