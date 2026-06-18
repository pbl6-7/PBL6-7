package com.campus.activity.mapper;

import com.campus.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 活动分享Mapper集成测试
 */
class ActivityShareMapperTest extends BaseIntegrationTest {

    @Autowired
    private ActivityShareMapper activityShareMapper;

    @Test
    void testInsertShare() {
        assertDoesNotThrow(() -> activityShareMapper.insertShare(1L, 1L));
    }

    @Test
    void testCountByActivityId() {
        Long count = activityShareMapper.countByActivityId(1L);
        assertNotNull(count);
        assertTrue(count >= 0);
    }

    @Test
    void testCheckUserShared() {
        Integer result = activityShareMapper.checkUserShared(1L, 1L);
        assertNotNull(result);
    }
}
