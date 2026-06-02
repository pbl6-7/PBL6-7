package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import com.campus.user.mapper.UserSecurityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 忘记密码功能测试
 */
public class ForgotPasswordTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSecurityMapper userSecurityMapper;

    @InjectMocks
    private UserSecurityService userSecurityService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常重置密码
     */
    @Test
    public void testResetPasswordSuccess() {
        String username = "testuser";
        String securityAnswer = "testanswer";
        String newPassword = "newpassword123";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        com.campus.user.entity.UserSecurity userSecurity = new com.campus.user.entity.UserSecurity();
        userSecurity.setUserId(1L);
        userSecurity.setSecurityQuestionId(1);
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest("testanswer".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String hashedAnswer = java.util.Base64.getEncoder().encodeToString(hash);
            userSecurity.setSecurityAnswer(hashedAnswer);
        } catch (Exception e) {
            fail("Failed to hash answer");
        }

        when(userMapper.selectByUsername(username)).thenReturn(user);
        when(userSecurityMapper.selectByUserId(1L)).thenReturn(userSecurity);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            userSecurityService.resetPassword(username, securityAnswer, newPassword);
        });

        verify(userMapper).updateById(any(User.class));
    }

    /**
     * 测试用户不存在
     */
    @Test
    public void testResetPasswordUserNotFound() {
        String username = "nonexistent";
        String securityAnswer = "答案";
        String newPassword = "newpassword123";

        when(userMapper.selectByUsername(username)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userSecurityService.resetPassword(username, securityAnswer, newPassword);
        });

        assertEquals(4001, exception.getCode());
    }

    /**
     * 测试密保答案错误
     */
    @Test
    public void testResetPasswordWrongAnswer() {
        String username = "testuser";
        String securityAnswer = "wrong_answer";
        String newPassword = "newpassword123";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        com.campus.user.entity.UserSecurity userSecurity = new com.campus.user.entity.UserSecurity();
        userSecurity.setUserId(1L);
        userSecurity.setSecurityQuestionId(1);
        userSecurity.setSecurityAnswer("hashed_answer");

        when(userMapper.selectByUsername(username)).thenReturn(user);
        when(userSecurityMapper.selectByUserId(1L)).thenReturn(userSecurity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userSecurityService.resetPassword(username, securityAnswer, newPassword);
        });

        assertEquals(4007, exception.getCode());
    }

    /**
     * 测试密保问题未设置
     */
    @Test
    public void testResetPasswordSecurityNotSet() {
        String username = "testuser";
        String securityAnswer = "答案";
        String newPassword = "newpassword123";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        when(userMapper.selectByUsername(username)).thenReturn(user);
        when(userSecurityMapper.selectByUserId(1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userSecurityService.resetPassword(username, securityAnswer, newPassword);
        });

        assertEquals(4005, exception.getCode());
    }
}
