-- 处理已删除的未读博客
-- KEYS[1]: BLOG_READ_KEY:{userId} - 用户的已读博客列表
-- KEYS[2]: TOTAL_UNREAD_COUNT_KEY:{userId} - 用户的总未读数
-- KEYS[3]: AUTHOR_UNREAD_COUNT_KEY:{userId}:{authorId} - 用户对特定作者的未读数
-- ARGV[1]: blogId - 博客ID
-- ARGV[2]: currentTimestamp - 当前时间戳

-- 检查博客是否已经被标记为已读（如果已读，则不需要更新计数）
local isRead = redis.call('ZSCORE', KEYS[1], ARGV[1])
if isRead then
    -- 如果已读，则直接返回当前未读数
    local totalCount = redis.call('GET', KEYS[2]) or '0'
    return totalCount
end

-- 将博客标记为已读（实际上是标记为"已处理删除"）
redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])

-- 安全地减少总未读数
local totalCount = redis.call('GET', KEYS[2])
if totalCount and tonumber(totalCount) > 0 then
    redis.call('DECR', KEYS[2])
end

-- 安全地减少对特定作者的未读数
local authorCount = redis.call('GET', KEYS[3])
if authorCount and tonumber(authorCount) > 0 then
    redis.call('DECR', KEYS[3])
end

-- 返回更新后的总未读数
local newTotalCount = redis.call('GET', KEYS[2]) or '0'
return newTotalCount 