-- 将博客推送给粉丝并更新未读计数
-- KEYS[1]: FEED_KEY:{fanId} - 粉丝的收件箱
-- KEYS[2]: TOTAL_UNREAD_COUNT_KEY:{fanId} - 粉丝的总未读数
-- KEYS[3]: AUTHOR_UNREAD_COUNT_KEY:{fanId}:{authorId} - 粉丝对特定作者的未读数
-- ARGV[1]: blogId - 博客ID
-- ARGV[2]: pushTimestamp - 推送时间戳

-- 1. 将博客添加到粉丝的收件箱
redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])

-- 2. 增加粉丝的总未读数
local totalCount = redis.call('GET', KEYS[2])
if not totalCount then
    -- 如果不存在，则初始化为1
    redis.call('SET', KEYS[2], '1')
else
    -- 如果存在，则递增
    redis.call('INCR', KEYS[2])
end

-- 3. 增加粉丝对特定作者的未读数
local authorCount = redis.call('GET', KEYS[3])
if not authorCount then
    -- 如果不存在，则初始化为1
    redis.call('SET', KEYS[3], '1')
else
    -- 如果存在，则递增
    redis.call('INCR', KEYS[3])
end

-- 返回操作后的总未读数
return redis.call('GET', KEYS[2]) 