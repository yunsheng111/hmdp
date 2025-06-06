# 商店评论系统数据库架构 v1.0

**创建时间:** [2024-07-29 12:30:00 +08:00]  
**创建者:** AR (架构师)  
**文档状态:** 已批准  

## 更新日志

| 版本 | 日期 | 更新者 | 更新内容 | 更新原因 |
|------|------|--------|----------|----------|
| 1.0 | [2024-07-29 12:30:00 +08:00] | AR | 初始版本 | 创建商店评论系统数据库架构文档 |

## 表结构

### 1. tb_shop_comment (商店评论表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| shop_id | bigint | NOT NULL, INDEX | 关联的商店ID |
| user_id | bigint | NOT NULL, INDEX | 评论用户ID |
| order_id | bigint | NOT NULL | 关联的订单ID（确保评论来自已验证的购买） |
| rating | int | NOT NULL | 评分(1-5) |
| content | varchar(1024) | NOT NULL | 评论内容 |
| status | tinyint | NOT NULL, DEFAULT 0 | 状态：0=正常，1=用户隐藏，2=管理员隐藏 |
| create_time | datetime | NOT NULL | 创建时间 |
| update_time | datetime | NOT NULL | 更新时间 |

**索引:**
- PRIMARY KEY (`id`)
- INDEX `idx_shop_id` (`shop_id`)
- INDEX `idx_user_id` (`user_id`)
- INDEX `idx_order_id` (`order_id`)
- INDEX `idx_shop_status_time` (`shop_id`, `status`, `create_time`) - 用于按时间排序查询
- INDEX `idx_shop_status_rating` (`shop_id`, `status`, `rating`) - 用于按评分排序查询

### 2. tb_comment_report (评论举报表)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| comment_id | bigint | NOT NULL, INDEX | 被举报的评论ID |
| reporter_id | bigint | NOT NULL | 举报者ID（商家） |
| reason | varchar(255) | NOT NULL | 举报原因 |
| status | tinyint | NOT NULL, DEFAULT 0 | 状态：0=待处理，1=已处理 |
| create_time | datetime | NOT NULL | 创建时间 |
| update_time | datetime | NOT NULL | 更新时间 |

**索引:**
- PRIMARY KEY (`id`)
- INDEX `idx_comment_id` (`comment_id`)
- INDEX `idx_reporter_id` (`reporter_id`)
- INDEX `idx_status` (`status`) - 用于查询待处理的举报

## 表关系

- `tb_shop_comment.shop_id` 关联到 `tb_shop.id`
- `tb_shop_comment.user_id` 关联到 `tb_user.id`
- `tb_shop_comment.order_id` 关联到 `tb_order.id`（假设存在订单表）
- `tb_comment_report.comment_id` 关联到 `tb_shop_comment.id`
- `tb_comment_report.reporter_id` 关联到 `tb_user.id`

## 数据库变更SQL

```sql
-- 创建商店评论表
CREATE TABLE tb_shop_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content VARCHAR(1024) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_shop_id (shop_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_shop_status_time (shop_id, status, create_time),
    INDEX idx_shop_status_rating (shop_id, status, rating)
);

-- 创建评论举报表
CREATE TABLE tb_comment_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_comment_id (comment_id),
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_status (status)
);
```

## 注意事项

1. 评论表使用软删除策略（通过status字段），而不是物理删除
2. 索引设计考虑了常见查询场景，特别是按店铺ID和状态筛选后的排序查询
3. 评论必须关联到有效订单，确保只有实际购买过的用户才能评论
4. 为了性能考虑，可能需要在tb_shop表中添加avg_rating字段，用于存储平均评分