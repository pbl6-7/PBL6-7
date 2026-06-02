package com.campus.activity.service;

import com.campus.activity.dto.RegistrationPageResponse;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityRegistration;
import com.campus.activity.mapper.ActivityRegistrationMapper;
import com.campus.activity.mapper.ActivityMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 活动报名功能测试
 */
public class RegistrationServiceTest {

    @Mock
    private ActivityRegistrationMapper registrationMapper;

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RegistrationService registrationService;

    private User testUser;
    private Activity testActivity;
    private ActivityRegistration testRegistration;

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
        testActivity.setApprovalStatus("approved");

        testRegistration = new ActivityRegistration();
        testRegistration.setId(1L);
        testRegistration.setActivityId(1L);
        testRegistration.setUserId(1L);
        testRegistration.setRegistrationTime(LocalDateTime.now());
        testRegistration.setStatus("pending");
    }

    /**
     * 测试正常报名活动
     */
    @Test
    public void testRegisterForActivitySuccess() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(registrationMapper.selectByActivityIdAndUserId(1L, 1L)).thenReturn(null);
        when(registrationMapper.countByActivityId(1L)).thenReturn(0L);
        when(registrationMapper.insert(any(ActivityRegistration.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        RegistrationResponse response = registrationService.registerForActivity(1L, 1L);

        assertNotNull(response);
        assertEquals("pending", response.getStatus());
        verify(registrationMapper).insert(any(ActivityRegistration.class));
    }

    /**
     * 测试报名活动-活动不存在
     */
    @Test
    public void testRegisterForActivityNotFound() {
        when(activityMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.registerForActivity(1L, 999L);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试报名活动-活动未通过审核
     */
    @Test
    public void testRegisterForActivityNotApproved() {
        testActivity.setApprovalStatus("pending");
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.registerForActivity(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("未通过审核"));
    }

    /**
     * 测试报名活动-活动未发布
     */
    @Test
    public void testRegisterForActivityNotPublished() {
        testActivity.setStatus("draft");
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.registerForActivity(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("未发布"));
    }

    /**
     * 测试报名活动-活动已开始
     */
    @Test
    public void testRegisterForActivityAlreadyStarted() {
        testActivity.setStartTime(LocalDateTime.now().minusDays(1));
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.registerForActivity(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("已开始或已结束"));
    }

    /**
     * 测试报名活动-重复报名
     */
    @Test
    public void testRegisterForActivityAlreadyRegistered() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(registrationMapper.selectByActivityIdAndUserId(1L, 1L)).thenReturn(testRegistration);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.registerForActivity(1L, 1L);
        });

        assertEquals(409, exception.getCode());
    }

    /**
     * 测试报名活动-人数已达上限
     */
    @Test
    public void testRegisterForActivityFull() {
        testActivity.setMaxParticipants(1);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(registrationMapper.selectByActivityIdAndUserId(1L, 1L)).thenReturn(null);
        when(registrationMapper.countByActivityId(1L)).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.registerForActivity(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("报名人数已达上限"));
    }

    /**
     * 测试获取我的报名列表
     */
    @Test
    public void testGetMyRegistrations() {
        List<ActivityRegistration> registrations = Arrays.asList(testRegistration);
        when(registrationMapper.countByUserId(1L)).thenReturn(1L);
        when(registrationMapper.selectByUserIdWithPage(1L, 0, 10)).thenReturn(registrations);
        when(activityMapper.selectByIds(anyList())).thenReturn(Arrays.asList(testActivity));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));

        RegistrationPageResponse response = registrationService.getMyRegistrations(1L, 1, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getRecords().size());
    }

    /**
     * 测试获取我的报名列表-空列表
     */
    @Test
    public void testGetMyRegistrationsEmpty() {
        when(registrationMapper.countByUserId(1L)).thenReturn(0L);
        when(registrationMapper.selectByUserIdWithPage(1L, 0, 10)).thenReturn(Collections.emptyList());

        RegistrationPageResponse response = registrationService.getMyRegistrations(1L, 1, 10);

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getRecords().isEmpty());
    }

    /**
     * 测试获取活动报名列表-无权查看
     */
    @Test
    public void testGetActivityRegistrationsNoPermission() {
        testActivity.setPublisherId(2L);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.getActivityRegistrations(1L, 1L, 1, 10);
        });

        assertEquals(403, exception.getCode());
    }

    /**
     * 测试更新报名状态
     */
    @Test
    public void testUpdateRegistrationStatus() {
        when(registrationMapper.selectById(1L)).thenReturn(testRegistration);
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(registrationMapper.updateById(any(ActivityRegistration.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        RegistrationResponse response = registrationService.updateRegistrationStatus(1L, 1L, "confirmed");

        assertNotNull(response);
        assertEquals("confirmed", response.getStatus());
    }

    /**
     * 测试取消报名
     */
    @Test
    public void testCancelRegistration() {
        when(registrationMapper.selectByActivityIdAndUserId(1L, 1L)).thenReturn(testRegistration);
        when(registrationMapper.updateById(any(ActivityRegistration.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            registrationService.cancelRegistration(1L, 1L);
        });

        verify(registrationMapper).updateById(any(ActivityRegistration.class));
    }

    /**
     * 测试取消报名-报名不存在
     */
    @Test
    public void testCancelRegistrationNotFound() {
        when(registrationMapper.selectByActivityIdAndUserId(999L, 1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.cancelRegistration(1L, 999L);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试取消报名-无权限
     */
    @Test
    public void testCancelRegistrationNoPermission() {
        when(registrationMapper.selectByActivityIdAndUserId(1L, 999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            registrationService.cancelRegistration(999L, 1L);
        });

        assertEquals(404, exception.getCode());
    }
}
