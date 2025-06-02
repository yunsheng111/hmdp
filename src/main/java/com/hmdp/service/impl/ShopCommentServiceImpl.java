package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.common.Result;
import com.hmdp.dto.ShopCommentDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopComment;
import com.hmdp.entity.User;
import com.hmdp.event.ShopScoreUpdateEvent;
import com.hmdp.exception.CommentException;
import com.hmdp.mapper.ShopCommentMapper;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IShopCommentService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.BooleanUtil;

/**
 * 商店评论服务实现类
 */
@Service
public class ShopCommentServiceImpl extends ServiceImpl<ShopCommentMapper, ShopComment> implements IShopCommentService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ShopMapper shopMapper;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Result createComment(ShopCommentDTO commentDTO) {
        // 1. 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new CommentException("用户未登录");
        }

        // 2. 参数校验
        if (commentDTO.getShopId() == null) {
            throw new CommentException("商店ID不能为空");
        }
        if (commentDTO.getOrderId() == null) {
            throw new CommentException("订单ID不能为空");
        }
        if (commentDTO.getRating() == null || commentDTO.getRating() < 1 || commentDTO.getRating() > 5) {
            throw new CommentException("评分必须在1-5之间");
        }
        if (StrUtil.isBlank(commentDTO.getContent())) {
            throw new CommentException("评论内容不能为空");
        }

        // 3. 验证商店是否存在
        Shop shop = shopMapper.selectById(commentDTO.getShopId());
        if (shop == null) {
            throw new CommentException("商店不存在");
        }

        // 4. 验证订单是否存在且属于当前用户
        // TODO: 这里需要调用订单服务验证订单是否存在、属于当前用户且已完成
        // 暂时使用一个假的验证方法，实际项目中需要替换为真实的订单验证逻辑
        boolean orderValid = verifyUserCompletedOrderForShop(user.getId(), commentDTO.getShopId(), commentDTO.getOrderId());
        if (!orderValid) {
            throw new CommentException("订单验证失败，可能原因：订单不存在、不属于当前用户、未完成或已评论");
        }

        // 5. 检查该订单是否已经评论过
        LambdaQueryWrapper<ShopComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopComment::getOrderId, commentDTO.getOrderId())
                .eq(ShopComment::getUserId, user.getId())
                .eq(ShopComment::getShopId, commentDTO.getShopId());
        if (count(queryWrapper) > 0) {
            throw new CommentException("该订单已经评论过");
        }

        // 6. 创建评论
        ShopComment comment = new ShopComment();
        comment.setShopId(commentDTO.getShopId());
        comment.setUserId(user.getId());
        comment.setOrderId(commentDTO.getOrderId());
        comment.setRating(commentDTO.getRating());
        comment.setContent(commentDTO.getContent());
        comment.setStatus(SystemConstants.COMMENT_STATUS_NORMAL);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        // 7. 保存评论
        save(comment);

        // 8. 更新商店平均评分
        calculateShopAverageRating(commentDTO.getShopId());
        
        // 9. 删除相关缓存，让缓存失效，下次查询时重新加载
        cleanCommentCache(commentDTO.getShopId());

        // 10. 返回评论信息，包括用户信息
        ShopCommentDTO resultDTO = BeanUtil.copyProperties(comment, ShopCommentDTO.class);
        User commentUser = userMapper.selectById(user.getId());
        if (commentUser != null) {
            resultDTO.setUserNickname(commentUser.getNickName());
            resultDTO.setUserIcon(commentUser.getIcon());
        }

        return Result.success(resultDTO);
    }

    @Override
    public Result queryShopComments(Long shopId, Integer current, String sortBy, String order) {
        // 1. 参数校验
        if (shopId == null) {
            throw new CommentException("商店ID不能为空");
        }

        // 2. 验证商店是否存在
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new CommentException("商店不存在");
        }

        // 3. 设置默认值
        current = current == null ? 1 : current;
        sortBy = StrUtil.isBlank(sortBy) ? SystemConstants.COMMENT_SORT_BY_TIME : sortBy;
        order = StrUtil.isBlank(order) ? "desc" : order.toLowerCase();
        
        // 4. 构建缓存key
        String cacheKey = RedisConstants.CACHE_SHOP_COMMENTS_KEY + shopId + ":" + sortBy + ":" + order + ":" + current;
        
        // 5. 尝试从缓存中获取
        String cacheResult = stringRedisTemplate.opsForValue().get(cacheKey);
        
        // 如果缓存命中，直接返回
        if (StrUtil.isNotBlank(cacheResult)) {
            // 判断是否为空值（缓存穿透保护）
            if ("{}".equals(cacheResult)) {
                return Result.success(new HashMap<>());
            }
            // 反序列化并返回
            return Result.success(JSONUtil.toBean(cacheResult, Map.class));
        }
        
        // 6. 缓存未命中，尝试获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_COMMENTS_KEY + shopId + ":" + sortBy + ":" + order + ":" + current;
        boolean getLock = tryLock(lockKey);
        
        try {
            // 双重检查，再次尝试从缓存获取
            if (!getLock) {
                // 获取锁失败，等待一段时间后重试
                Thread.sleep(50);
                return queryShopComments(shopId, current, sortBy, order);
            }
            
            // 再次检查缓存，可能在获取锁的过程中已经有其他线程重建了缓存
            cacheResult = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cacheResult)) {
                if ("{}".equals(cacheResult)) {
                    return Result.success(new HashMap<>());
                }
                return Result.success(JSONUtil.toBean(cacheResult, Map.class));
            }
            
            // 7. 热度排序特殊处理
            Result result;
            if (SystemConstants.COMMENT_SORT_BY_HOT.equals(sortBy)) {
                result = queryHotComments(shopId, current, order);
                
                // 将热门评论结果缓存起来，设置较长的过期时间
                String resultJson = JSONUtil.toJsonStr(result.getData());
                stringRedisTemplate.opsForValue().set(
                    cacheKey, 
                    resultJson, 
                    RedisConstants.CACHE_SHOP_HOT_COMMENTS_TTL, 
                    TimeUnit.HOURS
                );
            } else {
                // 8. 常规排序查询
                // 创建分页对象
                Page<ShopComment> page = new Page<>(current, SystemConstants.COMMENT_PAGE_SIZE);

                // 构建查询条件
                QueryWrapper<ShopComment> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("shop_id", shopId)
                        .eq("status", SystemConstants.COMMENT_STATUS_NORMAL);

                // 根据排序方式设置排序条件
                if (SystemConstants.COMMENT_SORT_BY_TIME.equals(sortBy)) {
                    queryWrapper.orderBy(true, "desc".equals(order), "create_time");
                } else if (SystemConstants.COMMENT_SORT_BY_RATING.equals(sortBy)) {
                    queryWrapper.orderBy(true, "desc".equals(order), "rating");
                } else {
                    // 默认按时间排序
                    queryWrapper.orderByDesc("create_time");
                }

                // 执行分页查询
                page = page(page, queryWrapper);
                result = convertPageToDTO(page);
                
                // 将结果缓存起来，设置过期时间
                String resultJson = JSONUtil.toJsonStr(result.getData());
                
                // 如果查询结果为空，仍然缓存，但使用较短的过期时间（缓存穿透保护）
                if (page.getRecords().isEmpty()) {
                    stringRedisTemplate.opsForValue().set(
                        cacheKey, 
                        "{}", 
                        RedisConstants.CACHE_NULL_TTL, 
                        TimeUnit.MINUTES
                    );
                } else {
                    stringRedisTemplate.opsForValue().set(
                        cacheKey, 
                        resultJson, 
                        RedisConstants.CACHE_SHOP_COMMENTS_TTL, 
                        TimeUnit.MINUTES
                    );
                }
            }
            
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException("查询评论时发生异常", e);
        } finally {
            // 释放互斥锁
            unlock(lockKey);
        }
    }
    
    /**
     * 尝试获取分布式锁
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(
            key, 
            "1", 
            RedisConstants.LOCK_SHOP_COMMENTS_TTL, 
            TimeUnit.SECONDS
        );
        return BooleanUtil.isTrue(flag);
    }
    
    /**
     * 释放分布式锁
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
    
    /**
     * 查询热门评论
     */
    private Result queryHotComments(Long shopId, Integer current, String order) {
        // 创建分页对象
        Page<ShopComment> page = new Page<>(current, SystemConstants.COMMENT_PAGE_SIZE);

        // 构建查询条件，先按时间排序获取评论
        QueryWrapper<ShopComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId)
                .eq("status", SystemConstants.COMMENT_STATUS_NORMAL)
                .orderByDesc("create_time");
        
        // 执行分页查询
        page = page(page, queryWrapper);
        List<ShopComment> records = page.getRecords();
        
        // 计算每条评论的热度得分
        List<Map.Entry<ShopComment, Double>> commentScores = new ArrayList<>();
        for (ShopComment comment : records) {
            double score = calculateHotnessScore(comment);
            commentScores.add(new AbstractMap.SimpleEntry<>(comment, score));
        }
        
        // 根据热度得分排序
        if ("asc".equals(order)) {
            commentScores.sort(Map.Entry.comparingByValue());
        } else {
            commentScores.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
        }
        
        // 更新分页记录
        List<ShopComment> sortedComments = commentScores.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        page.setRecords(sortedComments);
        
        // 转换为DTO并返回
        return convertPageToDTO(page);
    }

    @Override
    public Result deleteCommentByUser(Long commentId) {
        // 1. 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new CommentException("用户未登录");
        }

        // 2. 参数校验
        if (commentId == null) {
            throw new CommentException("评论ID不能为空");
        }

        // 3. 查询评论
        ShopComment comment = getById(commentId);
        if (comment == null) {
            throw new CommentException("评论不存在");
        }

        // 4. 验证评论所有者
        if (!comment.getUserId().equals(user.getId())) {
            throw new CommentException("无权限删除该评论");
        }

        // 5. 软删除评论（更新状态）
        comment.setStatus(SystemConstants.COMMENT_STATUS_HIDDEN_BY_USER);
        comment.setUpdateTime(LocalDateTime.now());
        updateById(comment);

        // 6. 更新商店平均评分
        calculateShopAverageRating(comment.getShopId());
        
        // 7. 删除相关缓存
        cleanCommentCache(comment.getShopId());

        return Result.success();
    }

    @Override
    public Result deleteCommentByAdmin(Long commentId) {
        // 1. 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            throw new CommentException("用户未登录");
        }

        // TODO: 验证用户是否为管理员
        // 这里需要根据项目的权限管理机制来验证
        // 暂时使用一个假的验证方法，实际项目中需要替换为真实的权限验证逻辑
        boolean isAdmin = isAdmin(user.getId());
        if (!isAdmin) {
            throw new CommentException("无管理员权限");
        }

        // 2. 参数校验
        if (commentId == null) {
            throw new CommentException("评论ID不能为空");
        }

        // 3. 查询评论
        ShopComment comment = getById(commentId);
        if (comment == null) {
            throw new CommentException("评论不存在");
        }

        // 4. 软删除评论（更新状态）
        comment.setStatus(SystemConstants.COMMENT_STATUS_HIDDEN_BY_ADMIN);
        comment.setUpdateTime(LocalDateTime.now());
        updateById(comment);

        // 5. 更新商店平均评分
        calculateShopAverageRating(comment.getShopId());
        
        // 6. 删除相关缓存
        cleanCommentCache(comment.getShopId());

        return Result.success();
    }

    @Override
    @Transactional
    public Result calculateShopAverageRating(Long shopId) {
        // 1. 参数校验
        if (shopId == null) {
            throw new CommentException("商店ID不能为空");
        }

        // 2. 验证商店是否存在
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new CommentException("商店不存在");
        }
        
        // 3. 尝试从缓存中获取平均评分
        String cacheKey = RedisConstants.CACHE_SHOP_SCORE_KEY + shopId;
        String cachedScore = stringRedisTemplate.opsForValue().get(cacheKey);
        
        double averageRating = 0.0;
        
        if (StrUtil.isNotBlank(cachedScore) && !"0".equals(cachedScore)) {
            // 缓存命中，直接返回
            averageRating = Double.parseDouble(cachedScore);
            return Result.success(averageRating);
        }
        
        // 4. 缓存未命中，查询商店的所有有效评论的评分
        LambdaQueryWrapper<ShopComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopComment::getShopId, shopId)
                .eq(ShopComment::getStatus, SystemConstants.COMMENT_STATUS_NORMAL)
                .select(ShopComment::getRating);
        List<ShopComment> comments = list(queryWrapper);

        // 5. 计算平均评分
        if (!comments.isEmpty()) {
            double sum = comments.stream().mapToInt(ShopComment::getRating).sum();
            averageRating = sum / comments.size();
        }

        // 6. 更新商店评分（使用事件发布而不是直接调用）
        int score = (int)(averageRating * 10);  // 评分是1-5分，乘10保存，避免小数
        // 替换直接调用为事件发布
        // shopService.updateShopScore(shopId, score);
        eventPublisher.publishEvent(new ShopScoreUpdateEvent(this, shopId, score));
        
        // 7. 更新缓存
        stringRedisTemplate.opsForValue().set(
            cacheKey, 
            String.valueOf(averageRating), 
            RedisConstants.CACHE_SHOP_SCORE_TTL, 
            TimeUnit.MINUTES
        );

        return Result.success(averageRating);
    }
    
    /**
     * 清除商店评论相关的所有缓存
     */
    private void cleanCommentCache(Long shopId) {
        // 1. 删除评论列表缓存
        Set<String> commentKeys = stringRedisTemplate.keys(RedisConstants.CACHE_SHOP_COMMENTS_KEY + shopId + ":*");
        if (commentKeys != null && !commentKeys.isEmpty()) {
            stringRedisTemplate.delete(commentKeys);
        }
        
        // 2. 删除热门评论缓存
        Set<String> hotCommentKeys = stringRedisTemplate.keys(RedisConstants.CACHE_SHOP_HOT_COMMENTS_KEY + shopId + ":*");
        if (hotCommentKeys != null && !hotCommentKeys.isEmpty()) {
            stringRedisTemplate.delete(hotCommentKeys);
        }
        
        // 3. 删除评分缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_SCORE_KEY + shopId);
    }

    /**
     * 将ShopComment分页结果转换为ShopCommentDTO分页结果
     */
    private Result convertPageToDTO(Page<ShopComment> page) {
        // 1. 获取当前登录用户ID（如果已登录）
        Long currentUserId = null;
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser != null) {
            currentUserId = currentUser.getId();
        }

        // 2. 获取评论用户ID列表
        Set<Long> userIds = page.getRecords().stream()
                .map(ShopComment::getUserId)
                .collect(Collectors.toSet());

        // 3. 批量查询用户信息
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        }

        // 4. 转换为DTO
        List<ShopCommentDTO> commentDTOs = new ArrayList<>();
        for (ShopComment comment : page.getRecords()) {
            ShopCommentDTO dto = BeanUtil.copyProperties(comment, ShopCommentDTO.class);
            
            // 设置用户信息
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                dto.setUserNickname(user.getNickName());
                dto.setUserIcon(user.getIcon());
            }
            
            // 设置是否是当前用户的评论
            dto.setIsCurrentUserComment(currentUserId != null && currentUserId.equals(comment.getUserId()));
            
            commentDTOs.add(dto);
        }

        // 5. 构建分页结果
        Page<ShopCommentDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(commentDTOs);

        // 6. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", dtoPage.getRecords());
        result.put("total", dtoPage.getTotal());
        result.put("pages", dtoPage.getPages());
        result.put("current", dtoPage.getCurrent());
        result.put("size", dtoPage.getSize());

        return Result.success(result);
    }

    /**
     * 计算评论热度得分
     * 热度得分 = (评分 * 0.6) + (时间衰减因子 * 0.4)
     * 评分部分：原始评分（1-5分）/ 5 * 100
     * 时间衰减因子：100 * exp(-0.05 * 天数)，其中"天数"是评论发布至今的天数
     */
    private double calculateHotnessScore(ShopComment comment) {
        // 评分部分：原始评分（1-5分）/ 5 * 100
        double ratingScore = (comment.getRating() / 5.0) * 100;
        
        // 时间衰减因子：100 * exp(-0.05 * 天数)
        long daysSinceCreation = ChronoUnit.DAYS.between(comment.getCreateTime(), LocalDateTime.now());
        double timeDecayFactor = 100 * Math.exp(-0.05 * daysSinceCreation);
        
        // 热度得分 = (评分 * 0.6) + (时间衰减因子 * 0.4)
        return (ratingScore * 0.6) + (timeDecayFactor * 0.4);
    }

    /**
     * 验证用户是否完成了指定商店的订单
     * 注意：这是一个临时方法，实际项目中需要替换为真实的订单验证逻辑
     */
    private boolean verifyUserCompletedOrderForShop(Long userId, Long shopId, Long orderId) {
        // TODO: 调用订单服务验证订单
        // 这里应该调用订单服务验证：
        // 1. 订单存在
        // 2. 订单属于当前用户
        // 3. 订单关联的是指定商店
        // 4. 订单状态为已完成
        // 5. 订单尚未被评论过
        
        // 暂时返回true，模拟验证通过
        return true;
    }

    /**
     * 验证用户是否为管理员
     * 注意：这是一个临时方法，实际项目中需要替换为真实的权限验证逻辑
     */
    private boolean isAdmin(Long userId) {
        // TODO: 实现真实的管理员验证逻辑
        // 这里应该调用权限服务或查询用户角色表验证用户是否为管理员
        
        // 暂时返回true，模拟验证通过
        return true;
    }
} 