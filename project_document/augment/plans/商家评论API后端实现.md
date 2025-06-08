# 商家评论API后端实现计划

**任务名称：** 商家评论API后端实现  
**创建时间：** 2024-12-23  
**实施阶段：** 后端实现  

## 一、实施概述

### 1.1 实施目标
基于已完成的API设计文档，实现商家评论管理系统的完整后端功能，包括控制器层、服务层和数据传输对象。

### 1.2 实施原则
- **独立实现：** 创建完全独立的商家评论管理模块
- **复用现有：** 最大化利用现有的认证体系和数据模型
- **权限安全：** 确保商家只能管理自己店铺的评论
- **代码质量：** 遵循现有代码规范和架构风格

### 1.3 技术架构
- **控制器：** `MerchantCommentController`
- **服务接口：** `IMerchantCommentService`
- **服务实现：** `MerchantCommentServiceImpl`
- **数据传输：** 5个专用DTO类
- **权限控制：** 复用`MerchantLoginInterceptor`

## 二、详细实施步骤

### 步骤1：创建查询参数DTO
**文件：** `src/main/java/com/hmdp/dto/MerchantCommentQueryDTO.java`
**功能：** 封装评论列表查询的分页、筛选、排序参数
**字段：** current, size, rating, status, hasReply, sortBy, order
**验证：** 添加参数默认值和范围验证

### 步骤2：创建评论列表DTO
**文件：** `src/main/java/com/hmdp/dto/MerchantCommentListDTO.java`
**功能：** 封装评论列表展示数据，包含用户信息
**字段：** id, userId, userNickname, userIcon, orderId, rating, content, reply, status, createTime, replyTime
**特点：** 包含用户昵称和头像信息，便于前端展示

### 步骤3：创建回复操作DTO
**文件：** `src/main/java/com/hmdp/dto/MerchantCommentReplyDTO.java`
**功能：** 封装商家回复内容
**字段：** content
**验证：** @NotBlank(message = "回复内容不能为空"), @Length(max = 500, message = "回复内容不能超过500字")

### 步骤4：创建统计数据DTO
**文件：** `src/main/java/com/hmdp/dto/MerchantCommentStatisticsDTO.java`
**功能：** 封装评论统计信息
**字段：** totalComments, averageRating, ratingDistribution, pendingReplyCount, replyRate
**计算：** 包含评分分布Map和回复率计算

### 步骤5：创建举报DTO
**文件：** `src/main/java/com/hmdp/dto/MerchantCommentReportDTO.java`
**功能：** 封装举报信息
**字段：** reason
**验证：** @NotBlank(message = "举报原因不能为空"), @Length(max = 200, message = "举报原因不能超过200字")

### 步骤6：创建服务接口
**文件：** `src/main/java/com/hmdp/service/IMerchantCommentService.java`
**功能：** 定义商家评论管理的业务方法
**方法：**
- `Result getCommentList(MerchantCommentQueryDTO queryDTO)` - 获取评论列表
- `Result replyComment(Long commentId, MerchantCommentReplyDTO replyDTO)` - 回复评论
- `Result updateReply(Long commentId, MerchantCommentReplyDTO replyDTO)` - 修改回复
- `Result deleteReply(Long commentId)` - 删除回复
- `Result getStatistics()` - 获取统计数据
- `Result reportComment(Long commentId, MerchantCommentReportDTO reportDTO)` - 举报评论

### 步骤7：创建服务实现
**文件：** `src/main/java/com/hmdp/service/impl/MerchantCommentServiceImpl.java`
**功能：** 实现商家评论管理的具体业务逻辑
**核心逻辑：**
- 权限校验：验证评论是否属于当前商家店铺
- 数据查询：使用MyBatis-Plus进行条件查询和分页
- 统计计算：计算平均评分、评分分布、回复率等
- 异常处理：使用CommentException处理业务异常

### 步骤8：创建控制器
**文件：** `src/main/java/com/hmdp/controller/MerchantCommentController.java`
**功能：** 提供商家评论管理的REST API接口
**接口：**
- `GET /merchant/comment/list` - 评论列表查询
- `POST /merchant/comment/{commentId}/reply` - 商家回复评论
- `PUT /merchant/comment/{commentId}/reply` - 修改商家回复
- `DELETE /merchant/comment/{commentId}/reply` - 删除商家回复
- `GET /merchant/comment/statistics` - 评论统计数据
- `POST /merchant/comment/{commentId}/report` - 举报不当评论

### 步骤9：配置拦截器
**文件：** `src/main/java/com/hmdp/config/MvcConfig.java`
**功能：** 确保`/merchant/comment/**`路径被`MerchantLoginInterceptor`拦截
**修改：** 在现有商家拦截器配置中添加评论管理路径

## 三、技术实现要点

### 3.1 权限控制实现
```java
// 获取当前商家店铺ID
Long currentShopId = MerchantHolder.getMerchant().getShopId();

// 验证评论是否属于当前商家店铺
ShopComment comment = getById(commentId);
if (comment == null || !comment.getShopId().equals(currentShopId)) {
    throw new CommentException("评论不存在或无权限操作");
}
```

### 3.2 分页查询实现
```java
// 构建查询条件
QueryWrapper<ShopComment> queryWrapper = new QueryWrapper<>();
queryWrapper.eq("shop_id", currentShopId);

// 添加筛选条件
if (queryDTO.getRating() != null) {
    queryWrapper.eq("rating", queryDTO.getRating());
}

// 分页查询
Page<ShopComment> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
Page<ShopComment> result = page(page, queryWrapper);
```

### 3.3 统计数据计算
```java
// 计算平均评分
Double averageRating = baseMapper.selectAvgRating(currentShopId);

// 计算评分分布
Map<Integer, Long> ratingDistribution = baseMapper.selectRatingDistribution(currentShopId);

// 计算回复率
Long totalComments = baseMapper.selectCount(queryWrapper);
Long repliedComments = baseMapper.selectRepliedCount(currentShopId);
Double replyRate = totalComments > 0 ? (repliedComments * 100.0 / totalComments) : 0.0;
```

## 四、依赖组件

### 4.1 现有依赖
- `ShopComment`实体类 - 评论数据模型
- `CommentReport`实体类 - 举报数据模型
- `MerchantHolder`工具类 - 获取当前商家信息
- `MerchantLoginInterceptor`拦截器 - 商家登录验证
- `Result`类 - 统一响应格式
- `CommentException`异常类 - 业务异常处理

### 4.2 新增依赖
- 无需新增外部依赖
- 复用现有的MyBatis-Plus、Spring Boot、Validation等框架

## 五、预期结果

### 5.1 功能完整性
- ✅ 评论列表查询（支持分页、筛选、排序）
- ✅ 商家回复管理（增删改）
- ✅ 评论统计分析
- ✅ 不当评论举报

### 5.2 安全性保障
- ✅ 商家身份认证
- ✅ 数据权限控制
- ✅ 参数验证
- ✅ 异常处理

### 5.3 代码质量
- ✅ 遵循现有架构风格
- ✅ 符合RESTful设计规范
- ✅ 完整的注释和文档
- ✅ 统一的响应格式

---

**计划版本：** v1.0  
**最后更新：** 2024-12-23  
**制定者：** Claude 4.0 Sonnet
