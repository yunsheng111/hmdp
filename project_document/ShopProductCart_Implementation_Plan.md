# 商铺详情页商品购物车功能实施计划

## 项目概述

为商铺详情页添加商品展示和购物车功能，实现完整的商品浏览、选择、加入购物车和结算流程。

**需求描述**: 在商铺详情页的优惠券和评论之间添加商品功能，分为两栏：左侧是商品类型，右侧是商品（包含标题、价格、库存），选择商品后在下方显示购物车，支持购物车结算。

**技术方案**: 完整购物车系统，包含后端API和数据持久化

## 实施计划

### 第一阶段：购物车后端基础设施

#### 1.1 数据库表设计与创建
- **文件**: `src/main/resources/db/hmdp.sql`
- **操作**: 添加购物车相关表DDL
- **表结构**:
  ```sql
  -- 购物车主表
  CREATE TABLE `tb_cart` (
    `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint(0) UNSIGNED NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
  );
  
  -- 购物车项表
  CREATE TABLE `tb_cart_item` (
    `id` bigint(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `cart_id` bigint(0) UNSIGNED NOT NULL COMMENT '购物车ID',
    `product_id` bigint(0) UNSIGNED NOT NULL COMMENT '商品ID',
    `quantity` int(0) UNSIGNED NOT NULL DEFAULT 1 COMMENT '商品数量',
    `specifications` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品规格信息JSON',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cart_product` (`cart_id`, `product_id`)
  );
  ```
- **预期结果**: 数据库表创建成功

#### 1.2 创建购物车实体类
- **文件**: 
  - `src/main/java/com/hmdp/entity/Cart.java`
  - `src/main/java/com/hmdp/entity/CartItem.java`
- **操作**: 创建与数据库表对应的实体类，使用MyBatis-Plus注解
- **预期结果**: 实体类与数据库表映射正确

#### 1.3 创建购物车DTO类
- **文件**:
  - `src/main/java/com/hmdp/dto/CartDTO.java` - 购物车响应DTO
  - `src/main/java/com/hmdp/dto/CartItemDTO.java` - 购物车项DTO
  - `src/main/java/com/hmdp/dto/AddToCartDTO.java` - 添加到购物车请求DTO
- **操作**: 创建数据传输对象，包含必要的验证注解
- **预期结果**: DTO类设计合理，满足API需求

#### 1.4 创建购物车Mapper接口
- **文件**:
  - `src/main/java/com/hmdp/mapper/CartMapper.java`
  - `src/main/java/com/hmdp/mapper/CartItemMapper.java`
- **操作**: 创建MyBatis数据访问接口，继承BaseMapper
- **预期结果**: 数据访问层接口完整

#### 1.5 创建购物车Service层
- **文件**:
  - `src/main/java/com/hmdp/service/ICartService.java`
  - `src/main/java/com/hmdp/service/impl/CartServiceImpl.java`
- **操作**: 实现购物车业务逻辑
- **核心功能**:
  - `addToCart(AddToCartDTO dto)` - 添加商品到购物车
  - `updateCartItemQuantity(Long itemId, Integer quantity)` - 更新商品数量
  - `removeCartItem(Long itemId)` - 删除购物车项
  - `getCartByUserId(Long userId)` - 获取用户购物车
  - `clearCart(Long userId)` - 清空购物车
- **预期结果**: 业务逻辑完整，支持所有购物车操作

#### 1.6 创建购物车Controller
- **文件**: `src/main/java/com/hmdp/controller/CartController.java`
- **操作**: 创建购物车REST API
- **接口设计**:
  - `GET /cart` - 获取当前用户购物车
  - `POST /cart/item` - 添加商品到购物车
  - `PUT /cart/item/{id}` - 更新购物车项数量
  - `DELETE /cart/item/{id}` - 删除购物车项
  - `DELETE /cart` - 清空购物车
- **预期结果**: API接口完整，可通过Postman测试

### 第二阶段：商品展示功能

#### 2.1 扩展商品相关API
- **检查文件**: 
  - `src/main/java/com/hmdp/controller/ProductController.java`
  - `src/main/java/com/hmdp/service/impl/ProductServiceImpl.java`
- **操作**: 确保有以下API
  - `GET /product/categories/{shopId}` - 获取商铺商品分类
  - `GET /product/shop/{shopId}` - 获取商铺商品列表（支持分类筛选）
- **预期结果**: 能够获取商铺的商品分类和商品列表

#### 2.2 修改商铺详情页HTML结构
- **文件**: `hmdp-front/nginx-1.18.0/html/hmdp/user-shop-detail.html`
- **操作**: 在优惠券区域和评论区域之间添加商品展示区域
- **布局设计**:
  ```html
  <!-- 在第184行（优惠券区域后）添加 -->
  <div class="shop-divider"></div>
  <div class="shop-products">
    <!-- 商品区域标题 -->
    <div class="products-header">
      <h3>店内商品</h3>
    </div>
    
    <!-- 商品展示区域 -->
    <div class="products-container">
      <!-- 左侧：商品分类 -->
      <div class="product-categories">
        <!-- 分类列表 -->
      </div>
      
      <!-- 右侧：商品列表 -->
      <div class="product-list">
        <!-- 商品网格 -->
      </div>
    </div>
    
    <!-- 购物车区域 -->
    <div class="shopping-cart" v-if="cartItems.length > 0">
      <!-- 购物车内容 -->
    </div>
  </div>
  ```
- **预期结果**: 页面布局符合需求，为商品展示预留空间

### 第三阶段：购物车前端功能

#### 3.1 添加Vue数据和方法
- **文件**: `user-shop-detail.html`
- **操作**: 扩展Vue实例的data和methods
- **新增数据**:
  ```javascript
  // 商品相关数据
  productCategories: [],
  products: [],
  selectedCategoryId: null,
  
  // 购物车相关数据
  cartItems: [],
  cartVisible: false,
  cartTotal: 0
  ```
- **新增方法**:
  - `loadProductCategories()` - 加载商品分类
  - `loadProducts(categoryId)` - 加载商品列表
  - `addToCart(product)` - 添加商品到购物车
  - `updateCartItemQuantity(itemId, quantity)` - 更新购物车数量
  - `removeFromCart(itemId)` - 从购物车删除商品
  - `calculateCartTotal()` - 计算购物车总价
- **预期结果**: Vue实例支持商品和购物车操作

#### 3.2 实现商品展示组件
- **文件**: `user-shop-detail.html`
- **操作**: 添加商品分类和商品列表的HTML模板
- **功能**: 
  - 商品分类切换
  - 商品信息展示（图片、标题、价格、库存）
  - 添加到购物车按钮
- **预期结果**: 商品信息正确展示，支持分类筛选

#### 3.3 实现购物车组件
- **文件**: `user-shop-detail.html`
- **操作**: 添加购物车的HTML模板和交互逻辑
- **功能**:
  - 购物车商品列表展示
  - 商品数量增减
  - 商品删除
  - 总价实时计算
  - 结算按钮
- **预期结果**: 购物车功能完整，用户体验良好

### 第四阶段：结算功能

#### 4.1 实现购物车结算
- **文件**: 购物车相关代码
- **操作**: 实现从购物车到订单的转换逻辑
- **功能**:
  - 购物车商品验证（库存、价格、状态）
  - 生成订单和订单项
  - 清空购物车
  - 跳转到支付页面
- **预期结果**: 能够从购物车成功生成订单

#### 4.2 集成测试
- **操作**: 端到端功能测试
- **测试场景**:
  1. 浏览商品分类和商品列表
  2. 添加商品到购物车
  3. 修改购物车商品数量
  4. 删除购物车商品
  5. 购物车结算下单
- **预期结果**: 完整流程正常运行，无明显bug

## 技术要点

### 后端技术要点
1. **用户身份验证**: 利用现有的UserHolder获取当前用户
2. **数据一致性**: 确保购物车与商品库存的一致性检查
3. **并发控制**: 处理多用户同时操作购物车的情况
4. **异常处理**: 完善的异常处理和错误信息返回

### 前端技术要点
1. **响应式设计**: 确保在不同设备上的良好体验
2. **实时更新**: 购物车状态的实时同步
3. **用户体验**: 流畅的交互动画和反馈
4. **错误处理**: 友好的错误提示和处理

### 性能优化
1. **缓存策略**: 商品信息的合理缓存
2. **懒加载**: 商品图片的懒加载
3. **防抖处理**: 购物车数量修改的防抖
4. **批量操作**: 减少不必要的API调用

## 风险控制

### 数据安全
1. **权限验证**: 确保用户只能操作自己的购物车
2. **数据验证**: 严格的输入参数验证
3. **SQL注入防护**: 使用参数化查询

### 业务风险
1. **库存控制**: 防止超卖情况
2. **价格一致性**: 确保下单时价格的准确性
3. **商品状态**: 处理商品下架后的购物车清理

### 技术风险
1. **数据库设计**: 预留扩展字段，支持未来功能扩展
2. **API兼容性**: 保持向后兼容
3. **前端兼容性**: 确保主流浏览器兼容

## 验收标准

### 功能验收
- [ ] 商品分类正确展示
- [ ] 商品列表按分类筛选正常
- [ ] 添加商品到购物车成功
- [ ] 购物车数量修改正常
- [ ] 购物车商品删除正常
- [ ] 购物车总价计算正确
- [ ] 购物车结算生成订单成功

### 性能验收
- [ ] 页面加载时间 < 3秒
- [ ] 购物车操作响应时间 < 1秒
- [ ] 支持并发用户数 > 100

### 用户体验验收
- [ ] 界面美观，符合整体设计风格
- [ ] 操作流程简单直观
- [ ] 错误提示友好明确
- [ ] 移动端适配良好

---

**创建时间**: 2024-12-22
**负责人**: AI Assistant
**预计完成时间**: 根据开发进度确定
**当前状态**: 计划阶段
