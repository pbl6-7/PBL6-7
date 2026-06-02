package com.campus.activity.controller;

import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.entity.Activity;
import com.campus.activity.service.ActivityService;
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
class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ActivityController activityController;

    private User testUser;
    private Activity testActivity;
    private ActivityPublishRequest publishRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("user");

        testActivity = new Activity();
        testActivity.setId(1L);
        testActivity.setTitle("测试活动");
        testActivity.setDescription("活动描述");
        testActivity.setLocation("图书馆");
        testActivity.setStartTime(LocalDateTime.of(2026, 6, 1, 10, 0));
        testActivity.setEndTime(LocalDateTime.of(2026, 6, 1, 12, 0));
        testActivity.setStatus("published");
        testActivity.setOrganizerId(1L);

        publishRequest = new ActivityPublishRequest();
        publishRequest.setTitle("新活动");
        publishRequest.setDescription("新活动描述");
        publishRequest.setLocation("报告厅");
        publishRequest.setStartTime(LocalDateTime.of(2026, 6, 15, 14, 0));
        publishRequest.setEndTime(LocalDateTime.of(2026, 6, 15, 16, 0));
    }

    @Test
    void testPublishActivity_Success() {
        when(activityService.publishActivity(any(ActivityPublishRequest.class), anyLong()))
                .thenReturn(testActivity);

        ResponseEntity<Result<Activity>> response = activityController.publishActivity(publishRequest, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ResultCode.SUCCESS.getCode(), response.getBody().getCode());
        assertEquals("测试活动", response.getBody().getData().getTitle());
        verify(activityService, times(1)).publishActivity(any(), eq(1L));
    }

    @Test
    void testPublishActivity_WithAdminRole() {
        testUser.setRole("admin");
        when(activityService.publishActivity(any(), anyLong())).thenReturn(testActivity);

        ResponseEntity<Result<Activity>> response = activityController.publishActivity(publishRequest, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityService, times(1)).publishActivity(any(), eq(1L));
    }

    @Test
    void testGetActivityDetail_Success() {
        when(activityService.getActivityDetail(1L)).thenReturn(testActivity);

        ResponseEntity<Result<Activity>> response = activityController.getActivityDetail(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getData().getId());
    }

    @Test
    void testGetActivityDetail_NotFound() {
        when(activityService.getActivityDetail(999L)).thenReturn(null);

        ResponseEntity<Result<Activity>> response = activityController.getActivityDetail(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateActivity_Success() {
        when(activityService.updateActivity(eq(1L), any(ActivityPublishRequest.class), anyLong()))
                .thenReturn(true);

        ResponseEntity<Result<Void>> response = activityController.updateActivity(1L, publishRequest, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityService, times(1)).updateActivity(eq(1L), any(), eq(1L));
    }

    @Test
    void testUpdateActivity_NotOwner() {
        when(activityService.updateActivity(eq(1L), any(), anyLong()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.FORBIDDEN, "无权限"));

        ResponseEntity<Result<Void>> response = activityController.updateActivity(1L, publishRequest, testUser);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteActivity_Success() {
        when(activityService.deleteActivity(1L, 1L)).thenReturn(true);

        ResponseEntity<Result<Void>> response = activityController.deleteActivity(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetActivityList_Success() {
        List<Activity> activities = Arrays.asList(testActivity);
        when(activityService.getActivityList(any(ActivityQueryRequest.class)))
                .thenReturn(activities);

        ActivityQueryRequest request = new ActivityQueryRequest();
        ResponseEntity<Result<List<Activity>>> response = activityController.getActivityList(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetActivityList_PublishedOnly() {
        Activity publishedActivity = new Activity();
        publishedActivity.setId(2L);
        publishedActivity.setStatus("published");
        when(activityService.getActivityList(any())).thenReturn(Arrays.asList(publishedActivity));

        ActivityQueryRequest request = new ActivityQueryRequest();
        ResponseEntity<Result<List<Activity>>> response = activityController.getActivityList(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateActivityStatus_Success() {
        when(activityService.updateActivityStatus(1L, "closed")).thenReturn(true);

        ResponseEntity<Result<Void>> response = activityController.updateActivityStatus(1L, "closed", testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
