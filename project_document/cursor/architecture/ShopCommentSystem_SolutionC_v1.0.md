# Solution C: Cache Integration & Comprehensive Statistics

*Timestamp: [YYYY-MM-DD HH:MM:SS UMT]*

## 1. Core Idea
Build upon Solution B by introducing caching for frequently accessed comment data and more robust statistics updates.

## 2. Data Models
*   `ShopComment`, `CommentLike`, `CommentReport` entities similar to Solution B.
### 2.1. `ShopStatistics` Entity (or extend `Shop`)
*   `shopId` (Long, PK/FK)
*   `totalComments` (Long)
*   `averageRating` (Double)
*   `fiveStarCount`, `fourStarCount`, etc. (for rating distribution)

## 3. Service Layer (`ShopCommentServiceImpl`)
*   `addComment`: Publish message to MQ (or reliable async event) for a dedicated statistics service to update `ShopStatistics`.
*   `getCommentsByShop`: Attempt to read from Redis cache first (e.g., paginated lists, hot comments). On cache miss, query DB and populate cache.
*   `likeComment`: Async update `ShopComment.likesCount`, invalidate/update relevant caches.
*   Other services similar to Solution B.

## 4. Image Handling
*   Similar to Solution B.

## 5. Cache Design (Redis)
*   `cache:shop_comments:shopId:{shopId}:page:{pageNum}`
*   `cache:shop_comments:shopId:{shopId}:hot`
*   `cache:user_likes:comment:{commentId}` (Set of user IDs)
*   `cache:shop_stats:{shopId}` (Hash for `ShopStatistics`)

## 6. Pros
*   Excellent read performance for high-traffic scenarios.
*   Accurate and independently managed statistics.
*   Smoother user experience due to caching.

## 7. Cons
*   Highest complexity; introduces cache and potentially MQ maintenance.
*   Cache consistency challenges (invalidation strategies).

## 8. Update Log
*   [YYYY-MM-DD HH:MM:SS UMT] Initial draft by AR.