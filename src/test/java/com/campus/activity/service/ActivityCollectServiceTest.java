package com.campus.activity.service;

import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityCollect;
import com.campus.activity.mapper.ActivityCollectMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 收藏功能测试
 */
public class ActivityCollectServiceTest {

    @Mock
    private ActivityCollectMapper activityCollectMapper;

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityCollectService activityCollectService;

    private Activity testActivity;
    private ActivityCollect testCollect;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testActivity = new Activity();
        testActivity.setId(1L);
        testActivity.setTitle("测试活动");

        testCollect = new ActivityCollect();
        testCollect.setId(1L);
        testCollect.setUserId(1L);
        testCollect.setActivityId(1L);
    }

    /**
     * 测试正常收藏活动
     */
    @Test
    public void testCollectActivitySuccess() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(activityCollectMapper.selectByUserIdAndActivityId(1L, 1L)).thenReturn(null);
        when(activityCollectMapper.insert(any(ActivityCollect.class))).thenReturn(1);

        assertDoesNotThrow(() -> {
            activityCollectService.collectActivity(1L, 1L);
        });

        verify(activityCollectMapper).insert(any(ActivityCollect.class));
    }

    /**
     * 测试收藏活动-活动不存在
     */
    @Test
    public void testCollectActivityNotFound() {
        when(activityMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityCollectService.collectActivity(1L, 999L);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试收藏活动-重复收藏
     */
    @Test
    public void testCollectActivityAlreadyCollected() {
        when(activityMapper.selectById(1L)).thenReturn(testActivity);
        when(activityCollectMapper.selectByUserIdAndActivityId(1L, 1L)).thenReturn(testCollect);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityCollectService.collectActivity(1L, 1L);
        });

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("已经收藏过"));
    }

    /**
     * 测试正常取消收藏
     */
    @Test
    public void testUncollectActivitySuccess() {
        when(activityCollectMapper.selectByUserIdAndActivityId(1L, 1L)).thenReturn(testCollect);
        when(activityCollectMapper.deleteByUserIdAndActivityId(1L, 1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            activityCollectService.uncollectActivity(1L, 1L);
        });

        verify(activityCollectMapper).deleteByUserIdAndActivityId(1L, 1L);
    }

    /**
     * 测试取消收藏-尚未收藏
     */
    @Test
    public void testUncollectActivityNotCollected() {
        when(activityCollectMapper.selectByUserIdAndActivityId(1L, 1L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityCollectService.uncollectActivity(1L, 1L);
        });

        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("尚未收藏"));
    }

    /**
     * 测试获取用户收藏列表
     */
    @Test
    public void testGetUserCollects() {
        List<ActivityCollect> collects = Arrays.asList(testCollect);
        when(activityCollectMapper.selectByUserId(1L)).thenReturn(collects);

        List<ActivityCollect> result = activityCollectService.getUserCollects(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getActivityId());
    }

    /**
     * 测试获取用户收藏列表-空列表
     */
    @Test
    public void testGetUserCollectsEmpty() {
        when(activityCollectMapper.selectByUserId(1L)).thenReturn(Collections.emptyList());

        List<ActivityCollect> result = activityCollectService.getUserCollects(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试检查是否已收藏-已收藏
     */
    @Test
    public void testIsCollectedTrue() {
        when(activityCollectMapper.selectByUserIdAndActivityId(1L, 1L)).thenReturn(testCollect);

        boolean result = activityCollectService.isCollected(1L, 1L);

        assertTrue(result);
    }

    /**
     * 测试检查是否已收藏-未收藏
     */
    @Test
    public void testIsCollectedFalse() {
        when(activityCollectMapper.selectByUserIdAndActivityId(1L, 1L)).thenReturn(null);

        boolean result = activityCollectService.isCollected(1L, 1L);

        assertFalse(result);
    }

    /**
     * 测试获取活动收藏数
     */
    @Test
    public void testGetCollectCount() {
        when(activityCollectMapper.countByActivityId(1L)).thenReturn(5);

        int count = activityCollectService.getCollectCount(1L);

        assertEquals(5, count);
    }

    /**
     * 测试获取活动收藏数-无收藏
     */
    @Test
    public void testGetCollectCountZero() {
        when(activityCollectMapper.countByActivityId(1L)).thenReturn(0);

        int count = activityCollectService.getCollectCount(1L);

        assertEquals(0, count);
    }
}
