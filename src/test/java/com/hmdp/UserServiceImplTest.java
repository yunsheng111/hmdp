package com.hmdp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.entity.User;
import com.hmdp.service.impl.UserServiceImpl;
import com.hmdp.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
public class UserServiceImplTest {

    private UserServiceImpl userService;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        session = Mockito.mock(HttpSession.class);
    }

    @Test
    void sendCode_InvalidPhone_ReturnsFailResult() {
        String invalidPhone = "123456";
        Result result = userService.sendCode(invalidPhone, session);
        assertEquals(Result.fail("手机号码格式不正确").getMessage(), result.getMessage());

    }

    @Test
    void sendCode_ValidPhone_SendsCodeSuccessfully() {
        String validPhone = "13812345678";
        Result result = userService.sendCode(validPhone, session);
        assertEquals(Result.success("验证码发送成功").getMessage(), result.getMessage());

        verify(session, times(1)).setAttribute(eq("code"), anyString());
    }

    @Test
    void login_InvalidPhone_ReturnsFailResult() {
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setPhone("123456");
        Result result = userService.login(loginForm, session);
        assertEquals(Result.fail("手机号码格式不正确").getMessage(), result.getMessage());

    }

    @Test
    void login_InvalidCode_ReturnsFailResult() {
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setPhone("13812345678");
        loginForm.setCode("wrongCode");
        when(session.getAttribute("code")).thenReturn("123456");
        Result result = userService.login(loginForm, session);
        assertEquals(Result.fail("验证码错误").getMessage(), result.getMessage());
    }

    @Test
    void login_NewUser_CreatesAndLogsInUser() {
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setPhone("13812345678");
        loginForm.setCode("123456");
        loginForm.setPassword("password");
        when(session.getAttribute("code")).thenReturn("123456");
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Result result = userService.login(loginForm, session);
        assertEquals(Result.success("登录成功").getMessage(), result.getMessage());
    }

    @Test
    void login_ExistingUser_LogsInSuccessfully() {
        LoginFormDTO loginForm = new LoginFormDTO();
        loginForm.setPhone("13812345678");
        loginForm.setCode("123456");
        User existingUser = new User();
        existingUser.setPhone("13812345678");
        existingUser.setPassword("password");
        when(session.getAttribute("code")).thenReturn("123456");
        when(userService.getOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);

        Result result = userService.login(loginForm, session);
        assertEquals(Result.success("登录成功").getMessage(), result.getMessage());
    }
}
