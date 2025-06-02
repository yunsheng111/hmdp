package com.hmdp;

import com.hmdp.common.Result;
import com.hmdp.dto.ShopCommentDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopComment;
import com.hmdp.entity.User;
import com.hmdp.mapper.ShopCommentMapper;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IShopCommentService;
import com.hmdp.service.impl.ShopCommentServiceImpl;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShopCommentService 测试类
 * @author yate
 * @date 2023-06-23
 */
@SpringBootTest
@Slf4j
public class ShopCommentServiceTest {

    @Resource
    private IShopCommentService shopCommentService;

    @Resource
    private ShopCommentMapper shopCommentMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 测试数据
    private final Long TEST_SHOP_ID = 1L;
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_ORDER_ID = 10001L;
    private Long testCommentId;

    /**
     * 每个测试方法执行前的准备工作
     */
    @BeforeEach
    void setUp() {
        // 模拟用户登录
        User user = userMapper.selectById(TEST_USER_ID);
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setNickName(user.getNickName());
            userDTO.setIcon(user.getIcon());
            UserHolder.saveUser(userDTO);
        } else {
            log.error("测试用户不存在，ID: {}", TEST_USER_ID);
        }

        // 清理可能存在的测试评论
        cleanupTestComment();
    }

    /**
     * 每个测试方法执行后的清理工作
     */
    @AfterEach
    void tearDown() {
        // 清理用户登录状态
        UserHolder.removeUser();
        
        // 清理测试评论
        cleanupTestComment();
    }

    /**
     * 测试创建评论
     */
    @Test
    void testCreateComment() {
        // 准备测试数据
        ShopCommentDTO commentDTO = new ShopCommentDTO();
        commentDTO.setShopId(TEST_SHOP_ID);
        commentDTO.setOrderId(TEST_ORDER_ID);
        commentDTO.setRating(5);
        commentDTO.setContent("这是一条测试评论，服务很好，环境很棒！");

        // 执行创建评论
        Result result = shopCommentService.createComment(commentDTO);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        
        // 保存评论ID用于后续清理
        ShopCommentDTO createdComment = (ShopCommentDTO) result.getData();
        testCommentId = createdComment.getId();
        
        // 验证评论内容
        assertEquals(commentDTO.getContent(), createdComment.getContent());
        assertEquals(commentDTO.getRating(), createdComment.getRating());
        assertEquals(TEST_USER_ID, createdComment.getUserId());
    }

    /**
     * 测试查询商店评论
     */
    @Test
    void testQueryShopComments() {
        // 先创建一条测试评论
        createTestComment();
        
        // 执行查询
        Result result = shopCommentService.queryShopComments(TEST_SHOP_ID, 1, "time", "desc");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        
        // 验证返回的数据结构
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("records"));
        assertNotNull(data.get("total"));
        assertNotNull(data.get("current"));
    }

    /**
     * 测试用户删除评论
     */
    @Test
    void testDeleteCommentByUser() {
        // 先创建一条测试评论
        createTestComment();
        
        // 执行删除操作
        Result result = shopCommentService.deleteCommentByUser(testCommentId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        
        // 验证评论状态已更改
        ShopComment comment = shopCommentMapper.selectById(testCommentId);
        assertNotNull(comment);
        assertEquals(2, comment.getStatus());  // 2表示用户删除
    }

    /**
     * 测试计算商店平均评分
     */
    @Test
    void testCalculateShopAverageRating() {
        // 先创建一条测试评论
        createTestComment();
        
        // 执行计算平均评分
        Result result = shopCommentService.calculateShopAverageRating(TEST_SHOP_ID);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        
        // 验证商店评分已更新
        Shop shop = shopMapper.selectById(TEST_SHOP_ID);
        assertNotNull(shop);
        assertTrue(shop.getScore() > 0);
    }

    /**
     * 创建测试评论
     */
    private void createTestComment() {
        ShopCommentDTO commentDTO = new ShopCommentDTO();
        commentDTO.setShopId(TEST_SHOP_ID);
        commentDTO.setOrderId(TEST_ORDER_ID);
        commentDTO.setRating(5);
        commentDTO.setContent("这是一条测试评论，服务很好，环境很棒！");
        
        Result result = shopCommentService.createComment(commentDTO);
        if (result.getSuccess() && result.getData() != null) {
            ShopCommentDTO createdComment = (ShopCommentDTO) result.getData();
            testCommentId = createdComment.getId();
        }
    }

    /**
     * 清理测试评论
     */
    private void cleanupTestComment() {
        if (testCommentId != null) {
            shopCommentMapper.deleteById(testCommentId);
            testCommentId = null;
        }
    }
} 