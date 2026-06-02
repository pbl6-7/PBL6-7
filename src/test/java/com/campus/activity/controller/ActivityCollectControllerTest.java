package com.campus.activity.controller;

import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityCollect;
import com.campus.activity.service.ActivityCollectService;
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
class ActivityCollectControllerTest {

    @Mock
    private ActivityCollectService activityCollectService;

    @InjectMocks
    private ActivityCollectController activityCollectController;

    private User testUser;
    private Activity testActivity;
    private ActivityCollect testCollect;

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

        testCollect = new ActivityCollect();
        testCollect.setId(1L);
        testCollect.setUserId(1L);
        testCollect.setActivityId(1L);
        testCollect.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testCollectActivity_Success() {
        doNothing().when(activityCollectService).collectActivity(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                activityCollectController.collectActivity(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityCollectService, times(1)).collectActivity(1L, 1L);
    }

    @Test
    void testCollectActivity_AlreadyCollected() {
        doThrow(new com.campus.core.common.BusinessException(ResultCode.BAD_REQUEST, "已收藏"))
                .when(activityCollectService).collectActivity(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                activityCollectController.collectActivity(1L, testUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testCollectActivity_ActivityNotFound() {
        doThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "活动不存在"))
                .when(activityCollectService).collectActivity(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                activityCollectController.collectActivity(999L, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCancelCollect_Success() {
        doNothing().when(activityCollectService).cancelCollect(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                activityCollectController.cancelCollect(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityCollectService, times(1)).cancelCollect(1L, 1L);
    }

    @Test
    void testCancelCollect_NotFound() {
        doThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "收藏记录不存在"))
                .when(activityCollectService).cancelCollect(anyLong(), anyLong());

        ResponseEntity<Result<Void>> response =
                activityCollectController.cancelCollect(999L, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetCollectStatus_Collected() {
        when(activityCollectService.isCollected(anyLong(), anyLong())).thenReturn(true);

        ResponseEntity<Result<Boolean>> response =
                activityCollectController.getCollectStatus(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData());
    }

    @Test
    void testGetCollectStatus_NotCollected() {
        when(activityCollectService.isCollected(anyLong(), anyLong())).thenReturn(false);

        ResponseEntity<Result<Boolean>> response =
                activityCollectController.getCollectStatus(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getData());
    }

    @Test
    void testGetMyCollects_Success() {
        List<ActivityResponse> collects = Arrays.asList(new ActivityResponse(testActivity));
        when(activityCollectService.getMyCollects(anyLong(), anyInt(), anyInt()))
                .thenReturn(collects);

        ResponseEntity<Result<List<ActivityResponse>>> response =
                activityCollectController.getMyCollects(1, 10, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void testGetMyCollects_Empty() {
        when(activityCollectService.getMyCollects(anyLong(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        ResponseEntity<Result<List<ActivityResponse>>> response =
                activityCollectController.getMyCollects(1, 10, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testGetMyCollects_Pagination() {
        when(activityCollectService.getMyCollects(anyLong(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        ResponseEntity<Result<List<ActivityResponse>>> response =
                activityCollectController.getMyCollects(2, 10, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(activityCollectService).getMyCollects(1L, 2, 10);
    }
}
