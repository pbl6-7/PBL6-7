package com.campus.activity.controller;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.service.ActivityService;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private ActivityController activityController;

    private ActivityResponse testActivityResponse;
    private ActivityPublishRequest publishRequest;
    private ActivityQueryRequest queryRequest;
    private static final String VALID_TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        testActivityResponse = new ActivityResponse();
        testActivityResponse.setId(1L);
        testActivityResponse.setTitle("测试活动");
        testActivityResponse.setDescription("活动描述");
        testActivityResponse.setLocation("图书馆");
        testActivityResponse.setStartTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        testActivityResponse.setEndTime(LocalDateTime.of(2026, 6, 1, 12, 0));
        testActivityResponse.setStatus("published");

        publishRequest = new ActivityPublishRequest();
        publishRequest.setTitle("新活动");
        publishRequest.setDescription("新活动描述");

        queryRequest = new ActivityQueryRequest();

        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("valid-token")).thenReturn(1L);
    }

    @Test
    void testPublishActivity_Success() {
        when(activityService.publishActivity(anyLong(), any(ActivityPublishRequest.class)))
                .thenReturn(testActivityResponse);

        Result<ActivityResponse> result = activityController.publishActivity(VALID_TOKEN, publishRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("测试活动", result.getData().getTitle());
        verify(activityService, times(1)).publishActivity(eq(1L), any());
    }

    @Test
    void testPublishActivity_Unauthorized() {
        Result<ActivityResponse> result = activityController.publishActivity(null, publishRequest);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testGetActivityById_Success() {
        when(activityService.getActivityById(1L)).thenReturn(testActivityResponse);

        Result<ActivityResponse> result = activityController.getActivityById(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
    }

    @Test
    void testGetMyActivities_Success() {
        List<ActivityResponse> activities = Arrays.asList(testActivityResponse);
        when(activityService.getActivitiesByPublisher(1L)).thenReturn(activities);

        Result<List<ActivityResponse>> result = activityController.getMyActivities(VALID_TOKEN);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testGetMyActivities_Unauthorized() {
        Result<List<ActivityResponse>> result = activityController.getMyActivities(null);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testUpdateActivity_Success() {
        when(activityService.updateActivity(anyLong(), anyLong(), any(ActivityPublishRequest.class)))
                .thenReturn(testActivityResponse);

        Result<ActivityResponse> result = activityController.updateActivity(VALID_TOKEN, 1L, publishRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(activityService, times(1)).updateActivity(eq(1L), eq(1L), any());
    }

    @Test
    void testUpdateActivity_Unauthorized() {
        Result<ActivityResponse> result = activityController.updateActivity(null, 1L, publishRequest);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testDeleteActivity_Success() {
        doNothing().when(activityService).deleteActivity(anyLong(), anyLong());

        Result<Void> result = activityController.deleteActivity(VALID_TOKEN, 1L);

        assertEquals(200, result.getCode());
        verify(activityService, times(1)).deleteActivity(1L, 1L);
    }

    @Test
    void testDeleteActivity_Unauthorized() {
        Result<Void> result = activityController.deleteActivity(null, 1L);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testGetActivityList_Success() {
        ActivityPageResponse pageResponse = new ActivityPageResponse();
        pageResponse.setRecords(Arrays.asList(testActivityResponse));
        pageResponse.setTotal(1L);

        when(activityService.getActivityList(anyLong(), any(ActivityQueryRequest.class)))
                .thenReturn(pageResponse);

        Result<ActivityPageResponse> result = activityController.getActivityList(VALID_TOKEN, queryRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
    }

    @Test
    void testGetActivityList_Unauthorized() {
        Result<ActivityPageResponse> result = activityController.getActivityList(null, queryRequest);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testGetPublicActivityList_Success() {
        ActivityPageResponse pageResponse = new ActivityPageResponse();
        pageResponse.setRecords(Arrays.asList(testActivityResponse));
        pageResponse.setTotal(1L);

        when(activityService.getPublicActivityList(any(ActivityQueryRequest.class)))
                .thenReturn(pageResponse);

        Result<ActivityPageResponse> result = activityController.getPublicActivityList(queryRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getRecords().size());
    }
}
