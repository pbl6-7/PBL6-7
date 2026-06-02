package com.campus.activity.service;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.core.common.BusinessException;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 活动管理功能测试
 */
public class ActivityServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ActivityRegistrationMapper registrationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ActivityService activityService;

    private User testUser;
    private Activity testActivity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");

        testActivity = new Activity();
        testActivity.setId(1L);
        testActivity.setTitle("测试活动");
        testActivity.setPublisherId(1L);
        testActivity.setStartTime(LocalDateTime.now().plusDays(1));
        testActivity.setEndTime(LocalDateTime.now().plusDays(2));
        testActivity.setLocation("测试地点");
        testActivity.setDescription("测试描述");
        testActivity.setMaxParticipants(100);
        testActivity.setStatus("published");
        testActivity.setApprovalStatus("pending");
    }

    /**
     * 测试正常发布活动
     */
    @Test
    public void testPublishActivitySuccess() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("新活动");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));
        request.setLocation("活动地点");
        request.setDescription("活动描述");
        request.setMaxParticipants(50);

        when(userMapper.selectById(1L)).thenReturn(testUser);
        doAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(1L);
            return null;
        }).when(activityMapper).insert(any(Activity.class));

        ActivityResponse response = activityService.publishActivity(1L, request);

        assertNotNull(response);
        assertEquals("新活动", response.getTitle());
        assertEquals("测试用户", response.getPublisherName());
        verify(activityMapper).insert(any(Activity.class));
    }

    /**
     * 测试发布活动-发布者不存在
     */
    @Test
    public void testPublishActivityUserNotFound() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("新活动");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));

        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.publishActivity(999L, request);
        });

        assertEquals(4001, exception.getCode());
    }

    /**
     * 测试发布活动-开始时间早于当前时间
     */
    @Test
    public void testPublishActivityInvalidStartTime() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("新活动");
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        when(userMapper.selectById(1L)).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.publishActivity(1L, request);
        });

        assertTrue(exception.getMessage().contains("活动开始时间不能早于当前时间"));
    }

    /**
     * 测试发布活动-结束时间早于开始时间
     */
    @Test
    public void testPublishActivityEndTimeBeforeStartTime() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("新活动");
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        when(userMapper.selectById(1L)).thenReturn(testUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.publishActivity(1L, request);
        });

        assertTrue(exception.getMessage().contains("活动结束时间不能早于开始时间"));
    }

    /**
     * 测试获取活动详情-活动存在
     */
    @Test
    public void testGetActivityByIdSuccess() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(registrationMapper.countByActivityIdAndStatus(1L, "APPROVED")).thenReturn(0L);

        ActivityResponse response = activityService.getActivityById(1L);

        assertNotNull(response);
        assertEquals("测试活动", response.getTitle());
    }

    /**
     * 测试获取活动详情-活动不存在
     */
    @Test
    public void testGetActivityByIdNotFound() {
        when(activityMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.getActivityById(999L);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试获取我发布的活动列表
     */
    @Test
    public void testGetActivitiesByPublisher() {
        List<Activity> activities = Arrays.asList(testActivity);
        when(activityMapper.selectByPublisherId(1L)).thenReturn(activities);
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));

        List<ActivityResponse> responses = activityService.getActivitiesByPublisher(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("测试活动", responses.get(0).getTitle());
    }

    /**
     * 测试编辑活动-正常编辑
     */
    @Test
    public void testUpdateActivitySuccess() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("更新后的标题");
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(3));

        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(activityMapper.updateById(any(Activity.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        ActivityResponse response = activityService.updateActivity(1L, 1L, request);

        assertNotNull(response);
        verify(activityMapper).updateById(any(Activity.class));
    }

    /**
     * 测试编辑活动-活动不存在
     */
    @Test
    public void testUpdateActivityNotFound() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("更新后的标题");

        when(activityMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.updateActivity(999L, 1L, request);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试编辑活动-无权限
     */
    @Test
    public void testUpdateActivityNoPermission() {
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("更新后的标题");

        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.updateActivity(1L, 999L, request);
        });

        assertEquals(403, exception.getCode());
    }

    /**
     * 测试编辑活动-已审核通过不允许修改
     */
    @Test
    public void testUpdateActivityAlreadyApproved() {
        testActivity.setApprovalStatus("approved");
        ActivityPublishRequest request = new ActivityPublishRequest();
        request.setTitle("更新后的标题");

        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.updateActivity(1L, 1L, request);
        });

        assertTrue(exception.getMessage().contains("已审核通过的活动不允许修改"));
    }

    /**
     * 测试删除活动-正常删除
     */
    @Test
    public void testDeleteActivitySuccess() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(activityMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            activityService.deleteActivity(1L, 1L);
        });

        verify(activityMapper).deleteById(1L);
    }

    /**
     * 测试删除活动-活动不存在
     */
    @Test
    public void testDeleteActivityNotFound() {
        when(activityMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.deleteActivity(999L, 1L);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试删除活动-无权限
     */
    @Test
    public void testDeleteActivityNoPermission() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.deleteActivity(1L, 999L);
        });

        assertEquals(403, exception.getCode());
    }

    /**
     * 测试删除活动-已结束的活动不允许删除
     */
    @Test
    public void testDeleteActivityEnded() {
        testActivity.setStatus("ended");
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.deleteActivity(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("已结束的活动不允许删除"));
    }

    /**
     * 测试获取活动列表-正常分页
     */
    @Test
    public void testGetActivityListSuccess() {
        ActivityQueryRequest request = new ActivityQueryRequest();
        request.setPage(1);
        request.setSize(10);

        List<Activity> activities = Arrays.asList(testActivity);
        when(activityMapper.selectList(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(activities);
        when(activityMapper.count(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));

        ActivityPageResponse response = activityService.getActivityList(1L, request);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getRecords().size());
    }

    /**
     * 测试获取活动列表-空列表
     */
    @Test
    public void testGetActivityListEmpty() {
        ActivityQueryRequest request = new ActivityQueryRequest();
        request.setPage(1);
        request.setSize(10);

        when(activityMapper.selectList(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(Collections.emptyList());
        when(activityMapper.count(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);

        ActivityPageResponse response = activityService.getActivityList(1L, request);

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getRecords().isEmpty());
    }
}
