package com.campus.activity.controller;

import com.campus.activity.dto.RegistrationRequest;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.dto.RegistrationStatusUpdateRequest;
import com.campus.activity.entity.ActivityRegistration;
import com.campus.activity.service.RegistrationService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private RegistrationController registrationController;

    private User testUser;
    private ActivityRegistration testRegistration;
    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("user");

        testRegistration = new ActivityRegistration();
        testRegistration.setId(1L);
        testRegistration.setActivityId(1L);
        testRegistration.setUserId(1L);
        testRegistration.setStatus("confirmed");
        testRegistration.setRegistrationTime(LocalDateTime.now());

        registrationRequest = new RegistrationRequest();
        registrationRequest.setActivityId(1L);
    }

    @Test
    void testRegister_Success() {
        when(registrationService.register(anyLong(), any(RegistrationRequest.class)))
                .thenReturn(testRegistration);

        ResponseEntity<Result<ActivityRegistration>> response =
                registrationController.register(registrationRequest, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("confirmed", response.getBody().getData().getStatus());
        verify(registrationService, times(1)).register(eq(1L), any());
    }

    @Test
    void testRegister_AlreadyRegistered() {
        when(registrationService.register(anyLong(), any()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.BAD_REQUEST, "已报名"));

        ResponseEntity<Result<ActivityRegistration>> response =
                registrationController.register(registrationRequest, testUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCancelRegistration_Success() {
        doNothing().when(registrationService).cancelRegistration(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                registrationController.cancelRegistration(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(registrationService, times(1)).cancelRegistration(1L, 1L);
    }

    @Test
    void testCancelRegistration_NotFound() {
        doThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "报名记录不存在"))
                .when(registrationService).cancelRegistration(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                registrationController.cancelRegistration(1L, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetMyRegistrations_Success() {
        RegistrationResponse pageResponse = new RegistrationResponse();
        pageResponse.setRecords(Arrays.asList(new RegistrationResponse()));
        pageResponse.setTotal(1L);
        when(registrationService.getMyRegistrations(anyLong(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        ResponseEntity<Result<RegistrationResponse>> response =
                registrationController.getMyRegistrations(1, 10, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getRecords().size());
    }

    @Test
    void testGetActivityRegistrations_Success() {
        RegistrationResponse pageResponse = new RegistrationResponse();
        pageResponse.setRecords(Arrays.asList(new RegistrationResponse()));
        pageResponse.setTotal(1L);
        when(registrationService.getActivityRegistrations(anyLong(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        ResponseEntity<Result<RegistrationResponse>> response =
                registrationController.getActivityRegistrations(1L, 1, 10, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetActivityRegistrations_AdminOnly() {
        testUser.setRole("admin");
        RegistrationResponse pageResponse = new RegistrationResponse();
        pageResponse.setRecords(Arrays.asList(new RegistrationResponse()));
        when(registrationService.getActivityRegistrations(anyLong(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        ResponseEntity<Result<RegistrationResponse>> response =
                registrationController.getActivityRegistrations(1L, 1, 10, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateRegistrationStatus_Success() {
        RegistrationStatusUpdateRequest request = new RegistrationStatusUpdateRequest();
        request.setStatus("cancelled");

        doNothing().when(registrationService).updateStatus(anyLong(), anyString());

        ResponseEntity<Result<Void>> response =
                registrationController.updateRegistrationStatus(1L, request, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateRegistrationStatus_AdminOnly() {
        testUser.setRole("admin");
        RegistrationStatusUpdateRequest request = new RegistrationStatusUpdateRequest();
        request.setStatus("rejected");

        doNothing().when(registrationService).updateStatus(anyLong(), anyString());

        ResponseEntity<Result<Void>> response =
                registrationController.updateRegistrationStatus(1L, request, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(registrationService, times(1)).updateStatus(1L, "rejected");
    }

    @Test
    void testGetRegistrationDetail_Success() {
        when(registrationService.getRegistrationDetail(anyLong()))
                .thenReturn(testRegistration);

        ResponseEntity<Result<ActivityRegistration>> response =
                registrationController.getRegistrationDetail(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetRegistrationDetail_Forbidden() {
        when(registrationService.getRegistrationDetail(anyLong()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.FORBIDDEN, "无权限查看"));

        ResponseEntity<Result<ActivityRegistration>> response =
                registrationController.getRegistrationDetail(1L, testUser);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
