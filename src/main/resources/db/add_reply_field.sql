-- 为tb_shop_comment表添加reply字段
-- 用于存储商家对评论的回复内容

ALTER TABLE tb_shop_comment 
ADD COLUMN reply VARCHAR(500) NULL COMMENT '商家回复内容' AFTER content;

-- 更新现有数据的示例（可选）
-- UPDATE tb_shop_comment SET reply = '感谢您的评价！' WHERE id = 1;
