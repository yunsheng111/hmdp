# HMDP项目后端结构

## 项目概览

HMDP (黑马点评) 是一个点评类应用的后端系统，基于Spring Boot和MyBatis Plus构建。系统包含用户管理、商户管理、商品管理、订单管理、点评管理等多个模块。

## 项目结构树

```
src/main/java/com/hmdp/
├── HmDianPingApplication.java (应用程序入口)
├── common/ (通用组件)
│   ├── AdminLoginInterceptor.java (管理员登录拦截器)
│   ├── LoginInterceptor.java (用户登录拦截器)
│   ├── MerchantLoginInterceptor.java (商家登录拦截器)
│   ├── MerchantRefreshTokenInterceptor.java (商家Token刷新拦截器)
│   ├── RefreshTokenInterceptor.java (Token刷新拦截器)
│   └── Result.java (统一响应结果)
├── config/ (配置类)
│   ├── CorsConfig.java (跨域配置)
│   ├── Knife4jConfig.java (API文档配置)
│   ├── MvcConfig.java (MVC配置)
│   ├── MybatisConfig.java (MyBatis配置)
│   ├── RedisConfig.java (Redis配置)
│   ├── RedissonConfig.java (Redisson配置)
│   ├── SecurityConfig.java (安全配置)
│   ├── StpInterfaceImpl.java (权限接口实现)
│   └── WebExceptionAdvice.java (全局异常处理)
├── controller/ (控制器层)
│   ├── AdminController.java (管理员控制器)
│   ├── AdminMerchantController.java (管理员-商家控制器)
│   ├── AdminMerchantShopController.java (管理员-商家-店铺控制器)
│   ├── AdminShopTypeController.java (管理员-店铺类型控制器)
│   ├── AdminStatsController.java (管理员-统计控制器)
│   ├── AdminVoucherController.java (管理员-优惠券控制器)
│   ├── BlogCommentsController.java (博客评论控制器)
│   ├── BlogController.java (博客控制器)
│   ├── CartController.java (购物车控制器)
│   ├── CommentReportController.java (评论举报控制器)
│   ├── FollowController.java (关注控制器)
│   ├── MerchantCommentController.java (商家评论控制器)
│   ├── MerchantController.java (商家控制器)
│   ├── MerchantVoucherController.java (商家-优惠券控制器)
│   ├── OrderController.java (订单控制器)
│   ├── ProductController.java (商品控制器)
│   ├── ShopCommentController.java (店铺评论控制器)
│   ├── ShopController.java (店铺控制器)
│   ├── ShopTypeController.java (店铺类型控制器)
│   ├── UploadController.java (文件上传控制器)
│   ├── UserController.java (用户控制器)
│   ├── UserOrderController.java (用户订单控制器)
│   └── VoucherController.java (优惠券控制器)
├── dto/ (数据传输对象)
│   ├── 用户相关DTO
│   │   ├── UserDTO.java
│   │   ├── LoginFormDTO.java
│   │   └── ...
│   ├── 商品相关DTO
│   │   ├── ProductCreateDTO.java
│   │   ├── ProductUpdateDTO.java
│   │   └── ...
│   ├── 订单相关DTO
│   │   ├── OrderDTO.java
│   │   ├── CreateOrderDTO.java
│   │   └── ...
│   ├── 优惠券相关DTO
│   │   ├── VoucherCreateDTO.java
│   │   ├── VoucherUpdateDTO.java
│   │   └── ...
│   └── 其他DTO...
├── entity/ (实体类)
│   ├── Admin.java (管理员)
│   ├── AdminRole.java (管理员角色)
│   ├── AdminUserRole.java (管理员-用户-角色)
│   ├── Blog.java (博客)
│   ├── BlogComments.java (博客评论)
│   ├── Cart.java (购物车)
│   ├── CartItem.java (购物车项)
│   ├── CommentReport.java (评论举报)
│   ├── Follow.java (关注)
│   ├── Merchant.java (商家)
│   ├── MerchantLog.java (商家日志)
│   ├── MerchantQualification.java (商家资质)
│   ├── MerchantStatistics.java (商家统计)
│   ├── Order.java (订单)
│   ├── OrderComment.java (订单评论)
│   ├── OrderItem.java (订单项)
│   ├── Product.java (商品)
│   ├── ProductCategory.java (商品类别)
│   ├── ProductSpec.java (商品规格)
│   ├── SeckillVoucher.java (秒杀优惠券)
│   ├── Shop.java (店铺)
│   ├── ShopComment.java (店铺评论)
│   ├── ShopType.java (店铺类型)
│   ├── Sign.java (签到)
│   ├── User.java (用户)
│   ├── UserInfo.java (用户信息)
│   ├── Voucher.java (优惠券)
│   └── VoucherOrder.java (优惠券订单)
├── event/ (事件)
│   └── ShopScoreUpdateEvent.java (店铺评分更新事件)
├── exception/ (异常类)
│   ├── CommentException.java (评论异常)
│   ├── ReportException.java (举报异常)
│   └── VoucherException.java (优惠券异常)
├── mapper/ (数据访问层)
│   ├── AdminMapper.java (管理员Mapper)
│   ├── AdminRoleMapper.java (管理员角色Mapper)
│   ├── AdminUserRoleMapper.java (管理员-用户-角色Mapper)
│   ├── BlogCommentsMapper.java (博客评论Mapper)
│   ├── BlogMapper.java (博客Mapper)
│   ├── CartItemMapper.java (购物车项Mapper)
│   ├── CartMapper.java (购物车Mapper)
│   ├── CommentReportMapper.java (评论举报Mapper)
│   ├── FollowMapper.java (关注Mapper)
│   ├── MerchantMapper.java (商家Mapper)
│   ├── MerchantQualificationMapper.java (商家资质Mapper)
│   ├── OrderCommentMapper.java (订单评论Mapper)
│   ├── OrderItemMapper.java (订单项Mapper)
│   ├── OrderMapper.java (订单Mapper)
│   ├── ProductCategoryMapper.java (商品类别Mapper)
│   ├── ProductMapper.java (商品Mapper)
│   ├── ProductSpecMapper.java (商品规格Mapper)
│   ├── SeckillVoucherMapper.java (秒杀优惠券Mapper)
│   ├── ShopCommentMapper.java (店铺评论Mapper)
│   ├── ShopMapper.java (店铺Mapper)
│   ├── ShopTypeMapper.java (店铺类型Mapper)
│   ├── UserInfoMapper.java (用户信息Mapper)
│   ├── UserMapper.java (用户Mapper)
│   ├── VoucherMapper.java (优惠券Mapper)
│   └── VoucherOrderMapper.java (优惠券订单Mapper)
├── service/ (服务层接口)
│   ├── IAdminService.java (管理员服务)
│   ├── IBlogCommentsService.java (博客评论服务)
│   ├── IBlogReadStatusService.java (博客阅读状态服务)
│   ├── IBlogService.java (博客服务)
│   ├── ICartService.java (购物车服务)
│   ├── ICommentReportService.java (评论举报服务)
│   ├── IFollowService.java (关注服务)
│   ├── IMerchantCommentService.java (商家评论服务)
│   ├── IMerchantService.java (商家服务)
│   ├── IOrderService.java (订单服务)
│   ├── IProductService.java (商品服务)
│   ├── ISeckillVoucherService.java (秒杀优惠券服务)
│   ├── IShopCommentService.java (店铺评论服务)
│   ├── IShopService.java (店铺服务)
│   ├── IShopTypeService.java (店铺类型服务)
│   ├── IUserInfoService.java (用户信息服务)
│   ├── IUserService.java (用户服务)
│   ├── IVoucherOrderService.java (优惠券订单服务)
│   ├── IVoucherService.java (优惠券服务)
│   └── impl/ (服务实现)
│       ├── AdminServiceImpl.java (管理员服务实现)
│       ├── BlogCommentsServiceImpl.java (博客评论服务实现)
│       ├── BlogReadStatusServiceImpl.java (博客阅读状态服务实现)
│       ├── BlogServiceImpl.java (博客服务实现)
│       ├── CartServiceImpl.java (购物车服务实现)
│       ├── CommentReportServiceImpl.java (评论举报服务实现)
│       ├── FollowServiceImpl.java (关注服务实现)
│       ├── MerchantCommentServiceImpl.java (商家评论服务实现)
│       ├── MerchantServiceImpl.java (商家服务实现)
│       ├── OrderServiceImpl.java (订单服务实现)
│       ├── ProductServiceImpl.java (商品服务实现)
│       ├── SeckillVoucherServiceImpl.java (秒杀优惠券服务实现)
│       ├── ShopCommentServiceImpl.java (店铺评论服务实现)
│       ├── ShopServiceImpl.java (店铺服务实现)
│       ├── ShopTypeServiceImpl.java (店铺类型服务实现)
│       ├── UserInfoServiceImpl.java (用户信息服务实现)
│       ├── UserServiceImpl.java (用户服务实现)
│       ├── VoucherOrderServiceImpl.java (优惠券订单服务实现)
│       └── VoucherServiceImpl.java (优惠券服务实现)
└── utils/ (工具类)
    ├── BlogReadStatusMigrationTool.java (博客阅读状态迁移工具)
    ├── CacheClient.java (缓存客户端)
    ├── ILock.java (锁接口)
    ├── MerchantHolder.java (商家信息持有者)
    ├── PasswordEncoder.java (密码编码器)
    ├── RedisConstants.java (Redis常量)
    ├── RedisData.java (Redis数据)
    ├── RedisIdWorker.java (Redis ID生成器)
    ├── RegexPatterns.java (正则表达式模式)
    ├── RegexUtils.java (正则工具类)
    ├── SimleRedisLock.java (简单Redis锁)
    ├── SystemConstants.java (系统常量)
    └── UserHolder.java (用户信息持有者)

src/main/resources/
├── application.yaml (应用配置)
├── db/ (数据库相关)
├── logback.xml (日志配置)
├── lua/ (Lua脚本)
├── mapper/ (MyBatis映射文件)
│   ├── AdminMapper.xml
│   ├── AdminRoleMapper.xml
│   ├── AdminUserRoleMapper.xml
│   ├── CommentReportMapper.xml
│   ├── ProductMapper.xml
│   ├── ProductSpecMapper.xml
│   ├── ShopCommentMapper.xml
│   └── VoucherMapper.xml
└── unlock.lua (解锁Lua脚本)
```

## 主要模块

1. **用户模块**：用户注册、登录、信息管理
2. **商家模块**：商家注册、认证、管理
3. **店铺模块**：店铺创建、管理、查询
4. **商品模块**：商品创建、管理、查询
5. **订单模块**：订单创建、支付、管理
6. **评论模块**：用户评论、商家回复、评论管理
7. **优惠券模块**：优惠券创建、使用、管理
8. **博客模块**：博客发布、评论、点赞
9. **关注模块**：用户关注、粉丝管理
10. **购物车模块**：购物车管理
11. **管理员模块**：系统管理、权限控制

## 技术栈

- **核心框架**：Spring Boot
- **ORM框架**：MyBatis Plus
- **数据库**：MySQL
- **缓存**：Redis
- **分布式锁**：Redisson
- **API文档**：Knife4j (基于Swagger)
- **权限控制**：自定义拦截器