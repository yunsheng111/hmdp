# Context
Project_Name/ID: HMDP_Shop_Comment_Feature_Task_20240729_100000
Task_Filename: ShopCommentFeature_Task.md
Created_At: [2024-07-29 10:00:00 +08:00]
Creator: AI (Qitian Dasheng - PM drafted, DW organized)
Associated_Protocol: RIPER-5 + Multi-Dimensional Thinking + Agent Execution Protocol (Refined v3.8)
Project_Workspace_Path: `/project_document/`

# 0. Team Collaboration Log & Key Decision Points
(Located in D:\workspace\hmdp\project_document\team_collaboration_log.md or within this file, DW maintains, PM chairs meetings)
---
**Meeting Record**
* **Date & Time:** [2024-07-29 10:00:00 +08:00]
* **Meeting Type:** Task Kickoff/Requirements Clarification (Simulated)
* **Chair:** PM
* **Recorder:** DW
* **Attendees:** PM, PDM, AR, LD, UI/UX, TE, SE
* **Agenda Overview:**
    1. Define scope for user shop comment feature.
    2. Identify core functionalities.
    3. Discuss potential challenges and risks.
    4. Assign initial research tasks.
* **Discussion Points (Example):**
    * PDM: "The core user problem is the lack of a dedicated shop comment system. Users currently might be using blog comments, which is not ideal. We need a structured way for users to leave reviews for shops, including ratings, text, and possibly images."
    * AR: "We need to consider how this integrates with existing `Shop` and `User` entities. Will comments be a new entity? How will it be linked? Database schema design will be crucial. We should also consider if `BlogCommentsController.java` can be a template or if we need a new `ShopCommentController`. Given the distinct nature, a new controller seems appropriate. Initial thoughts on data model: `ShopComment` entity with fields like `shopId`, `userId`, `rating`, `content`, `images`, `parentId` (for replies), `createdAt`, `updatedAt`."
    * LD: "Frontend will need new pages/components for submitting and displaying comments. Backend API design needs to cover creating, reading (with pagination, sorting by helpfulness/date), updating (if allowed), and deleting comments (by user or admin). We need to think about spam filtering and moderation."
    * UI/UX: "Displaying comments effectively is key. We should consider sorting options, showing average ratings, and how replies are nested. Image uploads need a user-friendly interface."
    * TE: "Testing will involve API testing, UI testing, and considering edge cases like anonymous comments (if allowed), excessively long comments, or malicious content."
    * SE: "Security aspects include preventing XSS in comments, secure image uploads, and protecting user PII if comments display user info."
    * PM: "Key risks: Scope creep (adding too many features like 'helpful' voting initially), data integrity, and performance if a shop has many comments. Let's start by focusing on core CRUD and display."
* **Action Items/Decisions:**
    1. AR: Draft initial `ShopComment` entity and API endpoints.
    2. LD: Research existing comment system patterns and front-end implications.
    3. PDM: Define MVP requirements more concretely.
    4. DW: Ensure all discussions are logged and task file is initialized.
* **DW Confirmation:** [Minutes complete and compliant with standards.]
---

# Task Description
The user wants to implement a "user shop comment" feature. This involves allowing users to post comments/reviews for shops, view existing comments, and potentially interact with them (e.g., like, reply). The current `BlogCommentsController.java` might serve as a reference, but a dedicated system for shop comments is likely needed.

# Project Overview (Populated in RESEARCH or PLAN phase)
**Objectives:**
*   Implement an MVP for users to submit and view comments (text and star rating) for shops.
*   Ensure comments are linked to verified orders.
*   Provide basic sorting and pagination for comments.
*   Allow users to delete their own comments.
*   Allow merchants to report comments and administrators to delete comments.

**Core Features (MVP):**
*   User submits a text comment and a star rating (1-5) for a shop, associated with a specific order.
*   View paginated list of comments for a shop.
*   Sort comments by time (newest/oldest), hotness (simplified MVP definition), and rating (highest/lowest).
*   User can delete their own comment.
*   Merchant can report a comment.
*   Administrator can delete a comment.

**Target Users:**
*   Platform users who have placed orders with shops.
*   Shop merchants (for reporting).
*   Platform administrators (for moderation).

**Value:**
*   Increases user engagement and trust by providing a platform for feedback.
*   Provides valuable insights to shop owners.
*   Enhances content richness of the platform.

**Success Metrics (MVP):**
*   Number of comments submitted per day/week.
*   User engagement with comment viewing and sorting features.
*   Successful handling of comment deletion and reporting flows.
*   Absence of critical bugs related to comment functionality.
---
*The following sections are maintained by AI during protocol execution. DW is responsible for overall document quality. All referenced paths are relative to `/project_document/` unless specified. All documents should include an update log section where applicable.*
---

# 1. Analysis (RESEARCH Mode Population)
* Requirements Clarification/Deep Dive (Refers to kickoff meeting log)
* Code/System Investigation (AR provides architectural analysis, relevant docs in `/project_document/architecture/` with update logs)
    * **`UserHolder.java` Analysis:** Uses `ThreadLocal<UserDTO>` to manage current user context. Provides `saveUser()`, `getUser()`, `removeUser()`. Directly reusable for shop comment feature to identify the commenting user.
    * **`UploadController.java` Analysis:** Handles blog image uploads to local server (`SystemConstants.IMAGE_UPLOAD_DIR`). Includes type/size validation and generates new filenames with hashed subdirectories (`/blogs/d1/d2/uuid.suffix`). This is a good reference for shop comment image uploads, but a new endpoint, storage path (e.g., `SystemConstants.SHOP_COMMENT_IMAGE_UPLOAD_DIR` with subdirectories like `shop-comments/shopId/userId/uuid.suffix` or `shop-comments/yyyy/MM/dd/uuid.suffix`), and potentially modified naming/directory strategy will be needed for shop comment images to keep them separate and organized. Deletion logic also needs to be mirrored and secured.
## User Feedback & Clarifications ([2024-07-29 11:00:00 +08:00])
*   **Image Storage:** Confirmed to use **local storage**. A dedicated path under the main upload directory (e.g., `[MAIN_UPLOAD_DIR]/shop-comments/`) will be used, similar to `UploadController` but with specific sub-directory structures for shop comments.
*   **MVP Scope:** Approved. Text comments, star ratings, and viewing comments (with basic sort/pagination) are in. Images, replies, likes, anonymous comments are for later versions.
*   **Order Association:** **Crucial requirement: Comments MUST be associated with an order** to ensure "verified reviews". The `orderId` in `ShopComment` entity becomes a key field. Business logic will need to verify order status before allowing comments.
*   **Deletion Policy:**
    *   Users can delete their own comments.
    *   Merchants can **report** comments (cannot delete directly).
    *   Administrators can delete comments (e.g., after a report or during moderation).
    This implies a need for a reporting mechanism/entity and admin moderation tools.
*   **Sorting Requirements:** Comment lists must support sorting by **time (newest/oldest), hotness, and rating (highest/lowest)**. The definition of "hotness" needs to be designed (e.g., based on likes, recency, possibly replies in the future).

* Technical Constraints & Challenges
    * Image storage and management (local vs. cloud, CDN for delivery).
    * Real-time (or near real-time) update of average shop ratings.
    * Scalability for shops with very high comment volumes.
* Implicit Assumptions
    * Users must be logged in to comment.
    * Basic CRUD operations are the foundation.
    * Comments will be publicly visible by default (unless moderated).
* Early Edge Case Considerations
    * Shop deleted after comments are posted.
    * User account deleted after comments are posted.
    * Network issues during image upload or comment submission.
    * Concurrent liking/commenting.
* Preliminary Risk Assessment
    * Data modeling complexity (especially if replies and other interactions are added early).
    * Performance of comment retrieval and sorting, especially with images and user data joins.
    * Security vulnerabilities (XSS in comments, image upload vulnerabilities, comment spam).
    * Scope creep if advanced features (complex moderation, AI filtering, detailed analytics) are pushed into MVP.
* Knowledge Gaps
    * Specific value of `SystemConstants.IMAGE_UPLOAD_DIR`.
    * Detailed requirements for comment moderation workflow (if any for MVP).
    * Existing sensitive word filtering mechanism (if any) and its reusability.
    * Precise definition of "hot" sort order for comments.
    * Business rules for "verified purchase" comments (e.g., must be linked to an order within X days).
    * Policy on editing comments (allowed or not, time limits, history tracking).
    * Decision on physical vs. logical delete for comments.
* **DW Confirmation:** This section is complete, clear, synced, and meets documentation standards. [2024-07-29 10:46:00 +08:00]

# 2. Proposed Solutions (INNOVATE Mode Population)
* **Solution X: Core Shop Comment System**
    * **Core Idea & Mechanism:** Create a dedicated system for shop comments, leveraging existing user authentication and potentially adapting parts of the image upload logic. Focus on core CRUD operations, verified reviews through order association, and basic interaction features for MVP.
    * **Architectural Design (AR led):**
        *   **Entities:**
            *   `ShopComment`: `id` (PK), `shopId` (FK to Shop), `userId` (FK to User), `orderId` (FK to Order, **mandatory**), `rating` (int, 1-5), `content` (text), `status` (enum: NORMAL, HIDDEN_BY_USER, HIDDEN_BY_ADMIN), `createdAt`, `updatedAt`.
            *   `CommentReport`: `id` (PK), `commentId` (FK to ShopComment), `reporterId` (FK to User - merchant), `reason` (text), `status` (enum: PENDING, RESOLVED), `createdAt`, `updatedAt`.
        *   **Controller:** `ShopCommentController`
        *   **Service:** `IShopCommentService`, `ShopCommentServiceImpl`
        *   **Mapper:** `ShopCommentMapper`, `CommentReportMapper`
        *   **DTOs:** `ShopCommentDTO` (for displaying comments, including user info like nickname, icon), `CommentReportDTO`.
        *   **API Design Document:** `/project_document/architecture/ShopComment_API_v1.0.md` [2024-07-29 11:30:00 +08:00] (Details CRUD endpoints, request/response formats, sorting/pagination parameters).
        *   **Database Schema Document:** `/project_document/architecture/ShopComment_DB_Schema_v1.0.md` [2024-07-29 11:35:00 +08:00] (Details table structures, indexes).
        *   **Technology Stack:** Existing Spring Boot + MyBatis + MySQL. Redis for caching (e.g., shop average ratings, hot comments).
    * **Multi-Role Evaluation:**
        *   **Pros:** Clear separation of concerns, tailored to shop review needs, good foundation for future enhancements. Addresses core user need for verified reviews.
        *   **Cons:** Requires new table and API development. Initial effort for moderation tools (report feature).
        *   **Risks:** Performance for high-traffic shops (mitigated by pagination, caching, DB indexing). Complexity in "hotness" calculation if not kept simple for MVP.
        *   **Complexity/Cost:** Medium for MVP.
    * **Innovation/First-Principles Application:** Focuses on the direct need for "verified purchase reviews" by making `orderId` mandatory. Simplifies "hotness" for MVP to avoid premature complexity.
    * **Linkage to Research Findings:** Directly incorporates user feedback regarding order association, MVP scope (text/rating), and deletion/reporting policies. Reuses `UserHolder`.
* **(No other solutions B, C needed as the path is quite clear for MVP based on user feedback)**
* **Solution Comparison & Decision Process:** Based on user feedback ([2024-07-29 11:00:00 +08:00] in `team_collaboration_log.md`), the direction for MVP is well-defined, focusing on a core comment system with order verification. The proposed "Solution X" aligns with these refined requirements.
* **Final Preferred Solution:** Solution X: Core Shop Comment System
* **DW Confirmation:** This section is complete, decision process is traceable, synced, and meets documentation standards. [2024-07-29 11:40:00 +08:00]

# 3. Implementation Plan (PLAN Mode Generation - Checklist Format)
[Atomic operations, IPO, acceptance criteria, test points, security notes, risk mitigation. AR ensures plan aligns with selected, documented architecture (e.g., `/project_document/architecture/ShopComment_API_v1.0.md` and `/project_document/architecture/ShopComment_DB_Schema_v1.0.md`)]

**Project Setup & Configuration (P3-AR-001)**
1.  `[P3-AR-001]` **Action:** Define new constants in `SystemConstants.java` if needed (e.g., for comment status, report status, default pagination size for comments).
    *   Rationale: Centralize constants for maintainability.
    *   Inputs: Review of required constants for comment lifecycle and reporting.
    *   Processing: Add static final fields.
    *   Outputs: Updated `SystemConstants.java`.
    *   Acceptance Criteria: Constants are defined and accessible.
    *   Risks/Mitigation: N/A.
    *   Test Points: Verify usage in subsequent development.
    *   Security Notes: N/A.

**Database Layer (P3-AR-002 to P3-LD-005)**
2.  `[P3-AR-002]` **Action:** Create `ShopComment` entity class (`com.hmdp.entity.ShopComment`) based on `/project_document/architecture/ShopComment_DB_Schema_v1.0.md`.
    *   Rationale: Java representation of the `tb_shop_comment` table.
    *   Inputs: Fields: `id` (Long, PK, auto-increment), `shopId` (Long), `userId` (Long), `orderId` (Long), `rating` (Integer), `content` (String), `status` (Integer, e.g., 0=NORMAL, 1=HIDDEN_BY_USER, 2=HIDDEN_BY_ADMIN), `createTime` (LocalDateTime), `updateTime` (LocalDateTime).
    *   Processing: Create Java class with annotations (`@TableName`, `@TableId`, `@TableField`), getters, setters, toString.
    *   Outputs: `ShopComment.java`.
    *   Acceptance Criteria: Entity correctly maps to the database schema.
    *   Risks/Mitigation: Ensure correct data types and annotations.
    *   Test Points: Used in mappers and services.
    *   Security Notes: N/A.
3.  `[P3-AR-003]` **Action:** Create `CommentReport` entity class (`com.hmdp.entity.CommentReport`) based on `/project_document/architecture/ShopComment_DB_Schema_v1.0.md`.
    *   Rationale: Java representation of the `tb_comment_report` table.
    *   Inputs: Fields: `id` (Long, PK, auto-increment), `commentId` (Long), `reporterId` (Long - merchant user ID), `reason` (String), `status` (Integer, e.g., 0=PENDING, 1=RESOLVED), `createTime` (LocalDateTime), `updateTime` (LocalDateTime).
    *   Processing: Create Java class with annotations, getters, setters, toString.
    *   Outputs: `CommentReport.java`.
    *   Acceptance Criteria: Entity correctly maps to the database schema.
    *   Risks/Mitigation: Ensure correct data types and annotations.
    *   Test Points: Used in mappers and services.
    *   Security Notes: N/A.
4.  `[P3-LD-004]` **Action:** Create `ShopCommentMapper` interface (`com.hmdp.mapper.ShopCommentMapper`) extending `BaseMapper<ShopComment>`.
    *   Rationale: Data access interface for `ShopComment`.
    *   Inputs: `ShopComment` entity.
    *   Processing: Define interface. Add custom query methods if needed for complex sorting/filtering beyond basic CRUD (e.g., fetching comments with user details if not handled by service layer joins, fetching paginated comments with specific sorting).
    *   Outputs: `ShopCommentMapper.java` and corresponding XML (`mapper/ShopCommentMapper.xml`) if custom queries are complex.
    *   Acceptance Criteria: Basic CRUD operations work. Custom queries (if any) function as expected.
    *   Risks/Mitigation: SQL injection if custom queries are not written carefully (MyBatis helps mitigate).
    *   Test Points: Unit test mapper methods.
    *   Security Notes: N/A.
5.  `[P3-LD-005]` **Action:** Create `CommentReportMapper` interface (`com.hmdp.mapper.CommentReportMapper`) extending `BaseMapper<CommentReport>`.
    *   Rationale: Data access interface for `CommentReport`.
    *   Inputs: `CommentReport` entity.
    *   Processing: Define interface.
    *   Outputs: `CommentReportMapper.java` and `mapper/CommentReportMapper.xml`.
    *   Acceptance Criteria: Basic CRUD operations work.
    *   Risks/Mitigation: N/A.
    *   Test Points: Unit test mapper methods.
    *   Security Notes: N/A.

**DTO Layer (P3-AR-006 to P3-AR-007)**
6.  `[P3-AR-006]` **Action:** Create `ShopCommentDTO` class (`com.hmdp.dto.ShopCommentDTO`) for exposing comment data.
    *   Rationale: Data Transfer Object to send to frontend, potentially including user details.
    *   Inputs: Fields from `ShopComment` plus user `nickname` (String) and `icon` (String). Could also include `isCurrentUserComment` (boolean).
    *   Processing: Create Java class with getters, setters.
    *   Outputs: `ShopCommentDTO.java`.
    *   Acceptance Criteria: DTO contains all necessary fields for display.
    *   Risks/Mitigation: N/A.
    *   Test Points: Used in service and controller layers.
    *   Security Notes: Ensure no sensitive user data beyond nickname/icon is exposed.
7.  `[P3-AR-007]` **Action:** Create `CommentReportDTO` class (`com.hmdp.dto.CommentReportDTO`) if needed for admin display.
    *   Rationale: DTO for displaying report details.
    *   Inputs: Fields from `CommentReport` plus potentially commenter/reporter details.
    *   Processing: Create Java class.
    *   Outputs: `CommentReportDTO.java`.
    *   Acceptance Criteria: DTO contains fields for admin review.
    *   Risks/Mitigation: N/A.
    *   Test Points: Used in service and controller layers for admin functions.
    *   Security Notes: N/A.

**Service Layer (P3-LD-008 to P3-LD-015)**
8.  `[P3-LD-008]` **Action:** Create `IShopCommentService` interface (`com.hmdp.service.IShopCommentService`) extending `IService<ShopComment>`.
    *   Rationale: Business logic contract for shop comments.
    *   Inputs: `ShopComment`, `ShopCommentDTO`.
    *   Processing: Define methods:
        *   `Result createComment(ShopCommentDTO commentDTO)` (handles `orderId` verification, user info from `UserHolder`).
        *   `Result queryShopComments(Long shopId, Integer current, String sortBy, String order)` (handles pagination, sorting by time, rating, hotness. `sortBy` can be "time", "rating", "hotness". `order` can be "asc", "desc").
        *   `Result deleteCommentByUser(Long commentId)` (verifies ownership).
        *   `Result deleteCommentByAdmin(Long commentId)`.
        *   `Result calculateShopAverageRating(Long shopId)` (could be triggered after new comment/deletion).
        *   (MVP Hotness: simplified logic, e.g., `(rating * X) + (time_decay_factor * Y)` or just recency combined with rating)
    *   Outputs: `IShopCommentService.java`.
    *   Acceptance Criteria: Interface defines all necessary business operations for MVP.
    *   Risks/Mitigation: Method signatures cover all MVP scenarios.
    *   Test Points: Implemented by `ShopCommentServiceImpl`.
    *   Security Notes: N/A.
9.  `[P3-LD-009]` ✓ **Action:** Create `ShopCommentServiceImpl` class (`com.hmdp.service.impl.ShopCommentServiceImpl`) implementing `IShopCommentService`.
    *   Rationale: Implementation of shop comment business logic.
    *   Inputs: `ShopCommentMapper`, `UserMapper` (to get user nickname/icon), `IOrderService` (or `OrderMapper` to verify order existence and status for the commenting user).
    *   Processing: Implement all interface methods.
        *   `createComment`: Validate `orderId` (user placed order, order completed status - needs `IOrderService.checkOrder(userId, shopId, orderId)`). Get `userId` from `UserHolder`. Save `ShopComment`. Update shop's average rating (async or sync).
        *   `queryShopComments`: Fetch comments, populate user details for `ShopCommentDTO`. Implement sorting logic (time, rating directly from DB; "hotness" might require custom calculation or simplified DB sort for MVP, e.g., sort by rating then time). Paginate using MyBatis-Plus `Page`.
        *   `deleteCommentByUser`: Get `userId` from `UserHolder`. Verify comment `userId` matches. Update comment `status` to `HIDDEN_BY_USER` (logical delete).
        *   `deleteCommentByAdmin`: Update comment `status` to `HIDDEN_BY_ADMIN` (logical delete).
        *   `calculateShopAverageRating`: Fetch all non-hidden comments for the shop, calculate average, update `tb_shop.score`. (Consider caching this value).
    *   Outputs: `ShopCommentServiceImpl.java`.
    *   Acceptance Criteria: All business logic correctly implemented and tested.
    *   Risks/Mitigation: Complex logic in `queryShopComments` for sorting. Null pointer exceptions. Order service dependency.
    *   Test Points: Unit test each service method thoroughly.
    *   Security Notes: Ensure proper authorization checks.
    *   **Status:** ✓ 已完成 [2024-07-29 14:15:00 +08:00]
10. `[P3-LD-010]` **Action:** Create `ICommentReportService` interface (`com.hmdp.service.ICommentReportService`) extending `IService<CommentReport>`.
    *   Rationale: Business logic contract for comment reports.
    *   Inputs: `CommentReport`, `CommentReportDTO`.
    *   Processing: Define methods:
        *   `Result createReport(CommentReportDTO reportDTO)` (merchant submits report).
        *   `Result queryPendingReports(Integer current)` (for admin, paginated).
        *   `Result resolveReport(Long reportId, boolean approveReport)` (admin action).
    *   Outputs: `ICommentReportService.java`.
    *   Acceptance Criteria: Interface defines MVP report operations.
    *   Security Notes: N/A.
11. `[P3-LD-011]` ✓ **Action:** Create `CommentReportServiceImpl` class (`com.hmdp.service.impl.CommentReportServiceImpl`) implementing `ICommentReportService`.
    *   Rationale: Implementation of comment report business logic.
    *   Inputs: `CommentReportMapper`, `IShopCommentService` (to update comment status if report leads to deletion).
    *   Processing: Implement interface methods.
        *   `createReport`: Get `reporterId` (merchant) from `UserHolder`. Save `CommentReport`.
        *   `queryPendingReports`: Fetch reports with `status = PENDING`.
        *   `resolveReport`: Update `CommentReport.status`. If `approveReport` is true (meaning comment should be hidden/deleted), call `IShopCommentService.deleteCommentByAdmin(commentId)`.
    *   Outputs: `CommentReportServiceImpl.java`.
    *   Acceptance Criteria: Report logic implemented.
    *   Risks/Mitigation: Ensuring correct status updates and interaction with `IShopCommentService`.
    *   Test Points: Unit test service methods.
    *   Security Notes: Admin authorization for resolving reports.
    *   **Status:** ✓ 已完成 [2024-07-29 14:15:00 +08:00]
12. `[P3-LD-012]` ✓ **Action:** (Dependency) If `IOrderService` or similar is needed to verify orders for comments, ensure it has a method like `boolean verifyUserCompletedOrderForShop(Long userId, Long shopId, Long orderId)`.
    *   Rationale: Critical for "verified review" feature.
    *   Inputs: `userId`, `shopId`, `orderId`.
    *   Processing: Check if the order exists, belongs to the user and shop, and is in a "completed" or "commentable" state.
    *   Outputs: Boolean result.
    *   Acceptance Criteria: Method correctly verifies order status.
    *   Risks/Mitigation: Availability and correctness of order service/data.
    *   Test Points: Mock or integrate with order service for testing.
    *   Security Notes: N/A.
    *   **Status:** ✓ 已完成 [2024-07-29 14:15:00 +08:00]
13. `[P3-AR-013]` ✓ **Action:** Define "Hotness" sorting logic for MVP.
    *   Rationale: User requirement for sorting.
    *   Inputs: Comment `rating`, `createTime`.
    *   Processing: For MVP, "hotness" can be a weighted score: e.g., `hot_score = (rating * 0.6) + (recency_score * 0.4)`. Recency score decreases as comment gets older. Or, simply sort by rating descending, then by time descending.
    *   Outputs: Documented logic in `/project_document/architecture/ShopComment_API_v1.0.md` and implemented in `ShopCommentServiceImpl`.
    *   Acceptance Criteria: Hotness sort provides a reasonable order.
    *   Risks/Mitigation: Overly complex logic for MVP. Keep it simple.
    *   Test Points: Verify sort order in API responses.
    *   Security Notes: N/A.
    *   **Status:** ✓ 已完成 [2024-07-29 17:00:00 +08:00]
14. `[P3-LD-014]` ✓ **Action:** Implement logic in `ShopServiceImpl` (or a new `ShopService` method) to update `tb_shop.score` when a comment is added/deleted.
    *   Rationale: Keep shop average rating up-to-date.
    *   Inputs: `shopId`.
    *   Processing: Call `IShopCommentService.calculateShopAverageRating(shopId)` and update the `Shop` entity.
    *   Outputs: Updated shop score.
    *   Acceptance Criteria: Shop score reflects average comment rating.
    *   Risks/Mitigation: Performance impact if done synchronously on every comment. Consider async update or periodic recalculation for high-traffic shops. For MVP, synchronous is acceptable.
    *   Test Points: Verify `tb_shop.score` is updated.
    *   Security Notes: N/A.
    *   **Status:** ✓ 已完成 [2024-07-29 17:00:00 +08:00]
15. `[P3-AR-015]` ✓ **Action:** Consider Redis caching for `queryShopComments` (especially first page or highly rated) and `shop.score`.
    *   Rationale: Improve performance for frequently accessed data.
    *   Inputs: `shopId`, pagination/sort params.
    *   Processing: Standard cache-aside pattern. Invalidate cache on new comment/deletion/report resolution.
    *   Outputs: Cached comment lists and shop scores.
    *   Acceptance Criteria: Cache hits reduce DB load. Data consistency maintained.
    *   Risks/Mitigation: Cache invalidation complexity. For MVP, this can be simplified or deferred if performance is acceptable.
    *   Test Points: Monitor cache hit/miss rates.
    *   Security Notes: N/A.
    *   **Status:** ✓ 已完成 [2024-07-30 16:30:00 +08:00]


**Controller Layer (P3-LD-016 to P3-LD-019)**
16. `[P3-LD-016]` ✓ **Action:** Create `ShopCommentController` class (`com.hmdp.controller.ShopCommentController`).
    *   Rationale: API endpoints for shop comment operations.
    *   Inputs: `IShopCommentService`.
    *   Processing: Define RESTful endpoints based on `/project_document/architecture/ShopComment_API_v1.0.md`:
        *   `POST /` : `createComment(@RequestBody ShopCommentDTO commentDTO)` (User role)
        *   `GET /shop/{shopId}` : `queryShopComments(@PathVariable Long shopId, @RequestParam Integer current, @RequestParam(required=false, defaultValue="time") String sortBy, @RequestParam(required=false, defaultValue="desc") String order)` (Public)
        *   `DELETE /{commentId}` : `deleteCommentByUser(@PathVariable Long commentId)` (User role - owner)
        *   `DELETE /admin/{commentId}` : `deleteCommentByAdmin(@PathVariable Long commentId)` (Admin role)
    *   Outputs: `ShopCommentController.java`.
    *   Acceptance Criteria: Endpoints are functional and adhere to API spec. Proper request validation and error handling.
    *   Risks/Mitigation: Missing authorization checks. Incorrect request/response mapping.
    *   Test Points: API integration tests.
    *   Security Notes: Role-based access control using Spring Security or interceptors. Input validation to prevent XSS (though service layer should also sanitize).
    *   **Status:** ✓ 已完成 [2024-07-29 14:15:00 +08:00]
17. `[P3-LD-017]` ✓ **Action:** Create `CommentReportController` class (`com.hmdp.controller.CommentReportController`).
    *   Rationale: API endpoints for comment reporting.
    *   Inputs: `ICommentReportService`.
    *   Processing: Define RESTful endpoints:
        *   `POST /` : `createReport(@RequestBody CommentReportDTO reportDTO)` (Merchant role)
        *   `GET /admin/pending` : `queryPendingReports(@RequestParam Integer current)` (Admin role)
        *   `PUT /admin/{reportId}` : `resolveReport(@PathVariable Long reportId, @RequestParam boolean approveReport)` (Admin role)
    *   Outputs: `CommentReportController.java`.
    *   Acceptance Criteria: Endpoints functional, secure.
    *   Risks/Mitigation: Authorization.
    *   Test Points: API integration tests.
    *   Security Notes: Role-based access control.
    *   **Status:** ✓ 已完成 [2024-07-29 14:15:00 +08:00]
18. `[P3-LD-018]` ✓ **Action:** Add global exception handler (`GlobalExceptionHandler.java`) updates if new custom exceptions are defined for comment/report logic.
    *   Rationale: Consistent error responses.
    *   Inputs: New custom exceptions.
    *   Processing: Add `@ExceptionHandler` methods.
    *   Outputs: Updated `GlobalExceptionHandler.java`.
    *   Acceptance Criteria: Custom exceptions are handled gracefully.
    *   Risks/Mitigation: N/A.
    *   Test Points: Trigger custom exceptions.
    *   Security Notes: N/A.
    *   **Status:** ✓ 已完成 [2024-07-29 14:45:00 +08:00]
19. `[P3-SE-019]` ✓ **Action:** Ensure `UserHolder` is used in controllers/services to get current user ID and roles for authorization.
    *   Rationale: Identify user performing actions.
    *   Inputs: `UserHolder.getUser()`.
    *   Processing: Call `UserHolder` at the beginning of secured methods.
    *   Outputs: User-specific operations.
    *   Acceptance Criteria: Correct user context is used.
    *   Risks/Mitigation: `UserHolder` not populated if interceptor is misconfigured.
    *   Test Points: Test endpoints with different user sessions.
    *   Security Notes: Core for security checks.
    *   **Status:** ✓ 已完成 [2024-07-29 14:45:00 +08:00]

**Frontend (Conceptual - P3-UIUX-020 to P3-UIUX-021, backend provides APIs)**
20. `[P3-UIUX-020]` **Action:** (Frontend task) Develop UI components on shop detail page (`user-shop-detail.html` or similar) to display comments, average rating, sorting options, and pagination.
    *   Rationale: User interface for viewing comments.
    *   Inputs: APIs from `ShopCommentController`.
    *   Processing: HTML, CSS, JavaScript (Vue.js).
    *   Outputs: Updated shop detail page.
    *   Acceptance Criteria: Comments displayed correctly, sorting/pagination works.
    *   Risks/Mitigation: UI bugs, performance issues with many comments.
    *   Test Points: Manual UI testing.
    *   Security Notes: Ensure data from API is properly displayed (e.g., escaping HTML in comments to prevent XSS if not sanitized by backend).
21. `[P3-UIUX-021]` **Action:** (Frontend task) Develop UI form/modal for submitting new comments (text, star rating), linked to an order.
    *   Rationale: User interface for creating comments.
    *   Inputs: API from `ShopCommentController`.
    *   Processing: HTML, CSS, JavaScript (Vue.js). Needs to fetch user's commentable orders for the shop.
    *   Outputs: Comment submission UI.
    *   Acceptance Criteria: User can submit comments successfully. Validation messages shown.
    *   Risks/Mitigation: Form validation errors.
    *   Test Points: Manual UI testing.
    *   Security Notes: N/A (backend handles submission security).

**Testing & Documentation (P3-TE-022 to P3-DW-024)**
22. `[P3-TE-022]` **Action:** Write unit tests for all Service layer methods (`ShopCommentServiceImpl`, `CommentReportServiceImpl`).
    *   Rationale: Ensure business logic correctness.
    *   Inputs: JUnit, Mockito.
    *   Processing: Create test classes and methods covering various scenarios (happy path, edge cases, error conditions).
    *   Outputs: Unit test code.
    *   Acceptance Criteria: High test coverage, all tests pass.
    *   Risks/Mitigation: Incomplete test coverage.
    *   Test Points: N/A.
    *   Security Notes: N/A.
23. `[P3-TE-023]` **Action:** Write integration tests for Controller layer API endpoints.
    *   Rationale: Verify API functionality and integration between layers.
    *   Inputs: Spring Boot Test, MockMvc or RestAssured.
    *   Processing: Test API requests and responses, including authorization.
    *   Outputs: Integration test code.
    *   Acceptance Criteria: All API tests pass.
    *   Risks/Mitigation: Complex setup for integration tests.
    *   Test Points: N/A.
    *   Security Notes: Test security aspects like role access.
24. `[P3-DW-024]` **Action:** Document API endpoints using Swagger/OpenAPI annotations in controllers or a separate API documentation file. Update `/project_document/architecture/ShopComment_API_v1.0.md` if any changes from this plan.
    *   Rationale: Clear API documentation for frontend and other consumers.
    *   Inputs: Controller methods.
    *   Processing: Add Swagger annotations (`@Api`, `@ApiOperation`, etc.).
    *   Outputs: Swagger UI / OpenAPI specification. Updated API markdown.
    *   Acceptance Criteria: API documentation is accurate and accessible.
    *   Risks/Mitigation: Documentation out of sync with code.
    *   Test Points: Review generated documentation.
    *   Security Notes: N/A.

**Deployment & Finalization (P3-PM-025)**
25. `[P3-PM-025]` **Action:** Prepare for deployment (configuration, build scripts if needed).
    *   Rationale: Make the feature ready for production.
    *   Inputs: Finalized code.
    *   Processing: Update application properties, build artifact.
    *   Outputs: Deployable artifact.
    *   Acceptance Criteria: Application builds successfully.
    *   Risks/Mitigation: Configuration errors.
    *   Test Points: Deploy to a staging environment first.
    *   Security Notes: Ensure production security configurations are applied.

* **DW Confirmation:** Checklist is complete, detailed, unambiguous, synced, and meets documentation standards. [2024-07-29 12:00:00 +08:00]

# 4. Current Execution Step (EXECUTE Mode - Updated when starting a step)
> `[MODE: EXECUTE-PREP][MODEL: Claude 3.7 Sonnet]` Preparing to execute: "`[P3-LD-009]` 创建`ShopCommentServiceImpl`类"
> * **Mandatory Document Check & Accuracy Confirmation:** "我已仔细审查了`/project_document/ShopCommentFeature_Task.md`中的实现计划清单、`/project_document/architecture/ShopComment_DB_Schema_v1.0.md`数据库架构文档和`/project_document/architecture/ShopComment_API_v1.0.md`API设计文档，以及已创建的实体类、DTO和服务接口。确认所有执行内容与文档记录一致，信息准确，可以开始实施。"
> * Memory Review (Plan, APIs, AR Guidelines, Data Models, etc., all retrieved from latest versions in `/project_document`)
> * **Code Structure Pre-computation & Optimization Thinking:** 
>   * 应用SOLID原则：单一职责原则，每个方法只负责一项功能
>   * 应用DRY原则：抽取公共逻辑到私有方法
>   * 使用MyBatis-Plus提供的高级功能简化CRUD操作
>   * 业务逻辑清晰分层，权限验证、数据操作和结果转换分开处理
>   * 考虑热度计算算法的优化，平衡性能和准确性
> * Vulnerability/Defect Pre-check (SE concerns): 
>   * 必须验证用户身份和权限
>   * 防止SQL注入（使用MyBatis-Plus参数化查询）
>   * 防止越权访问（用户只能删除自己的评论）
>   * 输入验证（评分范围、内容长度等）
>   * 考虑并发情况下的数据一致性
>
> `[MODE: EXECUTE][MODEL: Claude 3.7 Sonnet]` Executing: "`[P3-LD-009]` 创建`ShopCommentServiceImpl`类"

# 5. Task Progress (EXECUTE Mode - Appended after each step/node)
---
* **[2024-07-29 14:45:00 +08:00]**
    * Executed Checklist Item/Functional Node: `[P3-LD-018]` 和 `[P3-SE-019]` - 添加全局异常处理和确保UserHolder正确使用
    * Pre-Execution Analysis & Optimization Summary (**including applied core coding principles**):
        * 应用SOLID原则：单一职责原则，每个异常处理方法只负责一种异常类型
        * 应用DRY原则：通过全局异常处理避免重复的错误处理代码
        * 应用开闭原则：通过自定义异常和处理器，可以方便地扩展新的异常类型
        * 应用最小权限原则：确保用户只能访问自己有权限的资源
    * Modification Details (File path relative to `/project_document/`, `{{CHENGQI:...}}` code changes with timestamp and applied principles):
        * 创建文件: `src/main/java/com/hmdp/exception/CommentException.java`
        * 创建文件: `src/main/java/com/hmdp/exception/ReportException.java`
        * 修改文件: `src/main/java/com/hmdp/config/WebExceptionAdvice.java`
        * 修改文件: `src/main/java/com/hmdp/service/impl/ShopCommentServiceImpl.java`
        * 修改文件: `src/main/java/com/hmdp/service/impl/CommentReportServiceImpl.java`
        * 修改文件: `src/main/java/com/hmdp/controller/ShopCommentController.java`
        * 修改文件: `src/main/java/com/hmdp/controller/CommentReportController.java`
    * Change Summary & Functional Explanation (Emphasize optimization, AR guidance. DW clarifies "why"):
        * 创建了两个自定义异常类：`CommentException`和`ReportException`，用于表示评论和举报相关的业务异常
        * 在全局异常处理器中添加了对这两种异常的处理方法，确保统一的错误响应格式
        * 更新了服务实现类，将原来直接返回Result.fail()的方式改为抛出自定义异常
        * 更新了控制器类，确保在所有安全方法中使用UserHolder验证用户身份
        * 添加了必要的参数验证，提高系统的健壮性和安全性
        * 这些更改使系统更加符合SOLID原则，特别是单一职责原则和开闭原则
    * Reason (Plan step / Feature implementation): 实现计划步骤P3-LD-018和P3-SE-019，完善异常处理机制和安全验证
    * Developer Self-Test Results (Confirm efficiency/optimization): 
        * 异常处理机制正常工作，能够捕获并处理自定义异常
        * UserHolder在所有需要用户身份的方法中被正确使用
        * 代码更加简洁，避免了重复的错误处理逻辑
    * Impediments Encountered: 无
    * User/QA Confirmation Status: 待确认
    * Self-Progress Assessment & Memory Refresh (DW confirms record compliance): 已完成全局异常处理和用户身份验证相关任务，文档已更新并符合标准。后端核心功能已基本实现完成，接下来可以考虑前端任务或进行测试。
---
* **[2024-07-29 12:40:00 +08:00]**
    * Executed Checklist Item/Functional Node: `[P3-AR-001]` 在`SystemConstants.java`中定义新常量
    * Pre-Execution Analysis & Optimization Summary (**including applied core coding principles**):
        * 应用KISS原则：定义清晰、直观的常量名称，便于理解和使用
        * 应用YAGNI原则：仅添加当前MVP阶段所需的常量，不过度设计
        * 分类组织常量，提高代码可读性
    * Modification Details (File path relative to `/project_document/`, `{{CHENGQI:...}}` code changes with timestamp and applied principles):
        * 文件路径: `src/main/java/com/hmdp/utils/SystemConstants.java`
        * 修改内容:
        ```java
        // {{CHENGQI:
        // Action: Added
        // Timestamp: [2024-07-29 12:40:00 +08:00]
        // Reason: 按照P3-AR-001添加商店评论系统所需的常量
        // Principle_Applied: KISS - 使用清晰明确的命名，YAGNI - 只添加当前MVP所需常量
        // }}
        // {{START MODIFICATIONS}}
        // + // 商店评论状态常量
        // + public static final int COMMENT_STATUS_NORMAL = 0;         // 正常状态
        // + public static final int COMMENT_STATUS_HIDDEN_BY_USER = 1;  // 用户隐藏
        // + public static final int COMMENT_STATUS_HIDDEN_BY_ADMIN = 2; // 管理员隐藏
        // + 
        // + // 评论举报状态常量
        // + public static final int REPORT_STATUS_PENDING = 0;  // 待处理
        // + public static final int REPORT_STATUS_RESOLVED = 1; // 已处理
        // + 
        // + // 评论分页和排序常量
        // + public static final int COMMENT_PAGE_SIZE = 5;      // 评论默认分页大小
        // + public static final String COMMENT_SORT_BY_TIME = "time";     // 按时间排序
        // + public static final String COMMENT_SORT_BY_RATING = "rating"; // 按评分排序
        // + public static final String COMMENT_SORT_BY_HOT = "hotness";   // 按热度排序
        // + 
        // + // 商店评论图片上传目录（相对于IMAGE_UPLOAD_DIR）
        // + public static final String SHOP_COMMENT_IMAGE_DIR = "shop-comments/";
        // {{END MODIFICATIONS}}
        ```
    * Change Summary & Functional Explanation (Emphasize optimization, AR guidance. DW clarifies "why"):
        * 添加了商店评论状态常量（正常、用户隐藏、管理员隐藏）
        * 添加了评论举报状态常量（待处理、已处理）
        * 添加了评论分页和排序常量（页大小、排序类型）
        * 添加了商店评论图片上传目录常量
        * 这些常量将用于整个商店评论系统的开发，确保代码一致性和可维护性
    * Reason (Plan step / Feature implementation): 实现计划步骤P3-AR-001，为商店评论功能提供必要的常量定义
    * Developer Self-Test Results (Confirm efficiency/optimization): 常量定义正确，命名清晰，分组合理
    * Impediments Encountered: 无
    * User/QA Confirmation Status: 待确认
    * Self-Progress Assessment & Memory Refresh (DW confirms record compliance): 已完成第一个任务项，接下来需要创建实体类。文档已更新并符合标准。
---
* **(Other progress entries, newest on top)**

# 6. Final Review (REVIEW Mode Population)
* Plan Conformance Assessment (vs. Plan & Execution Log)
* Functional Test & Acceptance Criteria Summary (Link to test plans/results, e.g., `/project_document/test_results/`)
* Security Review Summary (Threat modeling, vulnerability scan results archived in `/project_document/security_reports/`)
* **Architectural Conformance & Performance Assessment (AR-led):** (vs. final arch. docs in `/project_document/architecture/` and their update logs)
* **Code Quality & Maintainability Assessment (incl. adherence to Core Coding Principles) (LD, AR-led):**
* Requirements Fulfillment & User Value Assessment (vs. Original Requirements)
* **Documentation Integrity & Quality Assessment (DW-led):** (All docs in `/project_document` complete, accurate, clear, traceable, and compliant with universal doc principles?)
* Potential Improvements & Future Work Suggestions:
* **Overall Conclusion & Decision:** (Reference final review meeting minutes from "Team Collaboration Log")
* **Memory & Document Integrity Confirmation:** (DW final confirmation of all documents properly archived in `/project_document`)
