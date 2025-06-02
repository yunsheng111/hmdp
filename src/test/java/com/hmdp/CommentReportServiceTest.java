package com.hmdp;

import com.hmdp.common.Result;
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

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommentReportService 测试类
 * @author yate
 * @date 2023-06-23
 */
@SpringBootTest
@Slf4j
public class CommentReportServiceTest {

    @Resource
    private ICommentReportService commentReportService;

    @Resource
    private CommentReportMapper commentReportMapper;

    @Resource
    private ShopCommentMapper shopCommentMapper;

    @Resource
    private UserMapper userMapper;

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
    void testReportComment() {
        // 模拟普通用户登录
        mockUserLogin(TEST_USER_ID);
        
        // 准备测试数据
        CommentReportDTO reportDTO = new CommentReportDTO();
        reportDTO.setCommentId(testCommentId);
        reportDTO.setReason("这条评论包含不适当内容");
        
        // 执行举报
        Result result = commentReportService.createReport(reportDTO);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        
        // 保存举报ID用于后续清理
        if (result.getData() instanceof CommentReportDTO) {
            CommentReportDTO createdReport = (CommentReportDTO) result.getData();
            testReportId = createdReport.getId();
            
            // 验证举报内容
            assertEquals(reportDTO.getReason(), createdReport.getReason());
            assertEquals(testCommentId, createdReport.getCommentId());
            assertEquals(TEST_USER_ID, createdReport.getReporterId());
            assertEquals(0, createdReport.getStatus()); // 0表示待处理
        }
    }

    /**
     * 测试查询待处理举报
     */
    @Test
    void testQueryPendingReports() {
        // 先创建一条测试举报
        createTestReport();
        
        // 模拟管理员登录
        mockUserLogin(TEST_ADMIN_ID);
        
        // 执行查询
        Result result = commentReportService.queryPendingReports(1);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        
        // 验证返回的数据结构
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertNotNull(data.get("records"));
        assertNotNull(data.get("total"));
    }

    /**
     * 测试处理举报 - 拒绝举报
     */
    @Test
    void testResolveReport() {
        // 先创建一条测试举报
        createTestReport();
        
        // 模拟管理员登录
        mockUserLogin(TEST_ADMIN_ID);
        
        // 执行处理举报（拒绝举报）
        Result result = commentReportService.resolveReport(testReportId, false);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        
        // 验证举报状态已更改
        CommentReport report = commentReportMapper.selectById(testReportId);
        assertNotNull(report);
        assertEquals(1, report.getStatus()); // 1表示已处理
    }

    /**
     * 测试处理举报并删除评论
     */
    @Test
    void testResolveReportAndDeleteComment() {
        // 先创建一条测试举报
        createTestReport();
        
        // 模拟管理员登录
        mockUserLogin(TEST_ADMIN_ID);
        
        // 执行处理举报（接受举报并删除评论）
        Result result = commentReportService.resolveReport(testReportId, true);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.getSuccess());
        
        // 验证举报状态已更改
        CommentReport report = commentReportMapper.selectById(testReportId);
        assertNotNull(report);
        assertEquals(1, report.getStatus()); // 1表示已处理
        
        // 验证评论状态已更改
        ShopComment comment = shopCommentMapper.selectById(testCommentId);
        assertNotNull(comment);
        assertEquals(3, comment.getStatus()); // 3表示被管理员删除
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
        
        Result result = commentReportService.createReport(reportDTO);
        if (result.getSuccess() && result.getData() != null) {
            CommentReportDTO createdReport = (CommentReportDTO) result.getData();
            testReportId = createdReport.getId();
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