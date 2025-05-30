# HMDP项目userroles.md更新说明

## 更新概述
基于您当前的HMDP(黑马点评)项目结构，我对userroles.md文档进行了全面定制化更新，使其更适合Spring Boot + Redis + MySQL技术栈的开发工作。

## 主要更改内容

### 1. 项目上下文定制化
**原版:** 通用的AI编程助手协议
**更新后:** 专门针对HMDP点评系统的Spring Boot开发协议

**具体更改:**
- 标题更改为"HMDP项目开发协议 - Spring Boot点评系统专用版 (v4.0)"
- 明确技术栈：Spring Boot 2.7.15 + Redis + MySQL + MyBatis-Plus
- 项目结构说明：包含具体的目录结构和现有文档

### 2. 团队角色优化
**原版:** 通用软件开发团队角色
**更新后:** 针对Spring Boot项目的专业角色

**新增/调整角色:**
- **后端开发工程师 (LD):** 专注Spring Boot应用开发、MyBatis-Plus数据访问、Redis缓存实现
- **前端工程师 (FE):** 负责前端页面开发、接口对接（替换原UI/UX Designer）
- **运维工程师 (OPS):** 负责部署策略、监控告警、性能优化（替换原Security Engineer）
- **系统架构师 (AR):** 强调Spring Boot架构设计、Redis缓存策略、数据库设计

### 3. 编码原则Spring Boot化
**原版:** 通用编码原则（SOLID、DRY等）
**更新后:** Spring Boot特定最佳实践

**新增内容:**
- **Spring Boot特定最佳实践:** 分层架构、异常处理、配置管理、缓存策略、事务管理
- **数据库与缓存优化:** MyBatis-Plus使用、Redis缓存策略、连接池配置
- **安全与性能:** 输入验证、SQL注入防护、接口限流

### 4. 开发模式本地化
**原版:** 英文模式说明
**更新后:** 中文模式说明，增加Spring Boot特定考虑

**RESEARCH模式更新:**
- 增加Spring Boot应用架构分析
- Redis缓存使用情况评估
- MySQL数据库设计审查
- 技术约束包含Java 8兼容性、Spring Boot版本限制

**INNOVATE模式更新:**
- Spring Boot特定考虑：Bean配置、自动配置、切面设计
- Redis缓存策略：缓存键设计、过期策略
- 数据库设计：表结构、索引策略

**PLAN模式更新:**
- Spring Boot特定配置：application.yml、Bean配置、切面配置
- 详细测试策略包含单元测试vs集成测试

### 5. 代码处理指南优化
**原版:** 通用代码块结构
**更新后:** Spring Boot专用代码块结构

**新增字段:**
```java
// Spring_Boot_Best_Practice: [应用的Spring Boot最佳实践]
// Performance_Impact: [性能影响评估]
// Security_Consideration: [安全考虑]
// Cache_Strategy: [缓存策略，如适用]
```

**Spring Boot特定编码规范:**
- Controller层：RESTful设计、统一异常处理、参数验证
- Service层：业务逻辑封装、事务管理、缓存策略
- Repository层：MyBatis-Plus使用、SQL优化

### 6. 任务文件模板定制
**原版:** 通用项目任务模板
**更新后:** HMDP项目专用模板

**主要更改:**
- 项目名称/ID格式：HMDP-[AI生成的当前任务唯一ID]
- 技术栈明确标注：Spring Boot 2.7.15 + Redis + MySQL + MyBatis-Plus
- 团队角色包含FE、OPS等适合当前项目的角色
- 分析部分包含Spring Boot应用架构分析、Redis缓存评估等
- 实现计划包含Spring Boot特定实现要求

### 7. 语言本地化
**原版:** 主要使用英文
**更新后:** 中文为主，技术术语保持英文

**改进:**
- 所有模式说明、思考过程示例都使用中文
- 保持代码块、文件名等技术内容使用英文
- 更符合中文开发团队的使用习惯

## 使用建议

1. **项目启动时:** 使用更新后的协议可以更好地适配您的Spring Boot项目需求
2. **团队协作:** 新的角色定义更贴合实际的前后端分离开发模式
3. **代码质量:** Spring Boot特定的编码原则有助于提高代码质量和可维护性
4. **文档管理:** 定制化的任务文件模板能更好地记录项目进展

## 保留的核心特性

- RIPER-5开发模式（RESEARCH -> INNOVATE -> PLAN -> EXECUTE -> REVIEW）
- 多角色协作思维模式
- 严格的文档管理和记忆机制
- MCP交互反馈机制
- 代码变更追踪机制

这个更新版本将更好地服务于您的HMDP点评系统开发工作，提供更精准的技术指导和更高效的开发流程。
