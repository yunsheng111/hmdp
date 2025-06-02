# Context
Project_Name/ID: FanInboxReadUnreadFeature_20240529_1000
Task_Filename: FanInboxReadUnreadDesign.md 
Created_At: [2024-05-29 10:00:00 +08:00]
Creator: AI (Qitian Dasheng - PM drafted, DW organized)
Associated_Protocol: RIPER-5 + Multi-Dimensional Thinking + Agent Execution Protocol (Refined v3.8)
Project_Workspace_Path: `/project_document/`

# 0. Team Collaboration Log & Key Decision Points 
(Located in /project_document/team_collaboration_log.md or within this file, DW maintains, PM chairs meetings)
---
Refer to `team_collaboration_log.md` for detailed meeting minutes.
---

# Task Description
研究和讨论当大V发布笔记推送到粉丝收件箱后，粉丝已读和未读这个功能如何设计。分析现有实现，识别问题，并探讨潜在的优化方案和新设计思路。

# Project Overview (Populated in RESEARCH or PLAN phase)
目标是为用户提供清晰、准确、高效的博文已读/未读状态管理功能，提升用户在粉丝收件箱中的内容消费体验。

---
*The following sections are maintained by AI during protocol execution. DW is responsible for overall document quality. All referenced paths are relative to `/project_document/` unless specified. All documents should include an update log section where applicable.*
---

# 1. Analysis (RESEARCH Mode Population)

**当前实现分析 (基于 `BlogServiceImpl.java`)**

**核心 Redis Keys:**

*   `FEED_KEY:{userId}` (ZSET): 用户收件箱，存储 `blogId`，`score` 为推送时间戳。
    *   用于 `queryBlogOfFollow` 拉取关注动态。
*   `BLOG_READ_KEY:{userId}` (ZSET): 用户已读博客，存储 `blogId`，`score` 为标记已读的时间戳。
    *   用于在 `queryBlogOfFollow` 和 `queryUserBlogByReadStatus` 中判断博文的 `isRead` 状态。
*   `USER_UNREAD_COUNT_KEY:{userId}` (String): 存储用户未读消息数。
    *   `queryBlogOfFollow`: 用 *当前页查询到的博文数量* 来更新此值。 **(主要问题点)**
    *   `markBlogAsRead`: 返回 `FEED_KEY` 中剩余元素数量作为未读数 (间接更新，如果此值被前端使用)。

**主要方法行为与问题点：**

1.  **`saveBlog(Blog blog)` (发布博文并推送给粉丝)**
    *   将博文ID (`blog.getId()`) 添加到每个粉丝的 `FEED_KEY:{fanId}` ZSET中，`score` 为当前时间戳。
    *   **潜在问题/思考点:**
        *   **原子性/事务性**: 如果有大量粉丝，部分推送成功，部分失败怎么办？（当前代码没有事务处理）
        *   **未读数更新**: 在这里，新博文推送给粉丝时，粉丝的未读总数应该原子性增加。当前代码没有直接操作 `USER_UNREAD_COUNT_KEY`。

2.  **`queryBlogOfFollow(Long max, Integer offset)` (查询关注的博文，即收件箱)**
    *   从 `FEED_KEY:{userId}` (ZSET) 中按分数（时间戳）倒序分页获取博文ID。
    *   获取该用户的所有已读博文ID列表 (`BLOG_READ_KEY:{userId}` ZSET)。
    *   遍历查询到的博文，如果博文ID在已读列表中，设置 `blog.setIsRead(true)`。
    *   **未读数更新逻辑**: `stringRedisTemplate.opsForValue().set(unreadKey, String.valueOf(blogs.size()));` -> 这是用**当前页返回的博文数量**来设置总未读数，是错误的。用户看到的应该是总的未读博文数量，而非当前页有多少条。
    *   **性能**: `stringRedisTemplate.opsForZSet().range(readKey, 0, -1)` 获取所有已读ID，如果已读数量巨大，这里会有性能问题。

3.  **`markBlogAsRead(Long id)` (标记博文已读)**
    *   将博文ID添加到用户的 `BLOG_READ_KEY:{userId}` ZSET。
    *   从用户的 `FEED_KEY:{userId}` ZSET 中移除该博文ID。
    *   返回 `FEED_KEY:{userId}` ZSET 的大小作为结果（可能被前端理解为新的未读数）。
    *   **原子性**: 添加到已读列表和从收件箱移除，这两步操作不是原原子性的。如果一步成功一步失败，数据会不一致。
    *   **未读数更新**: 如果 `FEED_KEY` 设计为只存未读消息，那么其 `size` 就是未读数。此操作间接维护了未读数。但 `USER_UNREAD_COUNT_KEY` 如何同步？

4.  **`queryUserBlogByReadStatus(Long userId, Integer current, Integer size, String readStatus)` (按阅读状态查某用户的博文)**
    *   主要用于查看某个特定用户（可能是自己也可能是他人主页）发布的博文，并根据当前登录用户的阅读状态来标记这些博文。
    *   `filterUnread = "UNREAD".equals(readStatus)`: 如果要查未读，会用 `AMPLIFICATION_FACTOR` 放大查询量，然后在内存过滤。
    *   **性能**: 同样存在获取当前登录用户所有已读ID (`readBlogIds`) 的性能问题。
    *   **逻辑清晰度**: 放大查询再内存过滤的逻辑，虽然是为了提高未读命中率，但也增加了复杂性，且不一定精确。

**总结当前实现的主要问题：**

*   **P1 (高优): 未读总数不准确且更新逻辑混乱。** `USER_UNREAD_COUNT_KEY` 的更新方式不正确。
*   **P2 (高优): 操作非原子性导致潜在数据不一致。** 特别是 `markBlogAsRead` 中的两步Redis操作。
*   **P3 (中优): 获取全量已读博文列表存在性能瓶颈。** 当用户已读博文非常多时。
*   **P4 (中优): "已读"触发机制不明确。** 当前是显式调用 `markBlogAsRead`。通常用户期望点击博文详情页即视为已读。
*   **P5 (中优): 博文被作者删除后，粉丝收件箱状态未处理。** (代码中未体现相关逻辑)
*   **P6 (低优): `queryBlogOfFollow` 中偏移量 `os` 的计算逻辑较复杂，需验证。**

**知识空白点与待澄清需求 (需要用户确认):**

1.  **"已读"的确切定义与触发机制**：
    *   **用户决策 (1A): 用户点击博文详情页即算已读。**
    *   ~~还是需要用户在详情页停留一段时间？~~
    *   ~~或者必须显式点击"标记为已读"按钮？（当前 `markBlogAsRead` 接口暗示了这点）~~
2.  **未读数展示需求**：
    *   **用户决策 (2C): 需要全局的总未读数，并且也需要能够区分每个关注的大V分别有多少篇未读博文。**
    *   ~~还是区分每个关注的大V各有多少未读？ (UI/UX 提及此需求)~~ 
    *   未读数是否需要在多个入口（如App角标、首页Tab、收件箱列表顶部）展示？ (此点待后续UI/UX确认细化)
3.  **收件箱 (`FEED_KEY`) 的设计理念**：
    *   **用户决策 (3B): 收件箱保留所有推送给用户的博文（无论已读未读）。阅读状态通过另外的方式标记，博文不会从主收件箱列表中移除。**
    *   ~~`FEED_KEY` 是否应该只包含"未读"的博文？从 `markBlogAsRead` 的移除操作看，似乎是这个意图。但如果这样，`queryBlogOfFollow` 中再根据 `BLOG_READ_KEY` 去判断 `isRead` 就显得多余了。这里逻辑似乎有矛盾或冗余。~~
4.  **博文删除处理 (新增用户需求):**
    *   **用户决策: 博文被作者删除后，粉丝收件箱的笔记标题下方应提示"该笔记已被作者删除"。对应的博文在逻辑上应该不再算作有效未读内容 (如何影响未读计数需讨论)。**

* Requirements Clarification/Deep Dive 
* Code/System Investigation
* Technical Constraints & Challenges
* Implicit Assumptions
* Early Edge Case Considerations
* Preliminary Risk Assessment
* Knowledge Gaps
* **DW Confirmation:** This section is complete, clear, synced, and meets documentation standards. [2024-05-29 10:20:00 +08:00] -> **Updated with user feedback [2024-05-29 10:30:00 +08:00]**

# 2. Proposed Solutions (INNOVATE Mode Population)

**方案1: 基于增强型原子计数器与被动删除处理的方案**

*   **核心思路**: 
    *   收件箱 (`FEED_KEY`) 保留所有推送历史。
    *   已读状态记录在单独的 `BLOG_READ_KEY`。
    *   引入明确的、原子更新的总未读数和按作者未读数计数器。
    *   博文删除采用被动更新策略，在用户查询时检测并更新状态和相关未读数。
    *   关键的复合Redis操作通过Lua脚本保证原子性。

*   **核心 Redis Keys 提案**:
    *   `FEED_KEY:{userId}` (ZSET: `blogId` -> `pushTimestamp`): 用户博文推送全量历史流。
    *   `BLOG_READ_KEY:{userId}` (ZSET: `blogId` -> `readTimestamp`): 用户已读博文。
    *   `TOTAL_UNREAD_COUNT_KEY:{userId}` (Redis Integer): 用户总未读数。
    *   `AUTHOR_UNREAD_COUNT_KEY:{fanId}:{authorId}` (Redis Integer): 用户对特定作者的未读数。
    *   `BLOG_AUTHOR_MAP_KEY:{blogId}` (Redis String/Hash field): `authorId` (用于辅助获取博文的作者ID，在标记已读或处理删除时使用)。
    *   `DELETED_BLOG_HINTS_KEY` (Redis Set): 存储已被作者删除的 `blogId`。

*   **主要操作流程设想**:
    1.  **发布博文 (博主操作)**:
        *   保存博文到数据库。
        *   获取博主的所有粉丝。
        *   对每个粉丝，通过Lua脚本原子执行 (或一系列原子Redis命令):
            *   `ZADD FEED_KEY:{fanId} {pushTimestamp} {blogId}`
            *   `INCR TOTAL_UNREAD_COUNT_KEY:{fanId}`
            *   `INCR AUTHOR_UNREAD_COUNT_KEY:{fanId}:{authorId}`
            *   (可选) `SET BLOG_AUTHOR_MAP_KEY:{blogId} {authorId}` 或 `HSET BLOG_INFO_KEY {blogId}:author {authorId}`
    2.  **点击博文详情 (粉丝操作，触发已读)**:
        *   前端调用后端 `markAsRead(blogId)` 接口。
        *   后端通过Lua脚本原子执行 (输入: `userId`, `blogId`, `authorId` - `authorId` 从 `BLOG_AUTHOR_MAP_KEY` 或博文数据获取):
            *   检查 `ZSCORE BLOG_READ_KEY:{userId} {blogId}` 是否已存在 (即是否已读)。
            *   如果未读 (score is nil):
                *   `DECR TOTAL_UNREAD_COUNT_KEY:{userId}` (确保结果不为负，Lua: `redis.call('SET', key, math.max(0, tonumber(redis.call('GET', key) or '1') - 1))` 或者直接 `DECR` 后判断，但原子性内处理更好)
                *   `DECR AUTHOR_UNREAD_COUNT_KEY:{userId}:{authorId}` (同上，确保不为负)
                *   `ZADD BLOG_READ_KEY:{userId} {readTimestamp} {blogId}`
    3.  **查询关注动态/收件箱 (粉丝操作 `queryBlogOfFollow`)**: 
        *   从 `FEED_KEY:{userId}` 分页获取 `blogId` 列表。
        *   对每个 `blogId`:
            *   查询博文基本内容 (如标题、作者信息等)。
            *   检查 `SISMEMBER DELETED_BLOG_HINTS_KEY {blogId}` 判断是否已删除。
                *   如果已删除:
                    *   在返回对象中标记 `isDeleted = true`。
                    *   检查 `ZSCORE BLOG_READ_KEY:{userId} {blogId}` 是否为 `nil` (即删除前是未读的)。
                    *   如果是删除前未读，则调用类似上述"标记已读"的Lua脚本逻辑（传入该 `blogId` 和 `authorId`）来原子性扣减未读数，并将此 `blogId` 加入 `BLOG_READ_KEY` (标记为已处理删除状态，防止重复扣减)。
                *   如果未删除: 标记 `isDeleted = false`。
            *   检查 `ZSCORE BLOG_READ_KEY:{userId} {blogId}`，设置 `isRead` 状态 (如果 score 非nil，则为true)。
        *   返回博文列表给前端（包含 `isRead`, `isDeleted` 状态，前端根据 `isDeleted` 显示"已被作者删除"）。
    4.  **获取未读数 (粉丝操作)**:
        *   总未读数: `GET TOTAL_UNREAD_COUNT_KEY:{userId}` (若为nil视作0)。
        *   按作者未读数: `GET AUTHOR_UNREAD_COUNT_KEY:{userId}:{authorId}` (若为nil视作0)。
    5.  **删除博文 (博主操作)**:
        *   数据库层面标记博文删除或实际删除博文记录。
        *   `SADD DELETED_BLOG_HINTS_KEY {blogId}`。
        *   (可选，作为补充) 可触发一个低优先级的异步任务，尝试清理与该 `blogId` 相关的粉丝未读计数，但主要依赖查询时的被动更新机制。

*   **优点**:
    *   未读数（总数和按作者）维护相对准确，且通过原子操作（Lua或Redis原生）保证。
    *   满足用户需求：区分总未读和按作者未读、收件箱保留全量历史、点击详情即已读。
    *   已读状态判断性能较好（单个 `ZSCORE` 或 `SISMEMBER`）。
    *   博文删除处理机制明确，用户感知到的未读数与博文状态基本同步。

*   **缺点/风险**:
    *   Lua脚本的编写、测试和维护有一定复杂度。
    *   `BLOG_AUTHOR_MAP_KEY` 增加了额外的Redis Key维护负担；或者需要在推送、标记已读时确保能便捷、一致地获取到 `authorId`。
    *   博文删除后，全局未读数的精确更新依赖于用户下一次拉取到该博文时触发，不是严格的实时全局扣减（除非实现非常复杂的强一致异步任务）。但用户在看到"已删除"提示时，其个人感知范围内的未读数会被修正。
    *   推送博文时，对大量粉丝进行多个 `INCR` 操作，虽然单个操作快，累积起来对Redis仍有压力。需关注推送性能。
    *   需要确保所有减计数操作 (`DECR`) 不会导致计数器变为负数，应在Lua脚本中处理或应用层保证。

* **DW Confirmation:** This section outlines a primary solution. [2024-05-29 11:00:00 +08:00]

---

**方案2: 基于收件箱内联已读标记（Set）与聚合计数**

*   **核心思路**:
    *   与方案1类似，收件箱 (`FEED_KEY`) 保留所有推送历史，并依赖原子更新的总未读数和按作者未读数计数器。
    *   主要区别在于已读状态的记录方式：使用一个独立的Redis Set (`READ_IN_FEED_KEY`) 来存储已被阅读的博文ID，而不是ZSet。这不记录阅读时间，但存储更轻量。
    *   博文删除和关键操作的原子性保证（Lua脚本）与方案1思路一致。

*   **核心 Redis Keys 提案**:
    *   `FEED_KEY:{userId}` (ZSET: `blogId` -> `pushTimestamp`): 用户博文推送全量历史流。
    *   `READ_IN_FEED_KEY:{userId}` (Redis Set: `blogId`): 存储用户在收件箱中已读的博文ID。
    *   `TOTAL_UNREAD_COUNT_KEY:{userId}` (Redis Integer): 用户总未读数。
    *   `AUTHOR_UNREAD_COUNT_KEY:{fanId}:{authorId}` (Redis Integer): 用户对特定作者的未读数。
    *   `BLOG_AUTHOR_MAP_KEY:{blogId}` (Redis String/Hash field): `authorId`.
    *   `DELETED_BLOG_HINTS_KEY` (Redis Set): 存储已被作者删除的 `blogId`.

*   **主要操作流程设想**:
    1.  **发布博文 (博主操作)**:
        *   同方案1 (ZADD到`FEED_KEY`, INCR计数器, 更新`BLOG_AUTHOR_MAP_KEY`)。
    2.  **点击博文详情 (粉丝操作，触发已读)**:
        *   前端调用后端 `markAsRead(blogId)` 接口。
        *   后端通过Lua脚本原子执行 (输入: `userId`, `blogId`, `authorId`):
            *   检查 `SISMEMBER READ_IN_FEED_KEY:{userId} {blogId}` 是否已在Set中。
            *   如果不在Set中 (表示首次阅读该feed流中的此博文):
                *   `DECR TOTAL_UNREAD_COUNT_KEY:{userId}` (确保不为负)
                *   `DECR AUTHOR_UNREAD_COUNT_KEY:{userId}:{authorId}` (确保不为负)
                *   `SADD READ_IN_FEED_KEY:{userId} {blogId}`
    3.  **查询关注动态/收件箱 (粉丝操作 `queryBlogOfFollow`)**: 
        *   从 `FEED_KEY:{userId}` 分页获取 `blogId` 列表。
        *   对每个 `blogId`:
            *   查询博文基本内容。
            *   检查 `SISMEMBER DELETED_BLOG_HINTS_KEY {blogId}` 判断是否已删除。
                *   如果已删除:
                    *   标记 `isDeleted = true`。
                    *   检查 `SISMEMBER READ_IN_FEED_KEY:{userId} {blogId}` 是否为false (即删除前是未读的)。
                    *   如果是删除前未读，则调用类似上述"标记已读"的Lua脚本逻辑来原子性扣减未读数，并将此 `blogId` 加入 `READ_IN_FEED_KEY`。
                *   如果未删除: 标记 `isDeleted = false`。
            *   检查 `SISMEMBER READ_IN_FEED_KEY:{userId} {blogId}`，设置 `isRead` 状态。
        *   返回博文列表给前端。
    4.  **获取未读数 (粉丝操作)**:
        *   同方案1 (GET计数器)。
    5.  **删除博文 (博主操作)**:
        *   同方案1 (数据库标记删除, SADD到`DELETED_BLOG_HINTS_KEY`)。

*   **优点 (对比方案1)**:
    *   `READ_IN_FEED_KEY` 使用Set记录已读，相比方案1中使用ZSet (`BLOG_READ_KEY`)，在不需要记录"阅读时间戳"的前提下，存储更轻量，内存占用更小。
    *   其他方面与方案1的优点类似（如未读数准确性、原子操作、删除处理机制）。

*   **缺点/风险 (对比方案1)**:
    *   明确放弃了记录"博文阅读时间戳"的能力。如果未来产品迭代需要此数据进行分析或展示，此方案需要调整。
    *   其他大部分风险与方案1类似（Lua脚本复杂度、`BLOG_AUTHOR_MAP_KEY`维护、推送性能、计数器负数处理等）。

* **DW Confirmation:** This section outlines a second solution. [2024-05-29 11:15:00 +08:00]

---

**方案3讨论简述: 动态/异步计算按作者未读数 或 事件驱动模型**

*   **初步构思**: 
    1.  **动态计算按作者未读数**: 精确维护总未读数，但按作者的未读数在用户请求时，通过拉取用户收件箱（可能全量）和已读列表，在内存中动态聚合计算。主要担忧是对于收件箱内容较多的用户，全量拉取和计算的性能开销过大，难以满足用户对按作者未读数实时性的期望。
    2.  **异步/周期性更新按作者未读数**: 后台任务周期性地计算并更新 `AUTHOR_UNREAD_COUNT_KEY`。此方案会引入数据延迟，与用户期望的实时性有差距。
    3.  **事件驱动模型**: 通过消息队列处理新博文推送和已读事件，由消费者更新各种状态和计数器。此方案系统复杂度高，引入了MQ的维护和最终一致性问题，对于未读数这类需要较高实时性的场景可能不是最优选。
*   **初步结论**: 鉴于用户需求中对总未读数和按作者未读数均有实时性要求，且希望系统不过于复杂，上述方案3的初步构思方向在性能、实时性或复杂度方面存在明显挑战，与方案1和方案2相比，在当前阶段未作为主要候选方案深入展开。我们将主要对比方案1和方案2。

* **DW Confirmation:** Summary of Solution 3 discussion. [2024-05-29 11:25:00 +08:00]

---

**解决方案对比与决策过程:**

| 特性/考虑点             | 方案1 (独立已读ZSET)                                  | 方案2 (收件箱内联已读Set)                               | 备注/偏好                                                                                             | 
| :---------------------- | :---------------------------------------------------- | :------------------------------------------------------ | :--------------------------------------------------------------------------------------------------- |
| **核心设计**            | `BLOG_READ_KEY` (ZSET) 记录已读                       | `READ_IN_FEED_KEY` (Set) 记录已读                         | 方案1记录阅读时间，对未来扩展浏览历史有利。                                                              |
| **已读状态存储**        | ZSet (`blogId` -> `readTimestamp`)                    | Set (`blogId`)                                          | **用户倾向方案1，因考虑未来浏览历史功能。**                                                              |
| **记录阅读时间戳**      | 是                                                    | 否                                                      | **用户认为此特性重要，支持方案1。**                                                                   |
| **总未读数维护**        | `TOTAL_UNREAD_COUNT_KEY` (Integer), Lua原子更新         | `TOTAL_UNREAD_COUNT_KEY` (Integer), Lua原子更新         | 一致                                                                                                 |
| **按作者未读数维护**    | `AUTHOR_UNREAD_COUNT_KEY` (Integer), Lua原子更新      | `AUTHOR_UNREAD_COUNT_KEY` (Integer), Lua原子更新      | 一致 (`{fanId}` 对应 `Follow.userId`, `{authorId}` 对应 `Follow.followUserId`)                               |
| **推送新博文**          | Lua/原子命令: ZADD `FEED_KEY`, INCRs                  | Lua/原子命令: ZADD `FEED_KEY`, INCRs                  | 一致                                                                                                 |
| **标记已读**            | Lua: 检查ZSET, DECRs, ZADD `BLOG_READ_KEY`            | Lua: 检查Set, DECRs, SADD `READ_IN_FEED_KEY`            | 逻辑相似。方案1操作ZSet。                                                                            |
| **查询收件箱性能**      | 分页查`FEED_KEY`, 对每条ZSCORE `BLOG_READ_KEY`         | 分页查`FEED_KEY`, 对每条SISMEMBER `READ_IN_FEED_KEY`    | `ZSCORE` 和 `SISMEMBER` 单次操作均很快，性能差异不大。                                                 |
| **博文删除处理**        | 类似：被动更新计数+标记`DELETED_BLOG_HINTS_KEY`         | 类似：被动更新计数+标记`DELETED_BLOG_HINTS_KEY`         | 一致                                                                                                 |
| **Lua脚本复杂度**       | 中                                                    | 中                                                      | 两者都需要Lua保证原子性，复杂度相近。                                                                  |
| **Redis Key数量**       | 略多一个ZSet用于存readTimestamp                       | 相对更少                                                | 差异不大，方案1的ZSet有明确价值。                                                                      |
| **存储空间**            | `BLOG_READ_KEY` (ZSet) 比 `READ_IN_FEED_KEY` (Set) 略大 | `READ_IN_FEED_KEY` (Set) 更小                             | 用户接受方案1的存储开销以换取功能扩展性。                                                                |
| **满足用户需求**        | 是                                                    | 是 (除未来浏览历史扩展)                                   | 方案1更能满足长期需求。                                                                              |
| **扩展性 (浏览历史)**   | 好，已记录阅读时间                                        | 差，需改动为ZSet                                          | **用户决策的关键点，支持方案1。**                                                                      |

**决策过程会议纪要 (模拟更新):**

*   **日期:** [2024-05-29 11:45:00 +08:00]
*   **主题:** 已读/未读功能方案最终选型 (基于用户反馈)
*   **出席:** PM, PDM, AR, LD, TE, UI/UX, DW
*   **用户反馈:** 用户倾向于方案1，主要因为考虑到未来可能增加"浏览历史"功能，记录阅读时间戳具有前瞻性价值。用户亦提醒注意 `Follow.java` 中的字段名 (`userId` 指粉丝, `followUserId` 指被关注者)。
*   **团队讨论:** 
    *   PM: 根据用户反馈，我们应选择方案1。
    *   AR: 方案1的 `BLOG_READ_KEY` (ZSET) 确实为浏览历史功能打下了良好基础。
    *   LD: 技术上无重大障碍，后续PLAN阶段细化Lua脚本和相关逻辑。
    *   TE: 测试复杂度也类似。
    *   DW: 将确保文档中涉及粉丝和被关注者ID的术语与 `Follow.java` 保持一致。
*   **最终结论:** 选择 **方案1 (基于增强型原子计数器与被动删除处理的方案)** 作为最终实现方案。

**Final Preferred Solution:** **方案1: 基于增强型原子计数器与被动删除处理的方案** (用户已确认)

* **DW Confirmation:** Comparison table and final decision based on user feedback recorded. Ensured terminology alignment with `Follow.java`. [2024-05-29 11:50:00 +08:00]

# 3. Implementation Plan (PLAN Mode Generation - Checklist Format)

**最终选定方案: 方案1 (基于独立已读ZSET与聚合计数)**

**架构文档:** `/project_document/architecture/FanInboxReadUnread_Architecture_v1.0.md` (由AR负责创建和维护)

**Redis Key 定义 (在 `RedisConstants.java` 中):**
*   `TOTAL_UNREAD_COUNT_KEY_PREFIX` (e.g., `"total_unread:"`)
*   `AUTHOR_UNREAD_COUNT_KEY_PREFIX` (e.g., `"author_unread:"`)
*   `BLOG_AUTHOR_MAP_KEY_PREFIX` (e.g., `"blog_author:"`)
*   `DELETED_BLOG_HINTS_KEY_PREFIX` (e.g., `"deleted_blogs"` - 此可能为单个Set Key，无需前缀加ID)
*   (已有的 `FEED_KEY`, `BLOG_READ_KEY` 前缀继续使用)

**Lua 脚本 (存放于 `src/main/resources/lua/`):**
1.  `push_to_fan.lua`
2.  `mark_read.lua` (包含安全递减逻辑)
3.  `handle_deleted_unread.lua` (可复用 `mark_read.lua` 或类似逻辑)

**Implementation Checklist:**

1.  `[P3-AR-001]` **Action:** 完成并归档 `FanInboxReadUnread_Architecture_v1.0.md`，详细定义方案1的Redis Key结构、数据类型、命名规范、精确的Key示例及详细交互流程图。
    *   Rationale: 为后续开发提供清晰的架构指导。
    *   Inputs: 方案1描述，用户反馈。
    *   Outputs: `FanInboxReadUnread_Architecture_v1.0.md` 文档。
    *   Acceptance Criteria: 文档清晰、完整，覆盖所有Key和交互逻辑，Key命名与常量定义匹配。
2.  `[P3-LD-001]` **Action:** 在 `RedisConstants.java` 中定义所有方案1中新增的Redis Key前缀及完整Key名（如果适用，如 `DELETED_BLOG_HINTS_KEY`）。
    *   Rationale: 统一管理常量，便于代码维护。
    *   Inputs: 方案1 Key列表，AR架构文档。
    *   Outputs: 更新后的 `RedisConstants.java`。
    *   Acceptance Criteria: 所有Key定义正确无误，与架构文档一致。
3.  `[P3-LD-002]` **Action:** 设计并实现 `push_to_fan.lua` 脚本。
    *   Inputs: `KEYS[1]=FEED_KEY:{fanId}`, `KEYS[2]=TOTAL_UNREAD_COUNT_KEY:{fanId}`, `KEYS[3]=AUTHOR_UNREAD_COUNT_KEY:{fanId}:{authorId}`. `ARGV[1]=blogId`, `ARGV[2]=pushTimestamp`.
    *   Outputs: `lua/push_to_fan.lua`.
    *   Acceptance Criteria: 脚本原子性地ZADD到FEED, INCR总未读, INCR作者未读。通过单元测试。
4.  `[P3-LD-003]` **Action:** 修改 `BlogServiceImpl.saveBlog()` 方法：在推送博文给粉丝时，获取博文作者ID (`blog.getUserId()`)，然后对每个粉丝调用 `push_to_fan.lua` 脚本。同时确保 `BLOG_AUTHOR_MAP_KEY:{blogId}` 被正确设置为博文的作者ID。
    *   Outputs: 更新后的 `BlogServiceImpl.java`。
    *   Acceptance Criteria: 新博文正确推送给粉丝，相关计数器准确增加，作者映射被存储。
5.  `[P3-LD-004]` **Action:** 设计并实现 `mark_read.lua` 脚本 (包含 `safeDecrement` 逻辑)。
    *   Inputs: `KEYS[1]=BLOG_READ_KEY:{userId}`, `KEYS[2]=TOTAL_UNREAD_COUNT_KEY:{userId}`, `KEYS[3]=AUTHOR_UNREAD_COUNT_KEY:{userId}:{authorId}`. `ARGV[1]=blogId`, `ARGV[2]=currentTimestamp`, `ARGV[3]=authorId` (注意，authorId需传入脚本或通过其他方式在脚本内获取，若从外部传入，则KEYS[3]的构造依赖它)。
    *   Outputs: `lua/mark_read.lua`.
    *   Acceptance Criteria: 脚本原子检查未读、安全DECR计数器、ZADD到已读ZSet。通过单元测试。
6.  `[P3-LD-005]` **Action:** 重构 `BlogServiceImpl.markBlogAsRead(Long blogId)` 方法：获取当前用户ID。获取 `blogId` 对应的 `authorId` (可从 `BLOG_AUTHOR_MAP_KEY` 读取，或要求调用者传入，或从DB读取Blog实体)。调用 `mark_read.lua` 脚本。
    *   Outputs: 更新后的 `BlogServiceImpl.java`。
    *   Acceptance Criteria: 博文被正确标记为已读，相关计数器准确减少，重复调用不影响计数。
7.  `[P3-LD-006]` **Action:** 设计并实现 `handle_deleted_unread.lua` 脚本 (可复用 `mark_read.lua` 的核心逻辑，确保幂等性)。
    *   Outputs: `lua/handle_deleted_unread.lua` (或确认复用 `mark_read.lua`)。
    *   Acceptance Criteria: 脚本能原子性处理已删除的未读博文，更新计数和已读状态。
8.  `[P3-LD-007]` **Action:** 修改 `BlogServiceImpl.queryBlogOfFollow()` 方法：实现新的已读状态判断 (`ZSCORE BLOG_READ_KEY`)。实现删除状态判断 (`SISMEMBER DELETED_BLOG_HINTS_KEY`)，如果已删除且之前未读，则调用 `handle_deleted_unread.lua` (或等效 `mark_read.lua`)。移除旧的未读数更新代码。确保返回给前端的DTO包含 `isRead` (boolean) 和 `isDeleted` (boolean) 字段（可能需要调整 `Blog.java` 或创建新的DTO）。
    *   Outputs: 更新后的 `BlogServiceImpl.java`, 可能需调整 `Blog.java`/DTO。
    *   Acceptance Criteria: 正确展示博文的已读/未读/已删除状态，旧的未读数更新逻辑被移除，删除的未读博文能被正确处理。
9.  `[P3-LD-008]` **Action:** 在 `IBlogService.java` 新增 `getUnreadCounts()` 接口声明，并在 `BlogServiceImpl.java` 中实现：获取当前用户的总未读数 (`GET TOTAL_UNREAD_COUNT_KEY`) 和该用户关注的所有作者的未读数列表 (查询 `tb_follow` 获取关注的 `followUserId` 列表，然后逐个 `GET AUTHOR_UNREAD_COUNT_KEY`)。
    *   Outputs: 更新后的 `IBlogService.java`, `BlogServiceImpl.java`。
    *   Acceptance Criteria: 接口能正确返回用户的总未读数及每个关注作者的未读数 (为0也应体现)。
10. `[P3-LD-009]` **Action:** 在博文被作者删除的业务逻辑处（例如 `BlogServiceImpl`中处理删除的方法，或博文对应Controller的删除方法），增加调用：`SADD DELETED_BLOG_HINTS_KEY {blogId}`。
    *   Outputs: 更新博文删除相关的服务或控制器代码。
    *   Acceptance Criteria: 博文ID被正确加入到 `DELETED_BLOG_HINTS_KEY` Set中。
11. `[P3-LD-010]` **Action:** (辅助任务) 实现从 `BLOG_AUTHOR_MAP_KEY:{blogId}` 获取 `authorId` 的辅助方法，或确保在需要 `authorId` 的地方 (如`markBlogAsRead`, `queryBlogOfFollow`处理删除博文时) 能可靠获取到 `authorId`。
    *   Outputs: 可能的辅助方法或代码调整。
    *   Acceptance Criteria: `authorId` 能被准确获取。
12. `[P3-TE-001]` **Action:** 制定并执行详细的单元测试（针对Java逻辑和Lua脚本分别编写）和集成测试用例。
    *   Outputs: 测试用例，测试报告。
    *   Acceptance Criteria: 核心逻辑100%测试通过，覆盖边界条件、并发场景。
13. `[P3-DW-001]` **Action:** 更新所有相关的项目文档（README、API文档等）。
    *   Outputs: 更新后的文档。
    *   Acceptance Criteria: 文档与实现一致。
14. `[P3-UIUX-001]` **Action:** (PDM/UI/UX 协同) 最终确认未读数在前端各版块的展示样式、位置及刷新逻辑。确认"已被作者删除"的笔记在前端的UI展示样式。
    *   Outputs: UI/UX确认稿或规范。
    *   Acceptance Criteria: 前端展示符合产品要求。

* **DW Confirmation:** Implementation checklist created and detailed. [2024-05-29 12:15:00 +08:00]

# 4. Current Execution Step (EXECUTE Mode - Updated when starting a step)

# 5. Task Progress (EXECUTE Mode - Appended after each step/node)

# 6. Final Review (REVIEW Mode Population)
* **DW Confirmation:** 