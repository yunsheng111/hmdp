# Solution A: Basic Core Functionality (MVP Oriented)

*Timestamp: [YYYY-MM-DD HH:MM:SS UMT]*

## 1. Core Idea
Focus on rapid implementation of MVP features, with subsequent iterations for enhancements.

## 2. Data Models
### 2.1. `ShopComment` Entity
*   `id` (Long, PK)
*   `shopId` (Long, FK, Indexed)
*   `userId` (Long, FK, Indexed)
*   `orderId` (Long, FK, Indexed, **Mandatory**)
*   `content` (String, Text)
*   `rating` (Integer) - 1-5 stars
*   `status` (Integer: 0-Normal, 1-UserDeleted, 2-AdminDeleted, 3-ReportedPending)
*   `createTime` (LocalDateTime)
*   `updateTime` (LocalDateTime)

### 2.2. `CommentReport` Entity
*   `id` (Long, PK)
*   `commentId` (Long, FK to `ShopComment`)
*   `reporterId` (Long, FK to `Merchant.id`)
*   `reportReason` (String)
*   `reportTime` (LocalDateTime)
*   `status` (Integer: 0-Pending, 1-ProcessedValid, 2-ProcessedInvalid)
*   `adminNotes` (String, Optional)

## 3. Service Layer (`ShopCommentServiceImpl`)
*   `addComment(ShopComment comment, UserDTO currentUser)`
*   `getCommentsByShop(shopId, page, sortType, sortOrder)` (Hotness sort simplified to `createTime` DESC for MVP)
*   `deleteCommentByUser(commentId, UserDTO currentUser)`
*   `reportComment(commentId, reporterId, reason)`
*   `deleteCommentByAdmin(commentId)`

## 4. Image Handling
*   Not included in MVP.

## 5. Pros
*   Fast development, quick to market with core features.
*   Low complexity, easy to understand and maintain.

## 6. Cons
*   Limited hotness sorting in MVP.
*   Image/reply features require significant future work.
*   Synchronous shop rating updates might impact performance.

## 7. Update Log
*   [YYYY-MM-DD HH:MM:SS UMT] Initial draft by AR.