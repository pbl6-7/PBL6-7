package com.campus.activity.controller;

import com.campus.activity.entity.ActivityCollect;
import com.campus.activity.service.ActivityCollectService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityCollectControllerTest {

    @Mock
    private ActivityCollectService activityCollectService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private ActivityCollectController activityCollectController;

    private static final String VALID_TOKEN = "Bearer valid-token";
    private static final String INVALID_TOKEN = "invalid-token";

    @BeforeEach
    void setUp() {
        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.validateToken("invalid-token")).thenReturn(false);
        when(jwtUtils.getUserIdFromToken("valid-token")).thenReturn(1L);
    }

    @Test
    void testCollect_Success() {
        doNothing().when(activityCollectService).collectActivity(anyLong(), anyLong());

        Result<Map<String, Object>> result = activityCollectController.collect(1L, VALID_TOKEN);

        assertEquals(HttpStatus.OK.value(), result.getCode());
        assertTrue((Boolean) result.getData().get("collected"));
        verify(activityCollectService, times(1)).collectActivity(1L, 1L);
    }

    @Test
    void testCollect_Unauthorized() {
        Result<Map<String, Object>> result = activityCollectController.collect(1L, null);

        assertEquals(401, result.getCode());
        verify(activityCollectService, never()).collectActivity(anyLong(), anyLong());
    }

    @Test
    void testCollect_InvalidToken() {
        Result<Map<String, Object>> result = activityCollectController.collect(1L, INVALID_TOKEN);

        assertEquals(401, result.getCode());
        verify(activityCollectService, never()).collectActivity(anyLong(), anyLong());
    }

    @Test
    void testUncollect_Success() {
        doNothing().when(activityCollectService).uncollectActivity(anyLong(), anyLong());

        Result<Map<String, Object>> result = activityCollectController.uncollect(1L, VALID_TOKEN);

        assertEquals(HttpStatus.OK.value(), result.getCode());
        assertFalse((Boolean) result.getData().get("collected"));
        verify(activityCollectService, times(1)).uncollectActivity(1L, 1L);
    }

    @Test
    void testUncollect_Unauthorized() {
        Result<Map<String, Object>> result = activityCollectController.uncollect(1L, null);

        assertEquals(401, result.getCode());
        verify(activityCollectService, never()).uncollectActivity(anyLong(), anyLong());
    }

    @Test
    void testGetMyCollects_Success() {
        ActivityCollect collect1 = new ActivityCollect();
        collect1.setId(1L);
        collect1.setUserId(1L);
        collect1.setActivityId(1L);

        ActivityCollect collect2 = new ActivityCollect();
        collect2.setId(2L);
        collect2.setUserId(1L);
        collect2.setActivityId(2L);

        when(activityCollectService.getUserCollects(1L)).thenReturn(Arrays.asList(collect1, collect2));

        Result<List<ActivityCollect>> result = activityCollectController.getMyCollects(VALID_TOKEN);

        assertEquals(HttpStatus.OK.value(), result.getCode());
        assertEquals(2, result.getData().size());
        verify(activityCollectService, times(1)).getUserCollects(1L);
    }

    @Test
    void testGetMyCollects_Unauthorized() {
        Result<List<ActivityCollect>> result = activityCollectController.getMyCollects(null);

        assertEquals(401, result.getCode());
        verify(activityCollectService, never()).getUserCollects(anyLong());
    }

    @Test
    void testCheckCollectStatus_Collected() {
        when(activityCollectService.isCollected(1L, 1L)).thenReturn(true);
        when(activityCollectService.getCollectCount(1L)).thenReturn(5);

        Result<Map<String, Object>> result = activityCollectController.checkCollectStatus(1L, VALID_TOKEN);

        assertEquals(HttpStatus.OK.value(), result.getCode());
        assertTrue((Boolean) result.getData().get("collected"));
        assertEquals(5, result.getData().get("collectCount"));
    }

    @Test
    void testCheckCollectStatus_NotCollected() {
        when(activityCollectService.isCollected(1L, 1L)).thenReturn(false);
        when(activityCollectService.getCollectCount(1L)).thenReturn(0);

        Result<Map<String, Object>> result = activityCollectController.checkCollectStatus(1L, VALID_TOKEN);

        assertEquals(HttpStatus.OK.value(), result.getCode());
        assertFalse((Boolean) result.getData().get("collected"));
        assertEquals(0, result.getData().get("collectCount"));
    }

    @Test
    void testCheckCollectStatus_Unauthorized() {
        Result<Map<String, Object>> result = activityCollectController.checkCollectStatus(1L, null);

        assertEquals(401, result.getCode());
        verify(activityCollectService, never()).isCollected(anyLong(), anyLong());
    }
}
