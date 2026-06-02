package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.JwtUtils;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户登录功能测试
 */
public class UserServiceLoginTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSecurityService userSecurityService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常登录
     */
    @Test
    public void testLoginSuccess() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk="); // admin123 的 SHA-256 加密
        user.setRealName("管理员");
        user.setRole("admin");

        // 模拟行为
        when(userMapper.selectByUsername("admin")).thenReturn(user);
        when(jwtUtils.generateToken(anyLong(), anyString(), anyString())).thenReturn("mock-token");

        // 执行测试
        LoginResponse response = userService.login(request);

        // 验证结果
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUsername());
        assertEquals("管理员", response.getRealName());
        assertEquals("admin", response.getRole());
        assertNotNull(response.getToken());
    }

    /**
     * 测试用户不存在
     */
    @Test
    public void testLoginUserNotFound() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        // 模拟行为
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(request);
        });

        assertEquals(4001, exception.getCode());
    }

    /**
     * 测试密码错误
     */
    @Test
    public void testLoginPasswordError() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("YTFkZDI5MTg0NjRlM2EyMDc0ZWE5M2YxM2E3YTg0MDM4NTQ5NWE5Ng=="); // admin123 的 SHA-256 加密

        // 模拟行为
        when(userMapper.selectByUsername("admin")).thenReturn(user);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(request);
        });

        assertEquals(4003, exception.getCode());
    }
}
