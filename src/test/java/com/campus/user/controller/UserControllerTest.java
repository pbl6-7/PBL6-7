package com.campus.user.controller;

import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.ChangePasswordRequest;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.dto.UpdateProfileRequest;
import com.campus.user.entity.User;
import com.campus.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private LoginRequest loginRequest;
    private static final String VALID_TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setContact("13800138000");
        testUser.setRole("user");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");

        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("valid-token")).thenReturn(1L);
    }

    @Test
    void testLogin_Success() {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token-123");
        loginResponse.setUserId(1L);
        loginResponse.setUsername("testuser");
        loginResponse.setRole("user");

        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        Result<LoginResponse> result = userController.login(loginRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("jwt-token-123", result.getData().getToken());
        assertEquals(1L, result.getData().getUserId());
    }

    @Test
    void testGetUserById_Success() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        Result<User> result = userController.getUserById(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("testuser", result.getData().getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userService.getUserById(999L)).thenReturn(null);

        Result<User> result = userController.getUserById(999L);

        assertEquals(ResultCode.USER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    void testGetCurrentUserProfile_Success() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        Result<User> result = userController.getCurrentUserProfile(VALID_TOKEN);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("testuser", result.getData().getUsername());
    }

    @Test
    void testGetCurrentUserProfile_Unauthorized() {
        Result<User> result = userController.getCurrentUserProfile(null);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testUpdateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setRealName("新名字");
        request.setContact("13900139000");

        doNothing().when(userService).updateProfile(anyLong(), anyString(), anyString());

        Result<Void> result = userController.updateProfile(VALID_TOKEN, request);

        assertEquals(200, result.getCode());
        verify(userService, times(1)).updateProfile(eq(1L), eq("新名字"), eq("13900139000"));
    }

    @Test
    void testUpdateProfile_Unauthorized() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setRealName("新名字");

        Result<Void> result = userController.updateProfile(null, request);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        doNothing().when(userService).changePassword(anyLong(), anyString(), anyString());

        Result<Void> result = userController.changePassword(VALID_TOKEN, request);

        assertEquals(200, result.getCode());
    }

    @Test
    void testChangePassword_Unauthorized() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        Result<Void> result = userController.changePassword(null, request);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testRegister_Success() {
        doNothing().when(userService).register(any(User.class), anyInt(), anyString());

        Result<Void> result = userController.register(testUser);

        assertEquals(200, result.getCode());
    }
}
