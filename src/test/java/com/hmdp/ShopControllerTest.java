package com.hmdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.controller.ShopController;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ShopController 集成测试类
 * @author yate
 * @date 2023-06-23
 */
@SpringBootTest
@Slf4j
public class ShopControllerTest {

    private MockMvc mockMvc;

    @Resource
    private ShopController shopController;

    @Resource
    private IShopService shopService;

    @Resource
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 测试数据
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_SHOP_ID = 1L;

    /**
     * 每个测试方法执行前的准备工作
     */
    @BeforeEach
    void setUp() {
        // 设置MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(shopController).build();
        
        // 模拟用户登录
        mockUserLogin(TEST_USER_ID);
    }

    /**
     * 每个测试方法执行后的清理工作
     */
    @AfterEach
    void tearDown() {
        // 清理用户登录状态
        UserHolder.removeUser();
    }

    /**
     * 测试根据ID查询商铺
     */
    @Test
    void testQueryShopById() throws Exception {
        mockMvc.perform(get("/shop/{id}", TEST_SHOP_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.id", is(TEST_SHOP_ID.intValue())));
    }

    /**
     * 测试根据类型分页查询商铺
     */
    @Test
    void testQueryShopByType() throws Exception {
        int typeId = 1;
        int current = 1;
        
        mockMvc.perform(get("/shop/of/type")
                .param("typeId", String.valueOf(typeId))
                .param("current", String.valueOf(current)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.records", notNullValue()));
    }

    /**
     * 测试根据名称关键字分页查询商铺
     */
    @Test
    void testQueryShopByName() throws Exception {
        String name = "咖啡"; // 假设有包含"咖啡"的店铺
        int current = 1;
        
        mockMvc.perform(get("/shop/of/name")
                .param("name", name)
                .param("current", String.valueOf(current)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    /**
     * 测试查询商铺评分
     */
    @Test
    void testQueryShopScore() throws Exception {
        mockMvc.perform(get("/shop/score/{id}", TEST_SHOP_ID))
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
} 