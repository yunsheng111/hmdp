package com.hmdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.controller.ShopCommentController;
import com.hmdp.dto.ShopCommentDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.ShopCommentMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IShopCommentService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShopCommentController 集成测试类
 * @author yate
 * @date 2023-06-23
 */
@SpringBootTest
@Slf4j
public class ShopCommentControllerTest {

    private MockMvc mockMvc;

    @Resource
    private ShopCommentController shopCommentController;

    @Resource
    private IShopCommentService shopCommentService;

    @Resource
    private ShopCommentMapper shopCommentMapper;

    @Resource
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 测试数据
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_SHOP_ID = 1L;
    private final Long TEST_ORDER_ID = 10001L;
    private Long testCommentId;

    /**
     * 每个测试方法执行前的准备工作
     */
    @BeforeEach
    void setUp() {
        // 设置MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(shopCommentController).build();
        
        // 模拟用户登录
        mockUserLogin(TEST_USER_ID);
        
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
    void testCreateComment() throws Exception {
        // 准备测试数据
        ShopCommentDTO commentDTO = new ShopCommentDTO();
        commentDTO.setShopId(TEST_SHOP_ID);
        commentDTO.setOrderId(TEST_ORDER_ID);
        commentDTO.setRating(5);
        commentDTO.setContent("这是一条测试评论，服务很好，环境很棒！");
        
        // 执行创建评论请求
        MvcResult result = mockMvc.perform(post("/shop-comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.content", is(commentDTO.getContent())))
                .andExpect(jsonPath("$.data.rating", is(commentDTO.getRating())))
                .andReturn();
        
        // 从响应中提取评论ID用于后续清理
        String responseJson = result.getResponse().getContentAsString();
        ShopCommentDTO createdComment = objectMapper.readTree(responseJson)
                .path("data")
                .traverse(objectMapper)
                .readValueAs(ShopCommentDTO.class);
        
        testCommentId = createdComment.getId();
    }

    /**
     * 测试查询商店评论
     */
    @Test
    void testQueryShopComments() throws Exception {
        // 先创建一条测试评论
        createTestComment();
        
        // 执行查询请求
        mockMvc.perform(get("/shop-comment/{shopId}", TEST_SHOP_ID)
                .param("current", "1")
                .param("sortBy", "time")
                .param("order", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.records", notNullValue()));
    }

    /**
     * 测试用户删除评论
     */
    @Test
    void testDeleteComment() throws Exception {
        // 先创建一条测试评论
        createTestComment();
        
        // 执行删除请求
        mockMvc.perform(delete("/shop-comment/{id}", testCommentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    /**
     * 测试计算商店平均评分
     */
    @Test
    void testCalculateShopAverageRating() throws Exception {
        // 先创建一条测试评论
        createTestComment();
        
        // 执行计算平均评分请求
        mockMvc.perform(get("/shop-comment/score/{shopId}", TEST_SHOP_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    /**
     * 模拟用户登录
     */
    private void mockUserLogin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setNickName(user.getNickName());
            userDTO.setIcon(user.getIcon());
            UserHolder.saveUser(userDTO);
        } else {
            log.error("测试用户不存在，ID: {}", userId);
        }
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
        
        try {
            String jsonContent = objectMapper.writeValueAsString(commentDTO);
            MvcResult result = mockMvc.perform(post("/shop-comment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
                    .andReturn();
            
            String responseJson = result.getResponse().getContentAsString();
            ShopCommentDTO createdComment = objectMapper.readTree(responseJson)
                    .path("data")
                    .traverse(objectMapper)
                    .readValueAs(ShopCommentDTO.class);
            
            testCommentId = createdComment.getId();
        } catch (Exception e) {
            log.error("创建测试评论失败", e);
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