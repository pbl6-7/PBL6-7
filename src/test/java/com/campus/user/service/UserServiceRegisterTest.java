package com.campus.user.service;

import com.campus.core.common.BusinessException;
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
 * 用户注册功能测试
 */
public class UserServiceRegisterTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSecurityService userSecurityService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常注册
     */
    @Test
    public void testRegisterSuccess() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");
        user.setRealName("新用户");

        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        doNothing().when(userSecurityService).setSecurity(anyLong(), anyInt(), anyString());
        
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));

        assertDoesNotThrow(() -> {
            userService.register(user, 1, "答案");
        });

        verify(userMapper).insert(any(User.class));
        verify(userSecurityService).setSecurity(eq(1L), eq(1), eq("答案"));
    }

    /**
     * 测试用户名已存在
     */
    @Test
    public void testRegisterUserAlreadyExists() {
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("password123");

        User existingUser = new User();
        existingUser.setUsername("existinguser");

        when(userMapper.selectByUsername("existinguser")).thenReturn(existingUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(user, 1, "答案");
        });

        assertEquals(4002, exception.getCode());
    }

    /**
     * 测试密保问题未设置
     */
    @Test
    public void testRegisterSecurityNotSet() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");

        when(userMapper.selectByUsername("newuser")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(user, null, "答案");
        });

        assertEquals(422, exception.getCode());
    }

    /**
     * 测试密保问题ID无效
     */
    @Test
    public void testRegisterInvalidSecurityQuestion() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");

        when(userMapper.selectByUsername("newuser")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(user, 10, "答案");
        });

        assertEquals(4006, exception.getCode());
    }

    /**
     * 测试密保答案为空
     */
    @Test
    public void testRegisterEmptySecurityAnswer() {
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password123");

        when(userMapper.selectByUsername("newuser")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(user, 1, "");
        });

        assertEquals(422, exception.getCode());
    }
}
