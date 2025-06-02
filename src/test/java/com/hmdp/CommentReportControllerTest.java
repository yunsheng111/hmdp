package com.hmdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.controller.CommentReportController;
import com.hmdp.dto.CommentReportDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.CommentReport;
import com.hmdp.entity.ShopComment;
import com.hmdp.entity.User;
import com.hmdp.mapper.CommentReportMapper;
import com.hmdp.mapper.ShopCommentMapper;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.ICommentReportService;
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
 * CommentReportController 集成测试类
 * @author yate
 * @date 2023-06-23
 */
@SpringBootTest
@Slf4j
public class CommentReportControllerTest {

    private MockMvc mockMvc;

    @Resource
    private CommentReportController commentReportController;

    @Resource
    private ICommentReportService commentReportService;

    @Resource
    private CommentReportMapper commentReportMapper;

    @Resource
    private ShopCommentMapper shopCommentMapper;

    @Resource
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 测试数据
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_ADMIN_ID = 2L; // 假设ID为2的用户是管理员
    private Long testCommentId;
    private Long testReportId;

    /**
     * 每个测试方法执行前的准备工作
     */
    @BeforeEach
    void setUp() {
        // 设置MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(commentReportController).build();
        
        // 创建测试评论
        createTestComment();
        
        // 清理可能存在的测试举报
        cleanupTestReport();
    }

    /**
     * 每个测试方法执行后的清理工作
     */
    @AfterEach
    void tearDown() {
        // 清理用户登录状态
        UserHolder.removeUser();
        
        // 清理测试举报和评论
        cleanupTestReport();
        cleanupTestComment();
    }

    /**
     * 测试举报评论
     */
    @Test
    void testReportComment() throws Exception {
        // 模拟普通用户登录
        mockUserLogin(TEST_USER_ID);
        
        // 准备测试数据
        CommentReportDTO reportDTO = new CommentReportDTO();
        reportDTO.setCommentId(testCommentId);
        reportDTO.setReason("这条评论包含不适当内容");
        
        // 执行举报请求
        MvcResult result = mockMvc.perform(post("/comment-report")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.reason", is(reportDTO.getReason())))
                .andReturn();
        
        // 从响应中提取举报ID用于后续清理
        String responseJson = result.getResponse().getContentAsString();
        CommentReportDTO createdReport = objectMapper.readTree(responseJson)
                .path("data")
                .traverse(objectMapper)
                .readValueAs(CommentReportDTO.class);
        
        testReportId = createdReport.getId();
    }

    /**
     * 测试查询待处理举报
     */
    @Test
    void testQueryPendingReports() throws Exception {
        // 先创建一条测试举报
        createTestReport();
        
        // 模拟管理员登录
        mockUserLogin(TEST_ADMIN_ID);
        
        // 执行查询请求
        mockMvc.perform(get("/comment-report/pending")
                .param("current", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.records", notNullValue()));
    }

    /**
     * 测试处理举报 - 拒绝举报
     */
    @Test
    void testResolveReportReject() throws Exception {
        // 先创建一条测试举报
        createTestReport();
        
        // 模拟管理员登录
        mockUserLogin(TEST_ADMIN_ID);
        
        // 执行处理举报请求（拒绝举报）
        mockMvc.perform(post("/comment-report/resolve/{id}", testReportId)
                .param("approve", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    /**
     * 测试处理举报 - 接受举报并删除评论
     */
    @Test
    void testResolveReportApprove() throws Exception {
        // 先创建一条测试举报
        createTestReport();
        
        // 模拟管理员登录
        mockUserLogin(TEST_ADMIN_ID);
        
        // 执行处理举报请求（接受举报）
        mockMvc.perform(post("/comment-report/resolve/{id}", testReportId)
                .param("approve", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
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
        // 创建一条测试评论用于举报
        ShopComment comment = new ShopComment();
        comment.setShopId(1L);
        comment.setUserId(3L); // 使用第三个用户创建评论，避免与测试用户冲突
        comment.setOrderId(10002L);
        comment.setRating(4);
        comment.setContent("这是一条测试评论，用于测试举报功能");
        comment.setStatus(0); // 0表示正常
        
        shopCommentMapper.insert(comment);
        testCommentId = comment.getId();
    }

    /**
     * 创建测试举报
     */
    private void createTestReport() {
        // 模拟普通用户登录
        mockUserLogin(TEST_USER_ID);
        
        // 创建举报
        CommentReportDTO reportDTO = new CommentReportDTO();
        reportDTO.setCommentId(testCommentId);
        reportDTO.setReason("这条评论包含不适当内容");
        
        try {
            String jsonContent = objectMapper.writeValueAsString(reportDTO);
            MvcResult result = mockMvc.perform(post("/comment-report")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
                    .andReturn();
            
            String responseJson = result.getResponse().getContentAsString();
            CommentReportDTO createdReport = objectMapper.readTree(responseJson)
                    .path("data")
                    .traverse(objectMapper)
                    .readValueAs(CommentReportDTO.class);
            
            testReportId = createdReport.getId();
        } catch (Exception e) {
            log.error("创建测试举报失败", e);
        }
        
        // 清除登录状态
        UserHolder.removeUser();
    }

    /**
     * 清理测试举报
     */
    private void cleanupTestReport() {
        if (testReportId != null) {
            commentReportMapper.deleteById(testReportId);
            testReportId = null;
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