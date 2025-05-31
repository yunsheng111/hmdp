# Sa-Token 集成指南 (针对 HMDP 项目)

## 1. 引言

Sa-Token 是一个轻量级的 Java 权限认证框架，主要解决登录认证、权限认证、SSO、OAuth2.0、微服务认证等一系列权限相关问题。它功能强大、使用简单、设计优雅，能极大地提高开发效率和系统的安全性。

本文档旨在为 `hmdp` 项目提供一个关于如何集成 Sa-Token 的初步指南。请注意，本指南部分信息参考自 Sa-Token 的 Quarkus 扩展文档，因此在实际 Spring Boot 项目集成时，**务必以 Sa-Token 官方文档为准 ([https://sa-token.dev33.cn/](https://sa-token.dev33.cn/))**，特别是关于 Maven/Gradle 依赖和具体配置项。

**核心优势:**
*   **功能全面:** 支持多种认证、授权机制。
*   **轻量易用:** 学习成本低，上手快。
*   **灵活扩展:** 易于与其他框架集成，支持多种Token存储策略（如 Redis）。
*   **代码优雅:** 设计注重简洁和高效。

## 2. Sa-Token 核心概念

在集成之前，了解 Sa-Token 的一些核心概念非常重要：

*   **StpUtil:** Sa-Token 的核心工具类，几乎所有功能都通过它来调用。例如 `StpUtil.login(userId)`、`StpUtil.isLogin()`、`StpUtil.checkPermission("user:add")` 等。
*   **登录认证 (Authentication):** 验证用户身份，成功后会为用户生成一个 Token。
*   **权限认证 (Authorization):** 验证用户是否拥有执行某个操作或访问某个资源的权限。这通常通过角色 (Role) 和权限 (Permission) 来实现。
*   **Token:** 用户登录成功后的身份凭证。Sa-Token 支持多种 Token 风格和存储方式。
*   **Session:** Sa-Token 也提供了 Session 功能，可以用来存储与用户会话相关的数据。
*   **注解式鉴权:** Sa-Token 提供了 `@SaCheckLogin`、`@SaCheckRole`、`@SaCheckPermission` 等注解，可以方便地在 Controller 方法上进行权限控制。
*   **路由拦截鉴权:** 可以配置拦截器，对指定路由进行统一的登录或权限校验。
*   **Token 持久化:** Sa-Token 支持将 Token 数据持久化到多种存储中，如 Cookie、Header、Session，以及外部存储如 Redis。`hmdp` 项目已使用 Redis，Sa-Token 可以很好地利用这一点。

## 3. 在 Spring Boot 项目中集成 Sa-Token (通用步骤)

以下步骤是通用的集成思路，具体到 `hmdp` (Spring Boot) 项目时，请查找 Sa-Token 官方提供的 Spring Boot Starter。

### 3.1. 添加 Maven/Gradle 依赖

首先，需要在项目的 `pom.xml` (Maven) 或 `build.gradle` (Gradle) 文件中添加 Sa-Token 的 Spring Boot Starter 依赖。

**请访问 Sa-Token 官方文档 ([https://sa-token.dev33.cn/](https://sa-token.dev33.cn/)) 获取最新和最准确的 Spring Boot Starter 依赖信息。** 通常依赖的 artifactId 可能是类似 `sa-token-spring-boot-starter`。

例如 (仅为示意，请以官方为准):
```xml
<!-- pom.xml -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version> LATEST_VERSION </version> <!-- 请替换为最新版本 -->
</dependency>

<!-- 如果需要 Redis 集成 (推荐 hmdp 项目使用) -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-dao-redis-jackson</artifactId>
    <version> LATEST_VERSION </version>
</dependency>
```

### 3.2. 基础配置

Sa-Token 提供了丰富的配置项，可以在 Spring Boot 的 `application.yml` 或 `application.properties` 文件中进行配置。

常见的配置项可能包括：

*   `sa-token.token-name`: Token 名称，默认为 `satoken`。
*   `sa-token.timeout`: Token 有效期，单位秒。
*   `sa-token.active-timeout`: Token 最低活跃有效时间，单位秒。
*   `sa-token.is-concurrent`: 是否允许同一账号多地登录。
*   `sa-token.is-share`: 是否在 Session 和 Token 中共享数据。
*   `sa-token.token-style`: Token 风格 (如 `uuid`, `simple-uuid`, `random-32`, `random-64`, `random-128`, `tik`)。
*   `sa-token.cookie.domain`: Cookie 作用域。
*   **Redis 配置:** 如果使用 `sa-token-dao-redis-jackson`，Sa-Token 通常会自动集成 Spring Boot 的 Redis 配置。你可能需要确保 Redis 序列化方式兼容。

**示例 (`application.yml`):**
```yaml
sa-token:
  token-name: "hmdp-token"
  timeout: 7200 # 2小时有效期
  active-timeout: 1800 # 30分钟内无操作则过期
  is-concurrent: true # 允许并发登录
  # ... 其他配置
  # 配置 SaTokenDaoRedisJackson (如果使用)
  dao-redis-jackson:
    # 配置 key 前缀
    key-prefix: "hmdp:satoken:"
    # 配置数据序列化方式 (默认 jackson)
    data-key-prefix: "data:" 
    # ... 其他 jackson 相关配置
```
**再次强调，具体配置项请查阅 Sa-Token 官方文档。**

### 3.3. 登录与注销

**登录:**
在用户服务 (如 `UserServiceImpl`) 的登录方法中，当验证用户凭证成功后，调用 `StpUtil.login(userId)` 来标记用户已登录。
```java
// UserServiceImpl.java
public void login(String username, String password) {
    // ... 验证用户名密码 ...
    if (loginSuccess) {
        Object userId = user.getId(); // 假设 user.getId() 返回用户唯一标识
        StpUtil.login(userId);
        // 可以通过 StpUtil.getTokenValue() 获取当前生成的Token
        // 可以通过 StpUtil.getTokenInfo() 获取当前Token的详细信息
    } else {
        // ... 登录失败处理 ...
    }
}
```

**注销:**
调用 `StpUtil.logout()` 或 `StpUtil.logout(userId)`。
```java
// UserController.java
@PostMapping("/logout")
public Result logout() {
    StpUtil.logout();
    return Result.ok("注销成功");
}
```

### 3.4. 状态校验与权限认证

**校验登录状态:**
*   注解方式: `@SaCheckLogin` (未登录会抛出 `NotLoginException` 异常)
    ```java
    @SaCheckLogin
    @GetMapping("/userInfo")
    public Result getUserInfo() {
        // ... 业务逻辑 ...
        Object loginId = StpUtil.getLoginId(); // 获取当前会话账号ID
        return Result.ok(userService.findUserById(loginId));
    }
    ```
*   编程方式: `StpUtil.isLogin()` (返回布尔值), `StpUtil.checkLogin()` (未登录抛异常), `StpUtil.getLoginId()` (获取用户ID)。

**校验角色:**
*   注解方式: `@SaCheckRole("admin")`, `@SaCheckRole(value = {"admin", "super-admin"}, mode = SaMode.OR)`
*   编程方式: `StpUtil.hasRole("admin")`, `StpUtil.checkRole("admin")`

**校验权限:**
*   注解方式: `@SaCheckPermission("user:add")`, `@SaCheckPermission(value = {"user:add", "user:delete"}, mode = SaMode.AND)`
*   编程方式: `StpUtil.hasPermission("user:add")`, `StpUtil.checkPermission("user:add")`

### 3.5. 与 Spring MVC 集成

**全局异常处理:**
Sa-Token 抛出的认证授权相关异常 (如 `NotLoginException`, `NotRoleException`, `NotPermissionException`) 可以通过 Spring MVC 的全局异常处理器 (`@RestControllerAdvice`) 来捕获并返回统一的响应格式。
```java
// WebExceptionAdvice.java (hmdp 项目中已有此类)
@ExceptionHandler(NotLoginException.class)
public Result handleNotLoginException(NotLoginException e) {
    log.error("用户未登录: " + e.getMessage());
    // 根据 NotLoginException 的不同类型，可以返回更友好的提示信息
    // 例如：e.getType()
    // NotLoginException.NOT_TOKEN --> "未提供token"
    // NotLoginException.INVALID_TOKEN --> "token无效"
    // NotLoginException.TOKEN_TIMEOUT --> "token已过期"
    // NotLoginException.BE_REPLACED --> "token已被顶下线"
    // NotLoginException.KICK_OUT --> "token已被踢下线"
    return Result.fail("请先登录: " + e.getMessage()); // 或自定义错误码和消息
}

@ExceptionHandler(NotPermissionException.class)
public Result handleNotPermissionException(NotPermissionException e) {
    log.error("无此权限: " + e.getPermission());
    return Result.fail("您没有访问权限：" + e.getPermission());
}

@ExceptionHandler(NotRoleException.class)
public Result handleNotRoleException(NotRoleException e) {
    log.error("无此角色: " + e.getRole());
    return Result.fail("您没有此角色权限：" + e.getRole());
}
```

**拦截器配置 (可选，通常 Sa-Token 的 Spring Boot Starter 会自动配置):**
Sa-Token 的 Spring Boot Starter 通常会自动注册必要的拦截器来处理 Token 的传递和校验。如果需要自定义拦截规则，可以查阅官方文档关于 `SaInterceptor` 的说明。
这可能会替代或增强 `hmdp`项目中现有的 `LoginInterceptor`、`MerchantLoginInterceptor` 和 `RefreshTokenInterceptor`。`RefreshTokenInterceptor` 的逻辑（Token刷新）在Sa-Token中也有对应的 `active-timeout` 配置和机制。

### 3.6. Token 存储与 Redis 集成

对于 `hmdp` 项目，推荐使用 Redis 来存储 Sa-Token 的数据 (Token、Session、权限信息等)，以支持分布式部署和更好的性能。
如果引入了 `sa-token-dao-redis-jackson` 或类似的 Redis DAO 依赖，并正确配置了 Spring Boot 的 Redis 连接，Sa-Token 通常会自动使用 Redis 进行数据存取。

`hmdp` 项目中已有的 `RedisConfig.java` 和 `CacheClient.java` 可能需要审视，以确保与 Sa-Token 的 Redis 操作不冲突或可以复用。

## 4. 如何与 HMDP 现有机制结合或替换

*   **用户身份传递:** Sa-Token 的 `StpUtil.getLoginId()` 可以替代 `UserHolder.getUser()` 来获取当前登录用户ID。类似的，如果商家登录也用 Sa-Token 管理，可以替代 `MerchantHolder`。
*   **拦截器:** Sa-Token 提供的拦截器或注解鉴权机制，可以逐步替换项目原有的 `LoginInterceptor`、`MerchantLoginInterceptor` 等。`RefreshTokenInterceptor` 的 Token 刷新功能，Sa-Token 本身也有相应的 `active-timeout` 机制，当用户在 `active-timeout` 时间内有操作，Token 的有效期会自动续期。
*   **密码加密:** Sa-Token 本身不强制密码加密方式，`hmdp`项目中现有的 `PasswordEncoder` 仍然可以使用。

## 5. 进一步学习

*   **Sa-Token 官方文档:** [https://sa-token.dev33.cn/](https://sa-token.dev33.cn/) - 这是最权威和详细的资源。
*   **Sa-Token Gitee 仓库:** [https://gitee.com/dromara/sa-token](https://gitee.com/dromara/sa-token) - 查看源码和示例。

## 6. 总结 (AR, LD)

集成 Sa-Token 到 `hmdp` 项目可以带来更现代化和功能全面的权限管理方案。
关键步骤包括：
1.  仔细阅读 Sa-Token 官方文档，选择合适的 Spring Boot Starter。
2.  在 `pom.xml` 中添加依赖。
3.  在 `application.yml` 中进行核心配置，特别是 Token 行为和 Redis DAO (如果使用)。
4.  在用户登录、注销逻辑中调用 `StpUtil` 的方法。
5.  使用注解 (`@SaCheckLogin`, `@SaCheckRole`, `@SaCheckPermission`) 或编程式 API 进行权限控制。
6.  配置全局异常处理器以美化错误提示。
7.  评估并逐步替换现有的认证拦截器和用户持有工具类。

建议先在项目的某个模块或新建一个测试模块进行小范围集成和测试，熟悉其工作方式后再全面推广。 