# Solution B: Enhanced Interaction & Asynchronous Processing

*Timestamp: [YYYY-MM-DD HH:MM:SS UMT]*

## 1. Core Idea
Build upon MVP with designs for future interactions (likes, replies) and introduce asynchronous processing for statistics.

## 2. Data Models
### 2.1. `ShopComment` Entity
*   (Same as Solution A, plus)
*   `likesCount` (Integer, Default 0)
*   `replyCount` (Integer, Default 0) (for future replies)
*   `isFeatured` (Boolean, Default false)

### 2.2. `CommentLike` Entity
*   `id` (Long, PK)
*   `commentId` (Long, FK to `ShopComment`, Indexed)
*   `userId` (Long, FK to `User`, Indexed)
*   `createTime` (LocalDateTime)
*   (Unique constraint on `commentId` + `userId`)

### 2.3. `CommentReport` Entity
*   (Same as Solution A)

## 3. Service Layer (`ShopCommentServiceImpl`)
*   `addComment`: Asynchronously update shop rating/comment count (e.g., Spring ApplicationEvent or MQ).
*   `getCommentsByShop`: Hotness sort based on `likesCount` (+ `replyCount`) and `createTime`.
*   `likeComment(commentId, UserDTO currentUser)`: Manage `CommentLike` records and asynchronously update `ShopComment.likesCount`.
*   `setFeaturedComment(commentId, isAdmin)`.
*   Other services similar to Solution A.

## 4. Image Handling
*   Not in MVP. Consider JSON field in `ShopComment` (e.g., `imageUrls`) or a separate `ShopCommentImage` table for future.

## 5. Pros
*   Better main API performance (asynchronous stats).
*   Good extensibility for likes/replies.
*   Basic hotness sorting is feasible.

## 6. Cons
*   Slightly increased complexity due to async processing.
*   `CommentLike` table adds DB write operations.

## 7. Detailed Design (To be populated based on team selection)

### 7.1. Entity Relationship Diagram (ERD)
```mermaid
erDiagram
    USER ||--o{ SHOP_COMMENT : creates
    SHOP ||--o{ SHOP_COMMENT : comments_on
    ORDER ||--o{ SHOP_COMMENT : validates
    SHOP_COMMENT ||--o{ COMMENT_LIKE : has_likes
    USER ||--o{ COMMENT_LIKE : likes
    SHOP_COMMENT ||--o{ COMMENT_REPORT : has_reports
    MERCHANT ||--o{ COMMENT_REPORT : reports
    SHOP_COMMENT ||--o{ SHOP_COMMENT_IMAGE : has_images (Post-MVP)

    SHOP_COMMENT {
        BIGINT id PK
        BIGINT shopId FK
        BIGINT userId FK
        BIGINT orderId FK "Mandatory, for verified review"
        TEXT content
        INT rating "1-5 stars"
        INT status "0-Normal, 1-UserDeleted, 2-AdminDeleted, 3-ReportedPending"
        INT likesCount "Default 0"
        INT replyCount "Default 0, for future use"
        BOOLEAN isFeatured "Default false, admin set"
        DATETIME createTime
        DATETIME updateTime
    }

    COMMENT_LIKE {
        BIGINT id PK
        BIGINT commentId FK
        BIGINT userId FK
        DATETIME createTime
        UNIQUE (commentId, userId)
    }

    COMMENT_REPORT {
        BIGINT id PK
        BIGINT commentId FK
        BIGINT reporterId FK "Merchant.id"
        VARCHAR reportReason
        DATETIME reportTime
        INT status "0-Pending, 1-ProcessedValid, 2-ProcessedInvalid"
        VARCHAR adminNotes
    }

    SHOP_COMMENT_IMAGE {
        BIGINT id PK
        BIGINT commentId FK
        VARCHAR imageUrl
        INT sortOrder
        DATETIME createTime
    }
```

### 7.2. API Endpoint Definitions (Preliminary)
*   **POST** `/api/shop-comments` (Create a new comment)
*   **GET** `/api/shop-comments/shop/{shopId}` (Get comments for a shop with pagination and sorting)
*   **DELETE** `/api/shop-comments/{commentId}` (User deletes their own comment)
*   **POST** `/api/shop-comments/{commentId}/like` (User likes/unlikes a comment)
*   **POST** `/api/shop-comments/report` (Merchant reports a comment)
*   **PUT** `/api/admin/shop-comments/{commentId}/status` (Admin updates comment status, e.g., delete, feature)
*   **GET** `/api/admin/shop-comments/reports` (Admin views reported comments)

### 7.3. Key Service Logic Flow (Example: Add Comment)
1.  Controller receives request (`ShopCommentDTO`, `UserDTO` from `UserHolder`).
2.  Service validates `orderId` (exists, belongs to user & shop, order status allows commenting - interacts with `IOrderService`).
3.  Service validates comment content (length, profanity - basic filter initially).
4.  Service saves `ShopComment` to DB with initial status.
5.  Service publishes an asynchronous event (e.g., Spring ApplicationEvent) with `shopId` and `rating` for statistics update.
6.  (Separate asynchronous listener) updates shop's average rating and total comment count in `Shop` table or a dedicated `ShopStatistics` table.


## 8. Update Log
*   [YYYY-MM-DD HH:MM:SS UMT] Initial draft by AR.
*   [YYYY-MM-DD HH:MM:SS UMT] Populated with ERD, API endpoints, and service logic flow based on team selection of Solution B by DW.