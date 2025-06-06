# Blog功能增强设计文档

**版本**: v1.0
**创建时间**: [2023-07-12 12:30:00 +08:00]
**创建者**: AR (架构师)

## 1. 设计概述
本文档详细描述在黑马点评系统中为个人主页页面添加"我的"和"关注"按钮，以及实现笔记已读功能的技术设计方案。

## 2. 架构设计

### 2.1 总体架构
采用Redis + 前端子标签方案，具体包括：
1. 使用Redis存储用户的博客已读状态
2. 在Blog实体类中添加isRead字段（非持久化）
3. 在前端页面笔记标签下添加"我的"和"关注"两个子标签
4. 根据子标签选择调用不同的API获取数据

### 2.2 数据结构设计

#### 2.2.1 Redis数据结构
- **键名格式**: `blog:read:{userId}`
- **数据类型**: ZSet (有序集合)
- **成员**: 博客ID
- **分数**: 阅读时间戳
- **示例**:
  ```
  blog:read:1 -> {
    "101": 1689147600, // 博客ID: 时间戳
    "102": 1689151200,
    ...
  }
  ```

#### 2.2.2 实体类设计
在Blog实体类中添加isRead字段：
```java
@TableField(exist = false)
private Boolean isRead;
```

### 2.3 API设计

#### 2.3.1 标记博客已读
- **路径**: `/blog/read/{id}`
- **方法**: POST
- **参数**: 
  - `id`: 博客ID
- **返回**: 
  - 成功: `{ "success": true, "data": 未读博客数量 }`
  - 失败: `{ "success": false, "errorMsg": "错误信息" }`
- **处理逻辑**:
  1. 获取当前登录用户ID
  2. 将博客ID添加到Redis的ZSet中，分数为当前时间戳
  3. 返回未读博客数量

#### 2.3.2 查询关注用户的博客
- **路径**: `/blog/of/follow`
- **方法**: GET
- **参数**: 
  - `lastId`: 上次查询的最后一条博客的时间戳
  - `offset`: 偏移量
- **返回**: 
  - 成功: `{ "success": true, "data": { "list": [...], "offset": 偏移量, "minTime": 最小时间戳 } }`
  - 失败: `{ "success": false, "errorMsg": "错误信息" }`
- **处理逻辑**:
  1. 获取当前登录用户ID
  2. 查询关注用户的博客列表
  3. 查询Redis中用户的已读博客列表
  4. 填充博客的isRead字段
  5. 返回博客列表、偏移量和最小时间戳

### 2.4 前端设计

#### 2.4.1 页面结构
在笔记标签页添加"我的"和"关注"两个子标签：
```html
<div class="blog-tabs">
  <div class="blog-tab" :class="{ active: activeSubTab === 'my' }" @click="switchSubTab('my')">我的</div>
  <div class="blog-tab" :class="{ active: activeSubTab === 'follow' }" @click="switchSubTab('follow')">关注</div>
</div>
```

#### 2.4.2 样式设计
为子标签和未读标记添加样式：
```css
.blog-tabs {
  display: flex;
  margin-bottom: 15px;
}

.blog-tab {
  padding: 5px 15px;
  border-radius: 15px;
  margin-right: 10px;
  cursor: pointer;
  background-color: #f5f5f5;
}

.blog-tab.active {
  background-color: #ff6633;
  color: white;
}

.unread-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #ff6633;
}
```

#### 2.4.3 交互逻辑
- 点击"我的"子标签，调用`/blog/of/me`接口获取自己的博客
- 点击"关注"子标签，调用`/blog/of/follow`接口获取关注用户的博客
- 点击博客，调用`/blog/read/{id}`接口标记为已读，并跳转到博客详情页

## 3. 设计原则应用

### 3.1 KISS原则
- 使用简单的Redis数据结构存储已读状态
- 前端采用简单的子标签切换方式
- 保持API设计简洁明了

### 3.2 SOLID原则
- **单一职责原则**: Blog实体类只负责数据传输，已读状态由Redis存储
- **开闭原则**: 通过扩展Blog实体类添加isRead字段，不修改现有功能
- **接口隔离原则**: API接口功能单一，职责明确

### 3.3 高内聚低耦合
- 已读功能与现有功能松散耦合，不影响现有功能
- Redis存储与实体类分离，便于后续扩展

## 4. 性能与安全考虑

### 4.1 性能优化
- 使用Redis存储已读状态，读写性能高
- 批量查询已读状态，减少Redis请求次数
- 前端使用懒加载，减少初始加载时间

### 4.2 安全考虑
- 验证用户登录状态，未登录用户无法标记已读
- 验证用户权限，只能标记自己可见的博客为已读
- 防止Redis键名冲突，使用用户ID作为键名的一部分

## 5. 更新日志
| 时间 | 版本 | 更新内容 | 更新原因 |
|------|------|----------|----------|
| [2023-07-12 12:30:00 +08:00] | v1.0 | 初始设计文档 | 基于方案讨论会议决策，详细设计实现方案 |