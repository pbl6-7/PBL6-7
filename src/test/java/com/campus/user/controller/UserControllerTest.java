package com.campus.user.controller;

import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setRole("user");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("123456");
    }

    @Test
    void testLogin_Success() {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token-123");
        loginResponse.setUserId(1L);
        loginResponse.setUsername("testuser");
        loginResponse.setRole("user");

        when(userService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<Result<LoginResponse>> response = userController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-123", response.getBody().getData().getToken());
        assertEquals(1L, response.getBody().getData().getUserId());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userService.login(any()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        ResponseEntity<Result<LoginResponse>> response = userController.login(loginRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testLogin_WrongPassword() {
        when(userService.login(any()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.UNAUTHORIZED, "密码错误"));

        ResponseEntity<Result<LoginResponse>> response = userController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetUserProfile_Success() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        ResponseEntity<Result<User>> response = userController.getUserProfile(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getData().getUsername());
    }

    @Test
    void testGetUserProfile_NotFound() {
        when(userService.getUserById(999L)).thenReturn(null);

        User notFoundUser = new User();
        notFoundUser.setId(999L);
        ResponseEntity<Result<User>> response = userController.getUserProfile(notFoundUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setRealName("新名字");
        request.setEmail("new@example.com");

        when(userService.updateProfile(anyLong(), any(UpdateProfileRequest.class)))
                .thenReturn(testUser);

        ResponseEntity<Result<User>> response = userController.updateProfile(request, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).updateProfile(eq(1L), any());
    }

    @Test
    void testUpdateProfile_NotFound() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setRealName("新名字");

        when(userService.updateProfile(anyLong(), any()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "用户不存在"));

        ResponseEntity<Result<User>> response = userController.updateProfile(request, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testChangePassword_Success() {
        when(userService.changePassword(anyLong(), anyString(), anyString()))
                .thenReturn(true);

        ResponseEntity<Result<Void>> response = userController.changePassword(
                1L, "oldPassword", "newPassword");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        when(userService.changePassword(anyLong(), anyString(), anyString()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.UNAUTHORIZED, "原密码错误"));

        ResponseEntity<Result<Void>> response = userController.changePassword(
                1L, "wrongOldPassword", "newPassword");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testLogout_Success() {
        ResponseEntity<Result<Void>> response = userController.logout(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRegister_Success() {
        when(userService.register(any(User.class))).thenReturn(testUser);

        ResponseEntity<Result<User>> response = userController.register(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testRegister_UsernameExists() {
        when(userService.register(any()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.BAD_REQUEST, "用户名已存在"));

        ResponseEntity<Result<User>> response = userController.register(testUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
