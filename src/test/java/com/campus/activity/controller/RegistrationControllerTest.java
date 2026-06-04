package com.campus.activity.controller;

import com.campus.activity.dto.RegistrationPageResponse;
import com.campus.activity.dto.RegistrationRequest;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.dto.RegistrationStatusUpdateRequest;
import com.campus.activity.entity.ActivityRegistration;
import com.campus.activity.service.RegistrationService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationControllerTest {

    @Mock
    private RegistrationService registrationService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private RegistrationController registrationController;

    private ActivityRegistration testRegistration;
    private RegistrationRequest registrationRequest;
    private RegistrationResponse registrationResponse;
    private static final String VALID_TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        testRegistration = new ActivityRegistration();
        testRegistration.setId(1L);
        testRegistration.setActivityId(1L);
        testRegistration.setUserId(1L);
        testRegistration.setStatus("confirmed");
        testRegistration.setRegistrationTime(LocalDateTime.now());

        registrationRequest = new RegistrationRequest();
        registrationRequest.setActivityId(1L);

        registrationResponse = new RegistrationResponse();
        registrationResponse.setId(1L);
        registrationResponse.setActivityId(1L);
        registrationResponse.setUserId(1L);
        registrationResponse.setStatus("confirmed");

        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("valid-token")).thenReturn(1L);
    }

    @Test
    void testRegisterForActivity_Success() {
        when(registrationService.registerForActivity(anyLong(), anyLong()))
                .thenReturn(registrationResponse);

        Result<RegistrationResponse> result = registrationController.registerForActivity(VALID_TOKEN, registrationRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("confirmed", result.getData().getStatus());
        verify(registrationService, times(1)).registerForActivity(1L, 1L);
    }

    @Test
    void testRegisterForActivity_Unauthorized() {
        Result<RegistrationResponse> result = registrationController.registerForActivity(null, registrationRequest);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testGetMyRegistrations_Success() {
        RegistrationPageResponse pageResponse = new RegistrationPageResponse();
        pageResponse.setRecords(Arrays.asList(registrationResponse));
        pageResponse.setTotal(1L);

        when(registrationService.getMyRegistrations(anyLong(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        Result<RegistrationPageResponse> result = registrationController.getMyRegistrations(VALID_TOKEN, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
    }

    @Test
    void testGetMyRegistrations_Unauthorized() {
        Result<RegistrationPageResponse> result = registrationController.getMyRegistrations(null, 1, 10);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testGetActivityRegistrations_Success() {
        RegistrationPageResponse pageResponse = new RegistrationPageResponse();
        pageResponse.setRecords(Arrays.asList(registrationResponse));
        pageResponse.setTotal(1L);

        when(registrationService.getActivityRegistrations(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        Result<RegistrationPageResponse> result = registrationController.getActivityRegistrations(VALID_TOKEN, 1L, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
    }

    @Test
    void testGetActivityRegistrations_Unauthorized() {
        Result<RegistrationPageResponse> result = registrationController.getActivityRegistrations(null, 1L, 1, 10);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testUpdateRegistrationStatus_Success() {
        RegistrationStatusUpdateRequest request = new RegistrationStatusUpdateRequest();
        request.setRegistrationId(1L);
        request.setStatus("confirmed");

        when(registrationService.updateRegistrationStatus(anyLong(), anyLong(), anyString()))
                .thenReturn(registrationResponse);

        Result<RegistrationResponse> result = registrationController.updateRegistrationStatus(VALID_TOKEN, request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void testUpdateRegistrationStatus_Unauthorized() {
        RegistrationStatusUpdateRequest request = new RegistrationStatusUpdateRequest();
        request.setRegistrationId(1L);
        request.setStatus("confirmed");

        Result<RegistrationResponse> result = registrationController.updateRegistrationStatus(null, request);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testCancelRegistration_Success() {
        doNothing().when(registrationService).cancelRegistration(anyLong(), anyLong());

        Result<Void> result = registrationController.cancelRegistration(VALID_TOKEN, 1L);

        assertEquals(200, result.getCode());
        verify(registrationService, times(1)).cancelRegistration(1L, 1L);
    }

    @Test
    void testCancelRegistration_Unauthorized() {
        Result<Void> result = registrationController.cancelRegistration(null, 1L);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }
}
