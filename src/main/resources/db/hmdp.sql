/*
 Navicat Premium Data Transfer

 Source Server         : heima
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : localhost:3306
 Source Schema         : hmdp

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 28/05/2025 02:30:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_blog
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) NOT NULL COMMENT '商户id',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '探店的照片，最多9张，多张以\",\"隔开',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '探店的文字描述',
  `liked` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '点赞数量',
  `comments` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '评论数量',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_blog
-- ----------------------------
INSERT INTO `tb_blog` VALUES (4, 4, 2, '无尽浪漫的夜晚丨在万花丛中摇晃着红酒杯🍷品战斧牛排🥩', '/imgs/blogs/7/14/4771fefb-1a87-4252-816c-9f7ec41ffa4a.jpg,/imgs/blogs/4/10/2f07e3c9-ddce-482d-9ea7-c21450f8d7cd.jpg,/imgs/blogs/2/6/b0756279-65da-4f2d-b62a-33f74b06454a.jpg,/imgs/blogs/10/7/7e97f47d-eb49-4dc9-a583-95faa7aed287.jpg,/imgs/blogs/1/2/4a7b496b-2a08-4af7-aa95-df2c3bd0ef97.jpg,/imgs/blogs/14/3/52b290eb-8b5d-403b-8373-ba0bb856d18e.jpg', '生活就是一半烟火·一半诗意<br/>手执烟火谋生活·心怀诗意以谋爱·<br/>当然<br/>\r\n男朋友给不了的浪漫要学会自己给🍒<br/>\n无法重来的一生·尽量快乐.<br/><br/>🏰「小筑里·神秘浪漫花园餐厅」🏰<br/><br/>\n💯这是一家最最最美花园的西餐厅·到处都是花餐桌上是花前台是花  美好无处不在\n品一口葡萄酒，维亚红酒马瑟兰·微醺上头工作的疲惫消失无际·生如此多娇🍃<br/><br/>📍地址:延安路200号(家乐福面)<br/><br/>🚌交通:地铁①号线定安路B口出右转过下通道右转就到啦～<br/><br/>--------------🥣菜品详情🥣---------------<br/><br/>「战斧牛排]<br/>\n超大一块战斧牛排经过火焰的炙烤发出阵阵香，外焦里嫩让人垂涎欲滴，切开牛排的那一刻，牛排的汁水顺势流了出来，分熟的牛排肉质软，简直细嫩到犯规，一刻都等不了要放入嘴里咀嚼～<br/><br/>「奶油培根意面」<br/>太太太好吃了💯<br/>我真的无法形容它的美妙，意面混合奶油香菇的香味真的太太太香了，我真的舔盘了，一丁点美味都不想浪费‼️<br/><br/><br/>「香菜汁烤鲈鱼」<br/>这个酱是辣的 真的绝好吃‼️<br/>鲈鱼本身就很嫩没什么刺，烤过之后外皮酥酥的，鱼肉蘸上酱料根本停不下来啊啊啊啊<br/>能吃辣椒的小伙伴一定要尝尝<br/><br/>非常可 好吃子🍽\n<br/>--------------🍃个人感受🍃---------------<br/><br/>【👩🏻‍🍳服务】<br/>小姐姐特别耐心的给我们介绍彩票 <br/>推荐特色菜品，拍照需要帮忙也是尽心尽力配合，太爱他们了<br/><br/>【🍃环境】<br/>比较有格调的西餐厅 整个餐厅的布局可称得上的万花丛生 有种在人间仙境的感觉🌸<br/>集美食美酒与鲜花为一体的风格店铺 令人向往<br/>烟火皆是生活 人间皆是浪漫<br/>', 3, 104, '2025-02-04 19:50:01', '2025-05-26 19:56:24');
INSERT INTO `tb_blog` VALUES (5, 1, 2, '人均30💰杭州这家港式茶餐厅我疯狂打call‼️', '/imgs/blogs/4/7/863cc302-d150-420d-a596-b16e9232a1a6.jpg,/imgs/blogs/11/12/8b37d208-9414-4e78-b065-9199647bb3e3.jpg,/imgs/blogs/4/1/fa74a6d6-3026-4cb7-b0b6-35abb1e52d11.jpg,/imgs/blogs/9/12/ac2ce2fb-0605-4f14-82cc-c962b8c86688.jpg,/imgs/blogs/4/0/26a7cd7e-6320-432c-a0b4-1b7418f45ec7.jpg,/imgs/blogs/15/9/cea51d9b-ac15-49f6-b9f1-9cf81e9b9c85.jpg', '又吃到一家好吃的茶餐厅🍴环境是怀旧tvb港风📺边吃边拍照片📷几十种菜品均价都在20+💰可以是很平价了！<br>·<br>店名：九记冰厅(远洋店)<br>地址：杭州市丽水路远洋乐堤港负一楼（溜冰场旁边）<br>·<br>✔️黯然销魂饭（38💰）<br>这碗饭我吹爆！米饭上盖满了甜甜的叉烧 还有两颗溏心蛋🍳每一粒米饭都裹着浓郁的酱汁 光盘了<br>·<br>✔️铜锣湾漏奶华（28💰）<br>黄油吐司烤的脆脆的 上面洒满了可可粉🍫一刀切开 奶盖流心像瀑布一样流出来  满足<br>·<br>✔️神仙一口西多士士（16💰）<br>简简单单却超级好吃！西多士烤的很脆 黄油味浓郁 面包体超级柔软 上面淋了炼乳<br>·<br>✔️怀旧五柳炸蛋饭（28💰）<br>四个鸡蛋炸成蓬松的炸蛋！也太好吃了吧！还有大块鸡排 上淋了酸甜的酱汁 太合我胃口了！！<br>·<br>✔️烧味双拼例牌（66💰）<br>选了烧鹅➕叉烧 他家烧腊品质真的惊艳到我！据说是每日广州发货 到店现烧现卖的黑棕鹅 每口都是正宗的味道！肉质很嫩 皮超级超级酥脆！一口爆油！叉烧肉也一点都不柴 甜甜的很入味 搭配梅子酱很解腻 ！<br>·<br>✔️红烧脆皮乳鸽（18.8💰）<br>乳鸽很大只 这个价格也太划算了吧， 肉质很有嚼劲 脆皮很酥 越吃越香～<br>·<br>✔️大满足小吃拼盘（25💰）<br>翅尖➕咖喱鱼蛋➕蝴蝶虾➕盐酥鸡<br>zui喜欢里面的咖喱鱼！咖喱酱香甜浓郁！鱼蛋很q弹～<br>·<br>✔️港式熊仔丝袜奶茶（19💰）<br>小熊🐻造型的奶茶冰也太可爱了！颜值担当 很地道的丝袜奶茶 茶味特别浓郁～<br>·', 2, 0, '2025-02-04 20:57:49', '2025-05-27 17:21:53');
INSERT INTO `tb_blog` VALUES (6, 10, 1, '杭州周末好去处｜💰50就可以骑马啦🐎', '/imgs/blogs/blog1.jpg', '杭州周末好去处｜💰50就可以骑马啦🐎', 2, 0, '2025-01-11 16:05:47', '2025-05-26 19:41:32');
INSERT INTO `tb_blog` VALUES (7, 10, 1, '杭州周末好去处｜💰50就可以骑马啦🐎', '/imgs/blogs/blog1.jpg', '杭州周末好去处｜💰50就可以骑马啦🐎', 2, 0, '2025-01-11 16:05:47', '2025-05-26 21:47:23');
INSERT INTO `tb_blog` VALUES (25, 3, 1023, '3333', '/imgs/blogs/6/0/fe70f5f1-847f-4b26-995d-b6868d9ab448.png', '9999', 4, NULL, '2025-05-22 14:01:39', '2025-05-26 19:40:20');
INSERT INTO `tb_blog` VALUES (26, 4, 1023, '肉夹馍', '/imgs/blogs/4/3/118e7bac-cbe0-48df-8a00-b6fa936e810f.jpg', '1111111111111111', 23, NULL, '2025-05-22 14:13:48', '2025-05-26 21:45:04');
INSERT INTO `tb_blog` VALUES (27, 5, 1023, '000', '/imgs/blogs/10/2/df515830-983e-486b-8750-2fef848a4cec.png', '777777', 1, NULL, '2025-05-22 14:20:00', '2025-05-25 20:34:30');
INSERT INTO `tb_blog` VALUES (28, 3, 1023, '111', '/imgs/blogs/6/1/78e60588-bddb-4a27-9498-d87537af831b.png', '222', 1, NULL, '2025-05-22 14:21:13', '2025-05-25 20:34:27');
INSERT INTO `tb_blog` VALUES (29, 1, 1023, '0000', '/imgs/blogs/3/1/2fd75f4f-a1e3-4327-ab8b-42a95a3590b5.png', '0000000000000', 0, 0, '2025-05-23 19:40:48', '2025-05-23 19:40:48');
INSERT INTO `tb_blog` VALUES (31, 1, 1023, '贾生年少虚垂泪', '/imgs/blogs/0/13/34ffc957-2554-4bc4-bdc6-840f4e76612d.png', '阿西木撒可能性马克思', 0, 0, '2025-05-23 20:44:09', '2025-05-23 20:44:09');
INSERT INTO `tb_blog` VALUES (35, 3, 1, 'mnoaksnaks', '/imgs/blogs/5/7/3b82da44-634c-45e2-b362-a6a8b68299bb.png', '此电脑卡DSCK就', 2, NULL, '2025-05-26 21:45:58', '2025-05-27 22:48:49');
INSERT INTO `tb_blog` VALUES (36, 2, 2, '第三次上传', '/imgs/blogs/9/10/20847e6b-67c1-47ae-a29b-1f32a3bbfe0b.jfif', '没看看得开懒得理看看了', 0, NULL, '2025-05-27 19:21:15', '2025-05-27 22:49:06');
INSERT INTO `tb_blog` VALUES (37, 5, 2, '第四次发布', '/imgs/blogs/6/0/090a83fe-d0a1-4fec-9b93-79f58b1b37c4.jfif', '莎米拉实木多层没啥可能', 0, NULL, '2025-05-27 20:25:20', '2025-05-27 22:49:03');

-- ----------------------------
-- Table structure for tb_blog_comments
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `blog_id` bigint(0) UNSIGNED NOT NULL COMMENT '探店id',
  `parent_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
  `answer_id` bigint(0) UNSIGNED NOT NULL COMMENT '回复的评论id',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '回复的内容',
  `liked` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '点赞数',
  `status` tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_follow
-- ----------------------------
DROP TABLE IF EXISTS `tb_follow`;
CREATE TABLE `tb_follow`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `follow_user_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的用户id',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_follow
-- ----------------------------
INSERT INTO `tb_follow` VALUES (3, 1023, 2, '2025-05-27 18:06:22');
INSERT INTO `tb_follow` VALUES (4, 1, 2, '2025-05-27 18:08:22');
INSERT INTO `tb_follow` VALUES (5, 1023, 1, '2025-05-27 22:48:43');

-- ----------------------------
-- Table structure for tb_seckill_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_seckill_voucher`;
CREATE TABLE `tb_seckill_voucher`  (
  `voucher_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的优惠券的id',
  `stock` int(0) NOT NULL COMMENT '库存',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `begin_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '生效时间',
  `end_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '失效时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`voucher_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '秒杀优惠券表，与优惠券是一对一关系' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_seckill_voucher
-- ----------------------------
INSERT INTO `tb_seckill_voucher` VALUES (12, 99, '2025-01-18 20:20:59', '2025-01-10 20:07:18', '2025-05-18 12:07:18', '2025-04-23 22:50:49');

-- ----------------------------
-- Table structure for tb_shop
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop`;
CREATE TABLE `tb_shop`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) UNSIGNED NULL COMMENT '商家ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺名称',
  `type_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺类型的id',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商铺图片，多个图片以\',\'隔开',
  `area` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商圈，例如陆家嘴',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
  `x` double UNSIGNED NOT NULL COMMENT '经度',
  `y` double UNSIGNED NOT NULL COMMENT '维度',
  `avg_price` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '均价，取整数',
  `sold` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '销量',
  `comments` int(10) UNSIGNED ZEROFILL NOT NULL COMMENT '评论数量',
  `score` int(2) UNSIGNED ZEROFILL NOT NULL COMMENT '评分，1~5分，乘10保存，避免小数',
  `open_hours` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间，例如 10:00-22:00',
  `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '营业状态，0-休息，1-营业',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `foreign_key_type`(`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_shop
-- ----------------------------
INSERT INTO `tb_shop` VALUES (1, 1, '103茶餐厅', 1, 'https://qcloud.dpfile.com/pc/jiclIsCKmOI2arxKN1Uf0Hx3PucIJH8q0QSz-Z8llzcN56-_QiKuOvyio1OOxsRtFoXqu0G3iT2T27qat3WhLVEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vfCF2ubeXzk49OsGrXt_KYDCngOyCwZK-s3fqawWswzk.jpg,https://qcloud.dpfile.com/pc/IOf6VX3qaBgFXFVgp75w-KKJmWZjFc8GXDU8g9bQC6YGCpAmG00QbfT4vCCBj7njuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '大关', '金华路锦昌文华苑29号', 120.149192, 30.316078, 80, 0000004215, 0000003035, 37, '10:00-22:00', 1, '2025-02-22 18:10:39', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (2, 1, '蔡馬洪涛烤肉·老北京铜锅涮羊肉', 1, 'https://p0.meituan.net/bbia/c1870d570e73accbc9fee90b48faca41195272.jpg,http://p0.meituan.net/mogu/397e40c28fc87715b3d5435710a9f88d706914.jpg,https://qcloud.dpfile.com/pc/MZTdRDqCZdbPDUO0Hk6lZENRKzpKRF7kavrkEI99OxqBZTzPfIxa5E33gBfGouhFuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '拱宸桥/上塘', '上塘路1035号（中国工商银行旁）', 120.151505, 30.333422, 85, 0000002160, 0000001460, 46, '11:30-03:00', 1, '2025-02-22 19:00:13', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (3, 1, '新白鹿餐厅(运河上街店)', 1, 'https://p0.meituan.net/biztone/694233_1619500156517.jpeg,https://img.meituan.net/msmerchant/876ca8983f7395556eda9ceb064e6bc51840883.png,https://img.meituan.net/msmerchant/86a76ed53c28eff709a36099aefe28b51554088.png', '运河上街', '台州路2号运河上街购物中心F5', 120.151954, 30.32497, 61, 0000012035, 0000008045, 47, '10:30-21:00', 1, '2025-02-22 19:10:05', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (4, 1, 'Mamala(杭州远洋乐堤港店)', 1, 'https://img.meituan.net/msmerchant/232f8fdf09050838bd33fb24e79f30f9606056.jpg,https://qcloud.dpfile.com/pc/rDe48Xe15nQOHCcEEkmKUp5wEKWbimt-HDeqYRWsYJseXNncvMiXbuED7x1tXqN4uzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '拱宸桥/上塘', '丽水路66号远洋乐堤港商城2期1层B115号', 120.146659, 30.312742, 290, 0000013519, 0000009529, 49, '11:00-22:00', 1, '2025-02-22 19:17:15', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (5, 1, '海底捞火锅(水晶城购物中心店）', 1, 'https://img.meituan.net/msmerchant/054b5de0ba0b50c18a620cc37482129a45739.jpg,https://img.meituan.net/msmerchant/59b7eff9b60908d52bd4aea9ff356e6d145920.jpg,https://qcloud.dpfile.com/pc/Qe2PTEuvtJ5skpUXKKoW9OQ20qc7nIpHYEqJGBStJx0mpoyeBPQOJE4vOdYZwm9AuzFvxlbkWx5uwqY2qcjixFEuLYk00OmSS1IdNpm8K8sG4JN9RIm2mTKcbLtc2o2vmIU_8ZGOT1OjpJmLxG6urQ.jpg', '大关', '上塘路458号水晶城购物中心F6', 120.15778, 30.310633, 104, 0000004125, 0000002764, 49, '10:00-07:00', 1, '2025-02-22 19:20:58', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (6, 1, '幸福里老北京涮锅（丝联店）', 1, 'https://img.meituan.net/msmerchant/e71a2d0d693b3033c15522c43e03f09198239.jpg,https://img.meituan.net/msmerchant/9f8a966d60ffba00daf35458522273ca658239.jpg,https://img.meituan.net/msmerchant/ef9ca5ef6c05d381946fe4a9aa7d9808554502.jpg', '拱宸桥/上塘', '金华南路189号丝联166号', 120.148603, 30.318618, 130, 0000009531, 0000007324, 46, '11:00-13:50,17:00-20:50', 1, '2025-02-22 19:24:53', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (7, 1, '炉鱼(拱墅万达广场店)', 1, 'https://img.meituan.net/msmerchant/909434939a49b36f340523232924402166854.jpg,https://img.meituan.net/msmerchant/32fd2425f12e27db0160e837461c10303700032.jpg,https://img.meituan.net/msmerchant/f7022258ccb8dabef62a0514d3129562871160.jpg', '北部新城', '杭行路666号万达商业中心4幢2单元409室(铺位号4005)', 120.124691, 30.336819, 85, 0000002631, 0000001320, 47, '00:00-24:00', 1, '2025-02-22 19:40:52', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (8, 1, '浅草屋寿司（运河上街店）', 1, 'https://img.meituan.net/msmerchant/cf3dff697bf7f6e11f4b79c4e7d989e4591290.jpg,https://img.meituan.net/msmerchant/0b463f545355c8d8f021eb2987dcd0c8567811.jpg,https://img.meituan.net/msmerchant/c3c2516939efaf36c4ccc64b0e629fad587907.jpg', '运河上街', '拱墅区金华路80号运河上街B1', 120.150526, 30.325231, 88, 0000002406, 0000001206, 46, ' 11:00-21:30', 1, '2025-02-22 19:51:06', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (9, 1, '羊老三羊蝎子牛仔排北派炭火锅(运河上街店)', 1, 'https://p0.meituan.net/biztone/163160492_1624251899456.jpeg,https://img.meituan.net/msmerchant/e478eb16f7e31a7f8b29b5e3bab6de205500837.jpg,https://img.meituan.net/msmerchant/6173eb1d18b9d70ace7fdb3f2dd939662884857.jpg', '运河上街', '台州路2号运河上街购物中心F5', 120.150598, 30.325251, 101, 0000002763, 0000001363, 44, '11:00-21:30', 1, '2025-02-22 19:53:59', '2025-05-26 15:28:24');
INSERT INTO `tb_shop` VALUES (10, 1, '开乐迪KTV（运河上街店）', 2, 'https://p0.meituan.net/joymerchant/a575fd4adb0b9099c5c410058148b307-674435191.jpg,https://p0.meituan.net/merchantpic/68f11bf850e25e437c5f67decfd694ab2541634.jpg,https://p0.meituan.net/dpdeal/cb3a12225860ba2875e4ea26c6d14fcc197016.jpg', '运河上街', '台州路2号运河上街购物中心F4', 120.149093, 30.324666, 67, 0000026891, 0000000902, 37, '00:00-24:00', 1, '2025-02-22 20:25:16', '2025-02-22 20:25:16');
INSERT INTO `tb_shop` VALUES (11, 1, 'INLOVE KTV(水晶城店)', 2, 'https://p0.meituan.net/dpmerchantpic/53e74b200211d68988a4f02ae9912c6c1076826.jpg,https://qcloud.dpfile.com/pc/4iWtIvzLzwM2MGgyPu1PCDb4SWEaKqUeHm--YAt1EwR5tn8kypBcqNwHnjg96EvT_Gd2X_f-v9T8Yj4uLt25Gg.jpg,https://qcloud.dpfile.com/pc/WZsJWRI447x1VG2x48Ujgu7vwqksi_9WitdKI4j3jvIgX4MZOpGNaFtM93oSSizbGybIjx5eX6WNgCPvcASYAw.jpg', '水晶城', '上塘路458号水晶城购物中心6层', 120.15853, 30.310002, 75, 0000035977, 0000005684, 47, '11:30-06:00', 1, '2025-02-22 20:29:02', '2025-02-22 20:39:00');
INSERT INTO `tb_shop` VALUES (12, 1, '魅(杭州远洋乐堤港店)', 2, 'https://p0.meituan.net/dpmerchantpic/63833f6ba0393e2e8722420ef33f3d40466664.jpg,https://p0.meituan.net/dpmerchantpic/ae3c94cc92c529c4b1d7f68cebed33fa105810.png,', '远洋乐堤港', '丽水路58号远洋乐堤港F4', 120.14983, 30.31211, 88, 0000006444, 0000000235, 46, '10:00-02:00', 1, '2025-02-22 20:34:34', '2025-02-22 20:34:34');
INSERT INTO `tb_shop` VALUES (13, 1, '讴K拉量贩KTV(北城天地店)', 2, 'https://p1.meituan.net/merchantpic/598c83a8c0d06fe79ca01056e214d345875600.jpg,https://qcloud.dpfile.com/pc/HhvI0YyocYHRfGwJWqPQr34hRGRl4cWdvlNwn3dqghvi4WXlM2FY1te0-7pE3Wb9_Gd2X_f-v9T8Yj4uLt25Gg.jpg,https://qcloud.dpfile.com/pc/F5ZVzZaXFE27kvQzPnaL4V8O9QCpVw2nkzGrxZE8BqXgkfyTpNExfNG5CEPQX4pjGybIjx5eX6WNgCPvcASYAw.jpg', 'D32天阳购物中心', '湖州街567号北城天地5层', 120.130453, 30.327655, 58, 0000018997, 0000001857, 41, '12:00-02:00', 1, '2025-02-22 20:38:54', '2025-02-22 20:40:04');
INSERT INTO `tb_shop` VALUES (14, 1, '星聚会KTV(拱墅区万达店)', 2, 'https://p0.meituan.net/dpmerchantpic/f4cd6d8d4eb1959c3ea826aa05a552c01840451.jpg,https://p0.meituan.net/dpmerchantpic/2efc07aed856a8ab0fc75c86f4b9b0061655777.jpg,https://qcloud.dpfile.com/pc/zWfzzIorCohKT0bFwsfAlHuayWjI6DBEMPHHncmz36EEMU9f48PuD9VxLLDAjdoU_Gd2X_f-v9T8Yj4uLt25Gg.jpg', '北部新城', '杭行路666号万达广场C座1-2F', 120.128958, 30.337252, 60, 0000017771, 0000000685, 47, '10:00-22:00', 1, '2025-02-22 20:48:54', '2025-02-22 20:48:54');

-- ----------------------------
-- Table structure for tb_shop_type
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_type`;
CREATE TABLE `tb_shop_type`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(0) UNSIGNED NULL DEFAULT NULL COMMENT '顺序',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_shop_type
-- ----------------------------
INSERT INTO `tb_shop_type` VALUES (1, '美食', '/types/ms.png', 1, '2024-12-22 20:17:47', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (2, 'KTV', '/types/KTV.png', 2, '2024-12-22 20:18:27', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (3, '丽人·美发', '/types/lrmf.png', 3, '2024-12-22 20:18:48', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (4, '健身运动', '/types/jsyd.png', 10, '2024-12-22 20:19:04', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (5, '按摩·足疗', '/types/amzl.png', 5, '2024-12-22 20:19:27', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (6, '美容SPA', '/types/spa.png', 6, '2024-12-22 20:19:35', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (7, '亲子游乐', '/types/qzyl.png', 7, '2024-12-22 20:19:53', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (8, '酒吧', '/types/jiuba.png', 8, '2024-12-22 20:20:02', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (9, '轰趴馆', '/types/hpg.png', 9, '2024-12-22 20:20:08', '2025-02-23 11:24:31');
INSERT INTO `tb_shop_type` VALUES (10, '美睫·美甲', '/types/mjmj.png', 4, '2024-12-22 20:21:46', '2025-02-23 11:24:31');

-- ----------------------------
-- Table structure for tb_shop_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_shop_comment`;
CREATE TABLE `tb_shop_comment`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '关联的商店ID',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '评论用户ID',
  `order_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '关联的订单ID（确保评论来自已验证的购买）',
  `rating` int(0) UNSIGNED NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评论内容',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0=正常，1=用户隐藏，2=管理员隐藏',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商店评论表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_comment_report
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment_report`;
CREATE TABLE `tb_comment_report`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `comment_id` bigint(0) UNSIGNED NOT NULL COMMENT '被举报的评论ID',
  `reporter_id` bigint(0) UNSIGNED NOT NULL COMMENT '举报者ID（商家）',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '举报原因',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0=待处理，1=已处理',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评论举报表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_sign
-- ----------------------------
DROP TABLE IF EXISTS `tb_sign`;
CREATE TABLE `tb_sign`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户id',
  `year` year NOT NULL COMMENT '签到的年',
  `month` tinyint(0) NOT NULL COMMENT '签到的月',
  `date` date NOT NULL COMMENT '签到的日期',
  `is_backup` tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '是否补签',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称，默认是用户id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '人物头像',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniqe_key_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1026 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, '13686869696', '', '小鱼同学', '/imgs/blogs/blog1.jpg', '2025-02-22 10:27:19', '2025-05-26 15:24:31');
INSERT INTO `tb_user` VALUES (2, '13838411438', '', '可可今天不吃肉', '/imgs/icons/kkjtbcr.jpg', '2025-02-22 15:14:39', '2025-05-26 15:24:31');
INSERT INTO `tb_user` VALUES (4, '13456789011', '', 'user_slxaxy2au9f3tanffaxr', '', '2025-02-22 12:07:53', '2025-02-22 12:07:53');
INSERT INTO `tb_user` VALUES (5, '13456789001', '', '可爱多', '/imgs/icons/user5-icon.png', '2025-02-22 16:11:33', '2025-05-26 15:25:16');
INSERT INTO `tb_user` VALUES (1011, '13022578976', '', 'user_3kk24l8xk9', '', '2025-01-03 21:18:23', '2025-01-03 21:18:23');
INSERT INTO `tb_user` VALUES (1012, '13021256644', '', 'user_v5l4bi6pa2', '', '2025-01-03 21:21:55', '2025-01-03 21:21:55');
INSERT INTO `tb_user` VALUES (1013, '13322554466', '', 'user_qk0fzu63p8', '', '2025-01-04 19:26:28', '2025-01-04 19:26:28');
INSERT INTO `tb_user` VALUES (1014, '13223568877', '', 'user_z21u0pt7xx', '', '2025-01-04 20:30:55', '2025-01-04 20:30:55');
INSERT INTO `tb_user` VALUES (1015, '13011576645', '', 'user_ci4s1ejaw1', '', '2025-01-04 21:04:24', '2025-01-04 21:04:24');
INSERT INTO `tb_user` VALUES (1016, '13255697711', '', 'user_799ezgpaig', '', '2025-01-04 21:07:24', '2025-01-04 21:07:24');
INSERT INTO `tb_user` VALUES (1017, '13211112222', '', 'user_f68wcwci9x', '', '2025-01-04 21:11:31', '2025-01-04 21:11:31');
INSERT INTO `tb_user` VALUES (1018, '13300002222', '', 'user_m78cp1lgyi', '', '2025-01-04 21:21:52', '2025-01-04 21:21:52');
INSERT INTO `tb_user` VALUES (1019, '13011123333', '', 'user_oqqyvcwojc', '', '2025-01-04 21:23:55', '2025-01-04 21:23:55');
INSERT INTO `tb_user` VALUES (1020, '13222556655', '9zn69eoh', 'user_13222556655', '', '2025-01-04 23:16:55', '2025-01-04 23:16:55');
INSERT INTO `tb_user` VALUES (1021, '13312120222', '18xp28wr', 'user_13312120222', '', '2025-01-05 18:27:34', '2025-01-05 18:27:34');
INSERT INTO `tb_user` VALUES (1022, '13022221111', 'dmn95gjv', 'user_13022221111', '', '2025-01-10 18:30:39', '2025-01-10 18:30:39');
INSERT INTO `tb_user` VALUES (1023, '13011112222', 'u8n0bvrh', 'user_13011112222', '', '2025-01-10 18:31:13', '2025-01-10 18:31:13');
INSERT INTO `tb_user` VALUES (1024, '13322223333', 'wap3vwml', 'user_13322223333', '', '2025-01-31 16:00:21', '2025-01-31 16:00:21');
INSERT INTO `tb_user` VALUES (1025, '13322221111', 'l7bbsq1f', 'user_13322221111', '', '2025-05-21 09:54:25', '2025-05-21 09:54:25');
INSERT INTO `tb_user` VALUES (1026, '13311112222', 'yefoeyz5', 'user_13311112222', '', '2025-05-22 12:37:50', '2025-05-22 12:37:50');

-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '主键，用户id',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '城市名称',
  `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
  `fans` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
  `followee` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
  `gender` tinyint(0) UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `credits` int(0) UNSIGNED NULL DEFAULT 0 COMMENT '积分',
  `level` tinyint(0) UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级,0代表未开通会员',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_voucher
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher`;
CREATE TABLE `tb_voucher`  (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NULL DEFAULT NULL COMMENT '商铺id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '代金券标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '副标题',
  `rules` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '使用规则',
  `pay_value` bigint(0) UNSIGNED NOT NULL COMMENT '支付金额，单位是分。例如200代表2元',
  `actual_value` bigint(0) NOT NULL COMMENT '抵扣金额，单位是分。例如200代表2元',
  `min_amount` bigint(0) UNSIGNED NULL COMMENT '最低消费金额，单位为分',
  `valid_days` int(0) UNSIGNED NULL COMMENT '有效天数',
  `type` tinyint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0,普通券；1,秒杀券',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1,上架; 2,下架; 3,过期',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_voucher
-- ----------------------------
INSERT INTO `tb_voucher` VALUES (1, 1, '50元代金券', '周一至周日均可使用', '全场通用\\n无需预约\\n可无限叠加\\不兑现、不找零\\n仅限堂食', 4750, 5000, NULL, NULL, 0, 1, '2025-02-04 09:42:39', '2025-02-04 09:43:31');
INSERT INTO `tb_voucher` VALUES (12, 1, '100元代金券', '周一至周五均可使用', '全场通用\\n无需预约\\n可无限叠加\\不兑现、不找零\\n仅限堂食', 8000, 10000, NULL, NULL, 1, 1, '2025-01-18 20:20:59', '2025-01-18 20:20:59');

-- ----------------------------
-- Table structure for tb_voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_voucher_order`;
CREATE TABLE `tb_voucher_order`  (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '下单的用户id',
  `voucher_id` bigint(0) UNSIGNED NOT NULL COMMENT '购买的代金券id',
  `shop_id` bigint(0) UNSIGNED NULL COMMENT '关联的商铺id',
  `pay_type` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式 1：余额支付；2：支付宝；3：微信',
  `status` tinyint(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '下单时间',
  `pay_time` timestamp(0) NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` timestamp(0) NULL DEFAULT NULL COMMENT '核销时间',
  `refund_time` timestamp(0) NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of tb_voucher_order
-- ----------------------------
INSERT INTO `tb_voucher_order` VALUES (41914796295061506, 1023, 12, 1, 1, 1, '2025-04-23 22:50:49', NULL, NULL, NULL, '2025-04-23 22:50:49');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for tb_order_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_item`;
CREATE TABLE `tb_order_item` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint(0) UNSIGNED NOT NULL COMMENT '订单ID',
  `product_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品ID',
  `product_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品标题',
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品图片',
  `product_price` bigint(0) UNSIGNED NOT NULL COMMENT '商品单价，单位为分',
  `quantity` int(0) UNSIGNED NOT NULL COMMENT '购买数量',
  `specifications` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品规格信息JSON',
  `subtotal_amount` bigint(0) UNSIGNED NOT NULL COMMENT '小计金额，单位为分',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单项信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_order
-- ----------------------------
DROP TABLE IF EXISTS `tb_order`;
CREATE TABLE `tb_order` (
  `id` bigint(0) NOT NULL COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺ID',
  `total_amount` bigint(0) UNSIGNED NOT NULL COMMENT '订单总金额，单位为分',
  `status` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '订单状态：1-待支付，2-已支付，3-已完成，4-已取消，5-已退款',
  `pay_type` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '支付方式：1-余额支付，2-支付宝，3-微信',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配送地址',
  `pay_time` timestamp(0) NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_product
-- ----------------------------
DROP TABLE IF EXISTS `tb_product`;
CREATE TABLE `tb_product` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺ID',
  `category_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品分类ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品标题',
  `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品描述',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品图片，多个以逗号分隔',
  `price` bigint(0) UNSIGNED NOT NULL COMMENT '商品价格，单位为分',
  `stock` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品库存',
  `sold` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品销量',
  `status` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '商品状态：0-下架，1-上架',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_product_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_category`;
CREATE TABLE `tb_product_category` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类图标',
  `sort` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品分类表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_product_spec
-- ----------------------------
DROP TABLE IF EXISTS `tb_product_spec`;
CREATE TABLE `tb_product_spec` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规格名称',
  `values` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规格值，JSON数组格式',
  `required` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否必选：0-否，1-是',
  `sort` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品规格表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_merchant
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant`;
CREATE TABLE `tb_merchant` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商家名称',
  `account` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码，加密存储',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '头像',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '商家描述',
  `status` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '商家状态：0-待审核，1-审核通过，2-审核拒绝',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_account`(`account`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商家信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_merchant_qualification
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_qualification`;
CREATE TABLE `tb_merchant_qualification` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) UNSIGNED NOT NULL COMMENT '商家ID',
  `business_license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '营业执照图片路径',
  `id_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '法人身份证图片路径',
  `business_permit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经营许可证图片路径',
  `food_service_permit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '餐饮服务许可证图片路径',
  `other_qualifications` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '其他资质证明',
  `status` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
  `comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商家资质信息表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_merchant_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_log`;
CREATE TABLE `tb_merchant_log` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `merchant_id` bigint(0) UNSIGNED NOT NULL COMMENT '商家ID',
  `type` int(0) UNSIGNED NOT NULL COMMENT '操作类型：1-登录，2-新增，3-修改，4-删除，5-查询',
  `module` int(0) UNSIGNED NOT NULL COMMENT '操作模块：1-商家账户，2-店铺，3-商品，4-优惠券，5-订单',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作描述',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作IP',
  `result` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '操作结果：0-失败，1-成功',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商家操作日志表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_merchant_statistics
-- ----------------------------
DROP TABLE IF EXISTS `tb_merchant_statistics`;
CREATE TABLE `tb_merchant_statistics` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺ID',
  `statistics_date` date NOT NULL COMMENT '统计日期',
  `sales_amount` bigint(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '销售额，单位为分',
  `order_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '订单数量',
  `new_user_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '新用户数量',
  `active_user_count` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '活跃用户数量',
  `page_views` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '访问量（PV）',
  `unique_visitors` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '访客数（UV）',
  `conversion_rate` double(5, 2) NOT NULL DEFAULT 0.00 COMMENT '转化率（百分比）',
  `type` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '统计类型：1-日报，2-周报，3-月报',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shop_date_type`(`shop_id`, `statistics_date`, `type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商家统计数据表' ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for tb_order_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_order_comment`;
CREATE TABLE `tb_order_comment` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `shop_id` bigint(0) UNSIGNED NOT NULL COMMENT '商铺ID',
  `order_id` bigint(0) UNSIGNED NOT NULL COMMENT '订单ID',
  `score` int(0) UNSIGNED NOT NULL DEFAULT 5 COMMENT '评分，1-5分',
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价内容',
  `images` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评价图片，多个以逗号分隔',
  `reply_content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商家回复内容',
  `reply_time` timestamp(0) NULL DEFAULT NULL COMMENT '商家回复时间',
  `status` int(0) UNSIGNED NOT NULL DEFAULT 0 COMMENT '评价状态：0-未回复，1-已回复',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单评价表' ROW_FORMAT = Compact;

-- ----------------------------
-- 添加索引以提高查询性能
-- ----------------------------
ALTER TABLE `tb_product` ADD INDEX `idx_product_title` (`title`(191)) COMMENT '商品标题索引，用于搜索';
ALTER TABLE `tb_order_item` ADD INDEX `idx_product_id` (`product_id`) COMMENT '商品ID索引，用于关联查询';
ALTER TABLE `tb_product` ADD INDEX `idx_category_status` (`category_id`,`status`) COMMENT '分类和状态组合索引，用于筛选商品';

-- ----------------------------
-- Records of tb_product_category
-- ----------------------------
INSERT INTO `tb_product_category` VALUES (1, 1, '热销套餐', '/imgs/categories/hot.png', 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_category` VALUES (2, 1, '人气单品', '/imgs/categories/popular.png', 2, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_category` VALUES (3, 1, '甜品饮料', '/imgs/categories/dessert.png', 3, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_category` VALUES (4, 2, '烤肉拼盘', '/imgs/categories/bbq.png', 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_category` VALUES (5, 2, '招牌菜', '/imgs/categories/signature.png', 2, '2025-01-10 12:00:00', '2025-01-10 12:00:00');

-- ----------------------------
-- Records of tb_product
-- ----------------------------
INSERT INTO `tb_product` VALUES (1, 1, 1, '香辣鸡腿堡套餐', '香辣鸡腿堡+薯条+可乐', '/imgs/products/p1.jpg,/imgs/products/p1-2.jpg', 2990, 100, 42, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (2, 1, 1, '双层牛肉堡套餐', '双层牛肉堡+薯条+可乐', '/imgs/products/p2.jpg', 3290, 100, 35, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (3, 1, 2, '脆皮炸鸡', '黄金脆皮，外酥里嫩', '/imgs/products/p3.jpg', 1590, 150, 89, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (4, 1, 2, '薯条', '美式薯条，外酥里软', '/imgs/products/p4.jpg', 990, 200, 156, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (5, 1, 3, '冰淇淋', '香草口味冰淇淋', '/imgs/products/p5.jpg', 590, 200, 95, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (6, 1, 3, '可乐', '冰镇可口可乐', '/imgs/products/p6.jpg', 590, 300, 258, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (7, 2, 4, '招牌烤肉拼盘', '多种肉类拼盘，满足各种口味需求', '/imgs/products/p7.jpg', 9900, 50, 28, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product` VALUES (8, 2, 5, '招牌冷面', '爽口冷面，夏日必备', '/imgs/products/p8.jpg', 2800, 80, 63, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');

-- ----------------------------
-- Records of tb_product_spec
-- ----------------------------
INSERT INTO `tb_product_spec` VALUES (1, 1, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', 1, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_spec` VALUES (2, 1, '甜度', '[\"无糖\",\"微糖\",\"半糖\",\"全糖\"]', 1, 2, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_spec` VALUES (3, 2, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', 1, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_spec` VALUES (4, 3, '辣度', '[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]', 1, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_spec` VALUES (5, 3, '部位', '[\"鸡腿\",\"鸡翅\",\"鸡胸\",\"混合\"]', 1, 2, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_product_spec` VALUES (6, 7, '熟度', '[\"三分熟\",\"五分熟\",\"七分熟\",\"全熟\"]', 1, 1, '2025-01-10 12:00:00', '2025-01-10 12:00:00');

-- ----------------------------
-- Records of tb_merchant
-- ----------------------------
INSERT INTO `tb_merchant` VALUES (1, '美食快餐连锁店', 'merchant1', 'e10adc3949ba59abbe56e057f20f883e', '13800138001', '/imgs/merchants/m1.jpg', '提供各类美食快餐，满足您的味蕾需求', 1, NULL, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_merchant` VALUES (2, '烤肉店', 'merchant2', 'e10adc3949ba59abbe56e057f20f883e', '13800138002', '/imgs/merchants/m2.jpg', '正宗韩式烤肉，味道纯正', 1, NULL, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_merchant` VALUES (3, '咖啡甜品店', 'merchant3', 'e10adc3949ba59abbe56e057f20f883e', '13800138003', '/imgs/merchants/m3.jpg', '提供各类咖啡和甜品，温馨舒适的环境', 0, NULL, '2025-01-10 12:00:00', '2025-01-10 12:00:00');

-- ----------------------------
-- Records of tb_merchant_qualification
-- ----------------------------
INSERT INTO `tb_merchant_qualification` VALUES (1, 1, '/imgs/qualifications/license1.jpg', '/imgs/qualifications/idcard1.jpg', '/imgs/qualifications/permit1.jpg', '/imgs/qualifications/food1.jpg', NULL, 1, '审核通过', '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_merchant_qualification` VALUES (2, 2, '/imgs/qualifications/license2.jpg', '/imgs/qualifications/idcard2.jpg', '/imgs/qualifications/permit2.jpg', '/imgs/qualifications/food2.jpg', NULL, 1, '审核通过', '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_merchant_qualification` VALUES (3, 3, '/imgs/qualifications/license3.jpg', '/imgs/qualifications/idcard3.jpg', '/imgs/qualifications/permit3.jpg', NULL, NULL, 0, '资料审核中', '2025-01-10 12:00:00', '2025-01-10 12:00:00');

-- ----------------------------
-- Records of tb_order
-- ----------------------------
INSERT INTO `tb_order` VALUES (100001, 1, 1, 4580, 2, 1, '杭州市上城区XX路XX号', '2025-01-10 12:30:00', '2025-01-10 12:00:00', '2025-01-10 12:30:00');
INSERT INTO `tb_order` VALUES (100002, 2, 1, 2580, 2, 2, '杭州市西湖区XX路XX号', '2025-01-10 13:15:00', '2025-01-10 13:00:00', '2025-01-10 13:15:00');
INSERT INTO `tb_order` VALUES (100003, 1, 2, 9900, 3, 3, '杭州市拱墅区XX路XX号', '2025-01-10 18:20:00', '2025-01-10 18:00:00', '2025-01-10 19:30:00');
INSERT INTO `tb_order` VALUES (100004, 2, 2, 2800, 2, 1, '杭州市滨江区XX路XX号', '2025-01-10 19:10:00', '2025-01-10 19:00:00', '2025-01-10 19:10:00');
INSERT INTO `tb_order` VALUES (100005, 1023, 1, 3290, 1, 1, '杭州市余杭区XX路XX号', NULL, '2025-01-10 20:00:00', '2025-01-10 20:00:00');

-- ----------------------------
-- Records of tb_order_item
-- ----------------------------
INSERT INTO `tb_order_item` VALUES (1, 100001, 1, '香辣鸡腿堡套餐', '/imgs/products/p1.jpg', 2990, 1, '{\"辣度\":\"中辣\",\"甜度\":\"微糖\"}', 2990, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_order_item` VALUES (2, 100001, 4, '薯条', '/imgs/products/p4.jpg', 990, 1, NULL, 990, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_order_item` VALUES (3, 100001, 6, '可乐', '/imgs/products/p6.jpg', 590, 1, NULL, 590, '2025-01-10 12:00:00', '2025-01-10 12:00:00');
INSERT INTO `tb_order_item` VALUES (4, 100002, 3, '脆皮炸鸡', '/imgs/products/p3.jpg', 1590, 1, '{\"辣度\":\"微辣\",\"部位\":\"鸡腿\"}', 1590, '2025-01-10 13:00:00', '2025-01-10 13:00:00');
INSERT INTO `tb_order_item` VALUES (5, 100002, 6, '可乐', '/imgs/products/p6.jpg', 590, 1, NULL, 590, '2025-01-10 13:00:00', '2025-01-10 13:00:00');
INSERT INTO `tb_order_item` VALUES (6, 100002, 5, '冰淇淋', '/imgs/products/p5.jpg', 590, 1, NULL, 590, '2025-01-10 13:00:00', '2025-01-10 13:00:00');
INSERT INTO `tb_order_item` VALUES (7, 100003, 7, '招牌烤肉拼盘', '/imgs/products/p7.jpg', 9900, 1, '{\"熟度\":\"五分熟\"}', 9900, '2025-01-10 18:00:00', '2025-01-10 18:00:00');
INSERT INTO `tb_order_item` VALUES (8, 100004, 8, '招牌冷面', '/imgs/products/p8.jpg', 2800, 1, NULL, 2800, '2025-01-10 19:00:00', '2025-01-10 19:00:00');
INSERT INTO `tb_order_item` VALUES (9, 100005, 2, '双层牛肉堡套餐', '/imgs/products/p2.jpg', 3290, 1, '{\"辣度\":\"微辣\"}', 3290, '2025-01-10 20:00:00', '2025-01-10 20:00:00');

-- ----------------------------
-- Records of tb_merchant_statistics
-- ----------------------------
INSERT INTO `tb_merchant_statistics` VALUES (1, 1, '2025-01-10', 715000, 156, 23, 89, 1200, 450, 13.00, 1, '2025-01-10 23:59:59', '2025-01-10 23:59:59');
INSERT INTO `tb_merchant_statistics` VALUES (2, 2, '2025-01-10', 427000, 93, 15, 62, 980, 320, 9.48, 1, '2025-01-10 23:59:59', '2025-01-10 23:59:59');
INSERT INTO `tb_merchant_statistics` VALUES (3, 1, '2025-01-11', 683000, 142, 18, 76, 1150, 410, 12.34, 1, '2025-01-11 23:59:59', '2025-01-11 23:59:59');
INSERT INTO `tb_merchant_statistics` VALUES (4, 2, '2025-01-11', 395000, 85, 12, 54, 920, 285, 9.23, 1, '2025-01-11 23:59:59', '2025-01-11 23:59:59');

-- ----------------------------
-- Records of tb_merchant_log
-- ----------------------------
INSERT INTO `tb_merchant_log` VALUES (1, 1, 1, 1, '商家登录系统', '192.168.1.100', 1, '2025-01-10 09:00:00');
INSERT INTO `tb_merchant_log` VALUES (2, 1, 3, 3, '修改商品价格', '192.168.1.100', 1, '2025-01-10 09:30:00');
INSERT INTO `tb_merchant_log` VALUES (3, 1, 2, 3, '新增商品', '192.168.1.100', 1, '2025-01-10 10:15:00');
INSERT INTO `tb_merchant_log` VALUES (4, 1, 5, 5, '查询订单列表', '192.168.1.100', 1, '2025-01-10 11:20:00');
INSERT INTO `tb_merchant_log` VALUES (5, 2, 1, 1, '商家登录系统', '192.168.1.101', 1, '2025-01-10 08:45:00');
INSERT INTO `tb_merchant_log` VALUES (6, 2, 2, 3, '新增商品', '192.168.1.101', 1, '2025-01-10 09:20:00');
INSERT INTO `tb_merchant_log` VALUES (7, 2, 5, 5, '查询订单列表', '192.168.1.101', 1, '2025-01-10 14:30:00');

-- ----------------------------
-- Records of tb_order_comment
-- ----------------------------
INSERT INTO `tb_order_comment` VALUES (1, 1, 1, 100001, 5, '味道很好，服务也不错', '/imgs/comments/c1.jpg,/imgs/comments/c2.jpg', '感谢您的好评，欢迎再次光临！', '2025-01-10 14:30:00', 1, '2025-01-10 13:30:00', '2025-01-10 14:30:00');
INSERT INTO `tb_order_comment` VALUES (2, 2, 1, 100002, 4, '食物还不错，就是送餐有点慢', '/imgs/comments/c3.jpg', '非常抱歉送餐延迟，我们会改进配送速度', '2025-01-10 15:00:00', 1, '2025-01-10 14:00:00', '2025-01-10 15:00:00');
INSERT INTO `tb_order_comment` VALUES (3, 1, 2, 100003, 5, '烤肉非常好吃，下次还会再来', '/imgs/comments/c4.jpg,/imgs/comments/c5.jpg', '感谢您的支持，期待您的再次光临！', '2025-01-10 20:15:00', 1, '2025-01-10 19:45:00', '2025-01-10 20:15:00');
INSERT INTO `tb_order_comment` VALUES (4, 2, 2, 100004, 4, '冷面很爽口，就是有点咸', NULL, '感谢您的反馈，我们会调整口味', '2025-01-10 20:30:00', 1, '2025-01-10 19:50:00', '2025-01-10 20:30:00');

-- ----------------------------
-- Records of tb_shop_comment
-- ----------------------------
INSERT INTO `tb_shop_comment` VALUES (1, 1, 1, 100001, 5, '服务很好，环境不错，菜品也很棒！', 0, '2025-01-10 14:30:00', '2025-01-10 14:30:00');
INSERT INTO `tb_shop_comment` VALUES (2, 1, 2, 100002, 4, '味道还可以，就是上菜有点慢', 0, '2025-01-10 15:20:00', '2025-01-10 15:20:00');
INSERT INTO `tb_shop_comment` VALUES (3, 2, 1, 100003, 5, '烤肉非常棒，下次还会再来！', 0, '2025-01-10 20:15:00', '2025-01-10 20:15:00');
INSERT INTO `tb_shop_comment` VALUES (4, 2, 2, 100004, 3, '价格有点贵，性价比一般', 0, '2025-01-10 21:00:00', '2025-01-10 21:00:00');
INSERT INTO `tb_shop_comment` VALUES (5, 3, 1023, NULL, 4, '环境很好，适合聚餐', 0, '2025-01-11 12:30:00', '2025-01-11 12:30:00');

-- ----------------------------
-- Records of tb_comment_report
-- ----------------------------
INSERT INTO `tb_comment_report` VALUES (1, 4, 1, '评论内容不实，恶意差评', 0, '2025-01-11 09:00:00', '2025-01-11 09:00:00');
INSERT INTO `tb_comment_report` VALUES (2, 2, 1, '评论涉嫌恶意诋毁', 1, '2025-01-11 10:30:00', '2025-01-11 15:20:00');

-- ----------------------------
-- Records of tb_sign
-- ----------------------------
INSERT INTO `tb_sign` VALUES (1, 1, 2025, 1, '2025-01-01', 0);
INSERT INTO `tb_sign` VALUES (2, 1, 2025, 1, '2025-01-02', 0);
INSERT INTO `tb_sign` VALUES (3, 1, 2025, 1, '2025-01-03', 0);
INSERT INTO `tb_sign` VALUES (4, 1, 2025, 1, '2025-01-04', 1);
INSERT INTO `tb_sign` VALUES (5, 1, 2025, 1, '2025-01-05', 0);
INSERT INTO `tb_sign` VALUES (6, 2, 2025, 1, '2025-01-01', 0);
INSERT INTO `tb_sign` VALUES (7, 2, 2025, 1, '2025-01-02', 0);
INSERT INTO `tb_sign` VALUES (8, 2, 2025, 1, '2025-01-03', 0);
INSERT INTO `tb_sign` VALUES (9, 2, 2025, 1, '2025-01-04', 0);
INSERT INTO `tb_sign` VALUES (10, 2, 2025, 1, '2025-01-05', 0);

-- ----------------------------
-- 管理员模块相关表
-- ----------------------------

-- ----------------------------
-- Table structure for tb_admin_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_user`;
CREATE TABLE `tb_admin_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '加密后的密码',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态 (0-正常, 1-禁用)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 (0-未删除, 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_role`;
CREATE TABLE `tb_admin_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称 (如：超级管理员, 用户管理员, 内容审核员)',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码 (如：SUPER_ADMIN, USER_MANAGER, CONTENT_AUDITOR)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态 (0-正常, 1-禁用)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 (0-未删除, 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_name`(`name`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_admin_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_permission`;
CREATE TABLE `tb_admin_permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限名称 (例如：查询用户列表, 禁用用户, 审核商品)',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限标识 (例如：user:list, user:disable, product:audit)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限描述 (可选，用于进一步说明该权限的作用)',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态 (0-正常, 1-禁用)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` tinyint(0) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 (0-未删除, 1-已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_admin_user_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_user_role`;
CREATE TABLE `tb_admin_user_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `admin_user_id` bigint(0) NOT NULL COMMENT '管理员ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_user_id`(`admin_user_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  CONSTRAINT `fk_admin_user_role_user` FOREIGN KEY (`admin_user_id`) REFERENCES `tb_admin_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_admin_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `tb_admin_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员-角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_admin_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_role_permission`;
CREATE TABLE `tb_admin_role_permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(0) NOT NULL COMMENT '权限ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id_perm`(`role_id`) USING BTREE,
  INDEX `idx_permission_id_role`(`permission_id`) USING BTREE,
  CONSTRAINT `fk_admin_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `tb_admin_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_admin_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `tb_admin_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_admin_audit_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_audit_log`;
CREATE TABLE `tb_admin_audit_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_user_id` bigint(0) NOT NULL COMMENT '操作管理员ID',
  `admin_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作管理员用户名',
  `action_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作名称 (如：禁用用户)',
  `request_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求URI',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法 (GET, POST, PUT, DELETE)',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数 (JSON格式)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP',
  `duration_ms` bigint(0) NULL DEFAULT NULL COMMENT '操作耗时 (毫秒)',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '操作状态 (0-成功, 1-失败)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息 (如果操作失败)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_user_id_log`(`admin_user_id`) USING BTREE,
  INDEX `idx_create_time_log`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 购物车模块相关表
-- ----------------------------

-- ----------------------------
-- Table structure for tb_cart
-- ----------------------------
DROP TABLE IF EXISTS `tb_cart`;
CREATE TABLE `tb_cart` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车主表';

-- ----------------------------
-- Table structure for tb_cart_item
-- ----------------------------
DROP TABLE IF EXISTS `tb_cart_item`;
CREATE TABLE `tb_cart_item` (
  `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cart_id` bigint(0) UNSIGNED NOT NULL COMMENT '购物车ID',
  `product_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '商品数量',
  `specifications` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品规格信息JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_product` (`cart_id`, `product_id`),
  KEY `idx_cart_id` (`cart_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车项表';

-- ----------------------------
-- Records of tb_cart
-- ----------------------------
INSERT INTO `tb_cart` VALUES (1, 1010, '2024-12-22 10:00:00', '2024-12-22 10:00:00');
INSERT INTO `tb_cart` VALUES (2, 1011, '2024-12-22 11:00:00', '2024-12-22 11:00:00');
INSERT INTO `tb_cart` VALUES (3, 1012, '2024-12-22 12:00:00', '2024-12-22 12:00:00');

-- ----------------------------
-- Records of tb_cart_item
-- ----------------------------
-- 用户1010的购物车项目
INSERT INTO `tb_cart_item` VALUES (1, 1, 1, 2, NULL, '2024-12-22 10:00:00');
INSERT INTO `tb_cart_item` VALUES (2, 1, 3, 1, NULL, '2024-12-22 10:05:00');
INSERT INTO `tb_cart_item` VALUES (3, 1, 4, 3, NULL, '2024-12-22 10:10:00');

-- 用户1011的购物车项目
INSERT INTO `tb_cart_item` VALUES (4, 2, 2, 1, NULL, '2024-12-22 11:00:00');
INSERT INTO `tb_cart_item` VALUES (5, 2, 5, 2, NULL, '2024-12-22 11:05:00');

-- 用户1012的购物车项目
INSERT INTO `tb_cart_item` VALUES (6, 3, 1, 1, NULL, '2024-12-22 12:00:00');
INSERT INTO `tb_cart_item` VALUES (7, 3, 6, 2, NULL, '2024-12-22 12:05:00');
INSERT INTO `tb_cart_item` VALUES (8, 3, 7, 1, NULL, '2024-12-22 12:10:00');

SET FOREIGN_KEY_CHECKS = 1;

