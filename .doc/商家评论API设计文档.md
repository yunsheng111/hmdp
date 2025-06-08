# 商家评论管理系统API接口设计文档

**版本：** v1.0  
**创建时间：** 2024-12-23  
**设计者：** Claude 4.0 Sonnet  

## 一、概述

### 1.1 设计目标
本文档定义了商家评论管理系统的API接口规范，为商家提供完整的评论管理功能，包括评论查看、回复管理、统计分析和举报处理等核心功能。

### 1.2 设计原则
- **职责分离：** 商家端与用户端评论功能完全分离
- **权限控制：** 商家只能管理自己店铺的评论
- **RESTful设计：** 遵循REST API设计规范
- **安全性：** 所有接口需要商家身份认证

### 1.3 技术架构
- **基础路径：** `/merchant/comment`
- **认证方式：** 基于现有商家登录体系
- **数据模型：** 复用现有`ShopComment`实体
- **权限控制：** 利用`MerchantHolder`和`MerchantLoginInterceptor`

## 二、API接口规范

### 2.1 评论列表查询

#### 接口信息
- **URL：** `GET /merchant/comment/list`
- **功能：** 获取当前商家店铺的评论列表
- **权限：** 需要商家登录

#### 请求参数
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页大小 |
| rating | Integer | 否 | - | 评分筛选(1-5星) |
| status | Integer | 否 | - | 状态筛选(0-正常,1-用户隐藏,2-管理员隐藏) |
| hasReply | Boolean | 否 | - | 是否已回复筛选 |
| sortBy | String | 否 | time | 排序字段(time/rating) |
| order | String | 否 | desc | 排序方向(asc/desc) |

#### 响应示例
```json
{
  "success": true,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 123,
        "userNickname": "张三",
        "userIcon": "https://example.com/avatar.jpg",
        "orderId": 456,
        "rating": 5,
        "content": "服务很好，菜品新鲜美味！",
        "reply": "感谢您的好评，欢迎再次光临！",
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

#### 接口信息
- **URL：** `POST /merchant/comment/{commentId}/reply`
- **功能：** 商家对指定评论进行回复
- **权限：** 需要商家登录，且评论属于当前商家店铺

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

#### 请求体
```json
{
  "content": "感谢您的评价，我们会继续努力提供更好的服务！"
}
```

#### 响应示例
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

#### 接口信息
- **URL：** `PUT /merchant/comment/{commentId}/reply`
- **功能：** 修改已有的商家回复
- **权限：** 需要商家登录，且评论属于当前商家店铺

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

#### 请求体
```json
{
  "content": "修改后的回复内容"
}
```

#### 响应示例
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

#### 接口信息
- **URL：** `DELETE /merchant/comment/{commentId}/reply`
- **功能：** 删除商家回复
- **权限：** 需要商家登录，且评论属于当前商家店铺

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

#### 响应示例
```json
{
  "success": true,
  "data": null
}
```

### 2.5 评论统计数据

#### 接口信息
- **URL：** `GET /merchant/comment/statistics`
- **功能：** 获取当前商家店铺的评论统计信息
- **权限：** 需要商家登录

#### 响应示例
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

#### 接口信息
- **URL：** `POST /merchant/comment/{commentId}/report`
- **功能：** 商家举报不当评论
- **权限：** 需要商家登录

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

#### 请求体
```json
{
  "reason": "评论内容不实，恶意差评"
}
```

#### 响应示例
```json
{
  "success": true,
  "data": {
    "reportId": 123,
    "status": "已提交举报，等待管理员处理"
  }
}
```

## 三、数据模型定义

### 3.1 评论列表响应模型
```typescript
interface MerchantCommentListDTO {
  id: number;                    // 评论ID
  userId: number;                // 用户ID
  userNickname: string;          // 用户昵称
  userIcon: string;              // 用户头像URL
  orderId: number;               // 关联订单ID
  rating: number;                // 评分(1-5)
  content: string;               // 评论内容
  reply?: string;                // 商家回复(可选)
  status: number;                // 评论状态
  createTime: string;            // 创建时间(ISO格式)
  replyTime?: string;            // 回复时间(ISO格式,可选)
}
```

### 3.2 回复请求模型
```typescript
interface MerchantCommentReplyDTO {
  content: string;               // 回复内容(1-500字符)
}
```

### 3.3 统计数据模型
```typescript
interface MerchantCommentStatisticsDTO {
  totalComments: number;         // 总评论数
  averageRating: number;         // 平均评分
  ratingDistribution: {          // 评分分布
    [key: string]: number;       // 星级: 数量
  };
  pendingReplyCount: number;     // 待回复数量
  replyRate: number;             // 回复率(百分比)
}
```

### 3.4 举报请求模型
```typescript
interface MerchantCommentReportDTO {
  reason: string;                // 举报原因(1-200字符)
}
```

## 四、权限控制

### 4.1 认证机制
- **拦截器：** `MerchantLoginInterceptor`
- **拦截路径：** `/merchant/comment/**`
- **认证头：** `merchant-token`
- **身份获取：** `MerchantHolder.getMerchant()`

### 4.2 数据权限
```java
// 权限校验示例代码
Long currentShopId = MerchantHolder.getMerchant().getShopId();
ShopComment comment = shopCommentMapper.selectById(commentId);

if (comment == null || !comment.getShopId().equals(currentShopId)) {
    throw new CommentException("评论不存在或无权限操作");
}
```

### 4.3 权限验证流程
1. 请求拦截验证商家登录状态
2. 获取当前商家的店铺ID
3. 验证操作对象是否属于当前商家
4. 执行具体业务逻辑

## 五、错误处理

### 5.1 错误响应格式
```json
{
  "success": false,
  "errorMsg": "具体的错误信息"
}
```

### 5.2 错误码对照表
| HTTP状态码 | 错误信息 | 说明 |
|------------|----------|------|
| 401 | 商家未登录 | 需要商家身份认证 |
| 403 | 评论不存在或无权限操作 | 数据权限验证失败 |
| 400 | 回复内容不能为空 | 参数验证失败 |
| 400 | 回复内容不能超过500字 | 参数长度验证失败 |
| 400 | 该评论已有回复，请使用修改接口 | 业务逻辑验证失败 |
| 400 | 该评论尚未回复，无法删除 | 业务逻辑验证失败 |
| 400 | 举报原因不能为空 | 参数验证失败 |
| 400 | 举报原因不能超过200字 | 参数长度验证失败 |

## 六、使用示例

### 6.1 获取评论列表
```javascript
// 请求示例
const response = await fetch('/merchant/comment/list?current=1&size=10&rating=5', {
  method: 'GET',
  headers: {
    'merchant-token': 'your-merchant-token'
  }
});

const data = await response.json();
console.log(data.data.records); // 评论列表
```

### 6.2 回复评论
```javascript
// 请求示例
const response = await fetch('/merchant/comment/123/reply', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'merchant-token': 'your-merchant-token'
  },
  body: JSON.stringify({
    content: '感谢您的评价！'
  })
});

const data = await response.json();
console.log(data.data.reply); // 回复内容
```

### 6.3 获取统计数据
```javascript
// 请求示例
const response = await fetch('/merchant/comment/statistics', {
  method: 'GET',
  headers: {
    'merchant-token': 'your-merchant-token'
  }
});

const data = await response.json();
console.log(data.data.averageRating); // 平均评分
```

## 七、性能考虑

### 7.1 查询优化
- 评论列表查询使用分页，避免大量数据加载
- 在`shop_id`、`create_time`字段上建立索引
- 统计数据可考虑缓存机制

### 7.2 并发控制
- 回复操作使用乐观锁防止并发冲突
- 统计数据更新采用异步处理

### 7.3 缓存策略
- 评论统计数据可缓存30分钟
- 评论列表可缓存5分钟
- 使用Redis作为缓存存储

## 八、安全考虑

### 8.1 输入验证
- 所有用户输入进行严格验证
- 防止SQL注入和XSS攻击
- 回复内容进行HTML标签过滤

### 8.2 权限控制
- 严格的数据权限校验
- 防止越权访问其他商家数据
- 敏感操作记录审计日志

### 8.3 频率限制
- 对回复操作进行频率限制
- 防止恶意刷评论回复
- 举报功能防止滥用

## 九、版本管理

### 9.1 版本号规则
- 主版本号：重大功能变更
- 次版本号：新增功能
- 修订版本号：Bug修复

### 9.2 兼容性
- 向后兼容原则
- 废弃功能提前通知
- 提供迁移指南

---

**文档状态：** 设计完成  
**下一步：** 后端实现  
**联系人：** 开发团队