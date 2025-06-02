**Meeting Record - Log**


---
**Meeting Record - User Feedback Incorporation**
* **Date & Time:** [2024-07-29 11:00:00 +08:00]
* **Meeting Type:** User Feedback Review & Requirement Update (Simulated)
* **Chair:** PM
* **Recorder:** DW
* **Attendees:** PM, PDM, AR, LD, TE
* **Agenda Overview:** Review user feedback on RESEARCH phase outputs and update requirements for Shop Comment Feature.
* **Discussion Points & Decisions based on User Feedback:**
    1.  **Image Storage:** Confirmed: **Local storage** to be used. AR to define specific sub-directory structure under `SystemConstants.IMAGE_UPLOAD_DIR` (or equivalent primary upload dir) for shop comment images, ensuring separation from blog images (e.g., `.../uploads/shop-comments/[shopId]/[orderId_or_commentId]/image.jpg`).
    2.  **MVP Scope:** Confirmed: MVP includes text comments, star ratings, viewing comments with pagination and sorting (time, hotness, rating). Images, replies, likes, anonymous features are post-MVP.
    3.  **Order Association:** Confirmed: Comments **must be linked to an `orderId`**. This enforces "verified reviews". 
        *   AR: `ShopComment.orderId` is mandatory. 
        *   LD: Service layer needs to validate the order's existence and status (e.g., user must have a completed order for the shop to comment). This may require interaction with an `IOrderService`.
    4.  **Deletion Policy:** 
        *   Users can delete their own comments (LD: API to verify ownership).
        *   Merchants can **report** comments. (AR: Requires a `CommentReport` entity and table. LD: API for reporting. UI/UX: Merchant frontend needs a report button/flow. PDM: Define report reasons and admin workflow).
        *   Admins can delete comments. (LD: Admin API for deletion. UI/UX: Admin backend needs interface for managing reports and comments).
    5.  **Sorting Requirements:** Confirmed: Sort by **time (newest/oldest), hotness, and rating (highest/lowest)**. 
        *   AR/LD: The definition and calculation logic for "hotness" needs to be designed. Initial thought: `hotness_score = (likes_count * w1) + (replies_count * w2) - (time_decay_factor * w3)`. Weights (w1, w2, w3) and decay factor to be determined.
* **Impact Assessment & Next Steps:**
    *   PDM: Update user stories and acceptance criteria based on these clarifications.
    *   AR: Refine `ShopComment` entity, design `CommentReport` entity. Update API contract drafts.
    *   LD: Plan for service layer logic değişiklikleri, especially order validation and report handling.
    *   TE: Add new test cases for order-linked comments, report/delete flows, and sorting.
* **DW Confirmation:** User feedback and subsequent decisions/impacts logged. [2024-07-29 11:05:00 +08:00]
---
**Meeting Record**
* **Date & Time:** [2024-07-29 10:00:00 +08:00]
* **Meeting Type:** Task Kickoff/Requirements Clarification (Simulated) - Shop Comment Feature
* **Chair:** PM
* **Recorder:** DW
* **Attendees:** PM, PDM, AR, LD, UI/UX, TE, SE
* **Agenda Overview:**
    1. Define scope for user shop comment feature.
    2. Identify core functionalities.
    3. Discuss potential challenges and risks.
    4. Assign initial research tasks.
* **Discussion Points:**
    * PDM: "The core user problem is the lack of a dedicated shop comment system. Users currently might be using blog comments, which is not ideal. We need a structured way for users to leave reviews for shops, including ratings, text, and possibly images. This will enhance user trust and provide valuable feedback to shop owners."
    * AR: "This will necessitate a new entity, let's call it `ShopComment`. It needs to be linked to both `Shop` (one-to-many: one shop has many comments) and `User` (one-to-many: one user can make many comments). Key fields for `ShopComment` could include: `id` (PK), `shopId` (FK), `userId` (FK), `orderId` (FK, optional, if comments are linked to specific orders), `parentId` (for replies, self-referencing FK), `content` (text), `rating` (e.g., 1-5 stars), `imageUrls` (list/JSON of image paths), `isAnonymous` (boolean), `status` (e.g., pending, approved, rejected), `createdAt`, `updatedAt`. We should create a new `ShopCommentController`, `IShopCommentService`, and `ShopCommentServiceImpl`. `BlogCommentsController.java` is a good reference for structure but functionality will differ. For example, shop comments are usually tied to a specific shop, whereas blog comments are tied to a blog post."
    * LD: "API endpoints needed: POST `/shop-comments` (create), GET `/shop-comments/shop/{shopId}` (list by shop, with pagination and sorting by date, rating, helpfulness), GET `/shop-comments/user/{userId}` (list by user), PUT `/shop-comments/{commentId}` (update, if allowed), DELETE `/shop-comments/{commentId}` (delete by user or admin). We'll also need a way to handle replies, potentially POST `/shop-comments/{parentId}/reply`. Image uploads need to be handled, similar to the existing `UploadController` but perhaps with specific validation for comment images. Spam/profanity filtering is a must; consider integrating a third-party service or a simple keyword-based filter initially. A 'helpful' or 'like' button for comments could be a V2 feature."
    * UI/UX: "Frontend will require new UI components. On the shop detail page (`user-shop-detail.html`), we'll need a section to display comments, including average rating, individual comment cards (showing user avatar, name, rating, text, images, date), and pagination. A form for submitting new comments will be needed, potentially as a modal or an inline section. This form should include a star rating input, a text area, and an image uploader. Display of replies should be clearly nested. Sorting options (newest, oldest, highest/lowest rating) are important for users."
    * TE: "Test cases will cover: creating comments with/without images, with/without ratings; viewing comments for a shop; pagination; sorting; replying; editing (if allowed); deleting. Edge cases: empty comments, very long comments, multiple image uploads, concurrent comment submissions. We also need to test access control: only the comment owner or an admin can delete/edit. Performance testing for shops with thousands of comments is crucial."
    * SE: "Security: Prevent XSS by sanitizing comment text. Ensure secure image uploads (file type validation, size limits, potentially scanning for malware). If `orderId` is linked, ensure only users who made an order can comment (or give them a 'verified purchase' badge). Protect against comment spamming (rate limiting, CAPTCHA). Data privacy: Consider what user information is displayed with the comment and if anonymous comments are allowed."
    * PM: "This is a significant feature. MVP should focus on: users posting text comments and star ratings for shops, and viewing these comments. Replies and image uploads can be V1.1. 'Helpful' voting can be V2. Let's keep it manageable. Initial risk assessment: data modeling complexity, performance of comment retrieval, and ensuring a good UX for comment submission and display. Ensuring data integrity and preventing abuse are also key."
* **Action Items/Decisions:**
    1.  AR: Draft the `ShopComment` entity schema and define preliminary API contracts for CRUD operations. Create `/project_document/architecture/shop_comment_system_design_v0.1.md`. (Due: EOD)
    2.  LD: Investigate existing system components (e.g., `UploadController`, `UserHolder`) that can be reused or adapted. Analyze `BlogCommentsController.java` for common patterns. (Due: EOD)
    3.  PDM: Detail the MVP user stories and acceptance criteria for shop comments (text & rating only for MVP). (Due: EOD)
    4.  UI/UX: Sketch initial wireframes for comment display and submission form on the shop detail page. (Due: Next meeting)
    5.  DW: Ensure all discussion points and decisions are logged in `team_collaboration_log.md` and `ShopCommentFeature_Task.md` is updated. (Ongoing)
* **DW Confirmation:** Minutes complete and compliant with standards. [2024-07-29 10:15:00 +08:00]
---
