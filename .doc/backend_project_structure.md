# HMDP 后端项目结构分析 (`src` 目录)

本文档旨在提供 `hmdp` 项目后端代码（主要在 `src` 目录下）的结构概览和各主要文件/目录的用途说明。

## 总体结构

项目后端采用典型的 Java Spring Boot 技术栈，具有清晰的分层结构：

- **`src/main/java/com/hmdp/`**: 核心业务代码和逻辑。
    - `controller`: API 接口层，处理外部 HTTP 请求。
    - `service`: 业务逻辑层，包含接口（`service`包）和实现（`service/impl`包）。
    - `dto`: 数据传输对象，用于各层之间的数据传递。
    - `entity`: 数据库实体类，映射数据库表结构。
    - `mapper`: 数据访问层接口（MyBatis Mapper）。
    - `config`: 应用配置类 (如MVC, Redis, Mybatis, 安全等)。
    - `common`: 通用组件 (如拦截器、统一结果返回类)。
    - `utils`: 工具类 (如Redis操作、ID生成器、正则校验等)。
    - `HmDianPingApplication.java`: Spring Boot 应用主入口。
- **`src/main/resources/`**: 资源文件。
    - `application.yaml`: 主要的应用配置文件。
    - `logback.xml`: 日志配置文件。
    - `unlock.lua`: Redis Lua 脚本。
    - `db/`: 数据库脚本 (如 `hmdp.sql`)。
    - `mapper/`: MyBatis Mapper XML 文件，定义 SQL 语句。
- **`src/test/java/com/hmdp/`**: 测试代码，包含单元测试和集成测试。

## 详细文件用途说明

### A. `src/main/java/com/hmdp/`

*   `HmDianPingApplication.java`: Spring Boot主启动类，应用程序的入口点。

#### A.1. `controller` (处理HTTP请求，API接口层)
*   `BlogCommentsController.java`: 处理博客评论相关的API请求。
*   `BlogController.java`: 处理博客相关的API请求 (如发布、查看、点赞等)。
*   `FollowController.java`: 处理用户关注功能相关的API请求。
*   `MerchantController.java`: 处理商家信息相关的API请求。
*   `ShopController.java`: 处理店铺信息相关的API请求。
*   `ShopTypeController.java`: 处理店铺类型相关的API请求。
*   `UploadController.java`: 处理文件上传相关的API请求。
*   `UserController.java`: 处理用户管理 (如登录、注册、用户信息) 相关的API请求。
*   `VoucherController.java`: 处理优惠券相关的API请求。
*   `VoucherOrderController.java`: 处理优惠券订单 (如秒杀优惠券下单) 相关的API请求。

#### A.2. `service` (业务逻辑层接口)
*   `IBlogCommentsService.java`: 博客评论相关业务逻辑的接口。
*   `IBlogService.java`: 博客相关业务逻辑的接口。
*   `IFollowService.java`: 用户关注功能相关业务逻辑的接口。
*   `IMerchantService.java`: 商家相关业务逻辑的接口。
*   `ISeckillVoucherService.java`: 秒杀优惠券相关业务逻辑的接口。
*   `IShopService.java`: 店铺相关业务逻辑的接口。
*   `IShopTypeService.java`: 店铺类型相关业务逻辑的接口。
*   `IUserInfoService.java`: 用户详细信息相关业务逻辑的接口。
*   `IUserService.java`: 用户核心业务逻辑 (如登录、注册) 的接口。
*   `IVoucherOrderService.java`: 优惠券订单相关业务逻辑的接口。
*   `IVoucherService.java`: 优惠券相关业务逻辑的接口。

    **`service/impl` (业务逻辑层实现)**
    *   `BlogCommentsServiceImpl.java`: `IBlogCommentsService`接口的实现类。
    *   `BlogServiceImpl.java`: `IBlogService`接口的实现类。
    *   `FollowServiceImpl.java`: `IFollowService`接口的实现类。
    *   `MerchantServiceImpl.java`: `IMerchantService` 接口的实现类.
    *   `SeckillVoucherServiceImpl.java`: `ISeckillVoucherService`接口的实现类。
    *   `ShopServiceImpl.java`: `IShopService`接口的实现类。
    *   `ShopTypeServiceImpl.java`: `IShopTypeService`接口的实现类。
    *   `UserInfoServiceImpl.java`: `IUserInfoService`接口的实现类。
    *   `UserServiceImpl.java`: `IUserService`接口的实现类。
    *   `VoucherOrderServiceImpl.java`: `IVoucherOrderService`接口的实现类。
    *   `VoucherServiceImpl.java`: `IVoucherService`接口的实现类。

#### A.3. `dto` (Data Transfer Object, 数据传输对象)
*   `LoginFormDTO.java`: 用于用户登录时传输登录表单数据的对象。
*   `MerchantDTO.java`: 用于商家数据显示的数据传输对象。
*   `MerchantLoginFormDTO.java`: 用于商家登录时传输登录表单数据的对象。
*   `ScrollResult.java`: 用于滚动分页查询结果的数据传输对象。
*   `UserDTO.java`: 用于在不同层之间传输用户简要信息 (通常不含敏感信息) 的对象。

#### A.4. `entity` (数据库实体类)
*   `Admin.java`: 管理员实体类，映射数据库中的管理员表。
*   `AdminRole.java`: 管理员角色实体类，映射数据库中的管理员角色表。
*   `AdminUserRole.java`: 管理员用户与角色关联实体类，映射中间表。
*   `Blog.java`: 博客实体类，映射数据库中的博客表。
*   `BlogComments.java`: 博客评论实体类，映射数据库中的博客评论表。
*   `Follow.java`: 用户关注关系实体类，映射数据库中的关注表。
*   `Merchant.java`: 商家实体类，映射数据库中的商家表。
*   `MerchantLog.java`: 商家操作日志实体类。
*   `MerchantQualification.java`: 商家资质实体类。
*   `MerchantStatistics.java`: 商家统计数据实体类。
*   `Order.java`: 订单实体类。
*   `OrderComment.java`: 订单评论实体类。
*   `OrderItem.java`: 订单项实体类。
*   `Product.java`: 商品/产品实体类。
*   `ProductCategory.java`: 商品分类实体类。
*   `ProductSpec.java`: 商品规格实体类。
*   `SeckillVoucher.java`: 秒杀优惠券实体类，映射数据库中的秒杀优惠券表。
*   `Shop.java`: 店铺实体类，映射数据库中的店铺表。
*   `ShopType.java`: 店铺类型实体类，映射数据库中的店铺类型表。
*   `Sign.java`: 用户签到实体类，映射数据库中的签到表。
*   `User.java`: 用户实体类，映射数据库中的用户表。
*   `UserInfo.java`: 用户详细信息实体类，映射数据库中的用户详细信息表。
*   `Voucher.java`: 优惠券实体类，映射数据库中的优惠券表。
*   `VoucherOrder.java`: 优惠券订单实体类，映射数据库中的优惠券订单表。

#### A.5. `config` (配置类)
*   `CorsConfig.java`: CORS (跨源资源共享) 配置类。
*   `Knife4jConfig.java`: Knife4j API文档生成工具的配置类。
*   `MvcConfig.java`: Spring MVC相关的配置类 (如拦截器注册)。
*   `MybatisConfig.java`: MyBatisPlus的配置类 (如分页插件)。
*   `RedisConfig.java`: Redis相关的配置类 (如序列化器)。
*   `RedissonConfig.java`: Redisson (Redis客户端) 的配置类。
*   `SecurityConfig.java`: Spring Security相关的配置类 (可能用于权限控制)。
*   `WebExceptionAdvice.java`: 全局异常处理配置类。

#### A.6. `common` (通用组件)
*   `LoginInterceptor.java`: 普通用户登录状态校验拦截器。
*   `MerchantLoginInterceptor.java`: 商家登录拦截器。
*   `MerchantRefreshTokenInterceptor.java`: 商家Token刷新拦截器。
*   `RefreshTokenInterceptor.java`: 普通用户Token刷新拦截器，用于延长会话有效期。
*   `Result.java`: 定义了统一的API接口返回结果格式。

#### A.7. `utils` (工具类)
*   `CacheClient.java`: 封装了基于Redis的缓存操作的客户端工具类，可能包含缓存穿透、雪崩等问题的解决方案。
*   `ILock.java`: 分布式锁的接口定义。
*   `PasswordEncoder.java`: 密码加密工具类。
*   `RedisConstants.java`: Redis相关的常量定义 (如key的前缀、过期时间等)。
*   `RedisData.java`: 用于缓存逻辑过期数据的包装类。
*   `RedisIdWorker.java`: 基于Redis实现的全局唯一ID生成器。
*   `RegexPatterns.java`: 常用的正则表达式模式定义。
*   `RegexUtils.java`: 正则表达式校验工具类。
*   `SimleRedisLock.java`: 一个简单的基于Redis SETNX命令实现的分布式锁。
*   `SystemConstants.java`: 系统级别的常量定义。
*   `UserHolder.java`: 使用ThreadLocal存储当前登录用户信息的工具类。
*   `MerchantHolder.java`: 使用ThreadLocal存储当前登录商家信息的工具类。

#### A.8. `mapper` (MyBatis Mapper接口，数据访问层)
*   `AdminMapper.java`: 管理员表的数据操作接口。
*   `AdminRoleMapper.java`: 管理员角色表的数据操作接口。
*   `AdminUserRoleMapper.java`: 管理员用户角色关联表的数据操作接口。
*   `BlogCommentsMapper.java`: 博客评论表的数据操作接口。
*   `BlogMapper.java`: 博客表的数据操作接口。
*   `FollowMapper.java`: 用户关注关系表的数据操作接口。
*   `MerchantMapper.java`: 商家表的数据操作接口。
*   `SeckillVoucherMapper.java`: 秒杀优惠券表的数据操作接口。
*   `ShopMapper.java`: 店铺表的数据操作接口。
*   `ShopTypeMapper.java`: 店铺类型表的数据操作接口。
*   `UserInfoMapper.java`: 用户详细信息表的数据操作接口。
*   `UserMapper.java`: 用户表的数据操作接口。
*   `VoucherMapper.java`: 优惠券表的数据操作接口。
*   `VoucherOrderMapper.java`: 优惠券订单表的数据操作接口。

### B. `src/main/resources` (资源文件)

*   `application.yaml`: Spring Boot主配置文件，包含端口、数据库连接、Redis配置、日志级别等应用配置。
*   `logback.xml`: Logback日志框架的配置文件，定义日志输出格式、级别和目标。
*   `unlock.lua`: Redis分布式锁释放的Lua脚本，保证操作的原子性。

#### B.1. `db` (数据库相关)
*   `hmdp.sql`: 项目数据库的SQL脚本，可能包含建表语句和初始数据。
*   `mysql`: (可能是个空文件或者标记文件，具体用途需结合项目上下文)

#### B.2. `mapper` (MyBatis Mapper XML文件)
*   `AdminMapper.xml`: 对应 `AdminMapper.java` 接口的SQL语句定义。
*   `AdminRoleMapper.xml`: 对应 `AdminRoleMapper.java` 接口的SQL语句定义。
*   `AdminUserRoleMapper.xml`: 对应 `AdminUserRoleMapper.java` 接口的SQL语句定义。
*   `VoucherMapper.xml`: 对应 `VoucherMapper.java` 接口的SQL语句定义，可能包含更新优惠券库存等复杂SQL。
*   *(注: 其他Mapper XML文件，如`BlogMapper.xml`, `ShopMapper.xml`等，应根据`src/main/java/com/hmdp/mapper/`中的Java接口类推断存在于此目录，并遵循相应命名约定。)*

### C. `src/test/java/com/hmdp/` (测试代码)

*   `HmDianPingApplicationTests.java`: Spring Boot 项目自动生成的集成测试入口类，通常用于简单的上下文加载测试。
*   `TestRedisIdWork.java`: 针对 `RedisIdWorker` 工具类的单元测试或集成测试。
*   `TestShopServiceImpl.java`: 针对 `ShopServiceImpl` 业务逻辑实现类的单元测试或集成测试。
*   *(注: 可能包含其他针对特定类或功能的测试文件。)*
