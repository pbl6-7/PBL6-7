package com.campus.user.service;

import com.campus.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 登录锁定服务集成测试
 */
class LoginLockServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LoginLockService loginLockService;

    @Test
    void testRecordLoginFailure() {
        int count = loginLockService.recordLoginFailure("testuser_lock_" + System.currentTimeMillis());
        assertEquals(1, count);
    }

    @Test
    void testLockAndUnlock() {
        String username = "locktest_" + System.currentTimeMillis();
        loginLockService.recordLoginFailure(username);
        loginLockService.lockUser(username);
        assertTrue(loginLockService.isUserLocked(username));

        loginLockService.unlockUser(username);
        assertFalse(loginLockService.isUserLocked(username));
    }

    @Test
    void testGetLockedList() {
        String username = "lockedlist_" + System.currentTimeMillis();
        loginLockService.recordLoginFailure(username);
        loginLockService.lockUser(username);

        List<Map<String, Object>> lockedList = loginLockService.getLockedList();
        assertNotNull(lockedList);
        assertTrue(lockedList.size() > 0);
    }

    @Test
    void testGetMaxLoginFailCount() {
        assertEquals(5, loginLockService.getMaxLoginFailCount());
    }

    @Test
    void testClearLoginFailure() {
        String username = "clearfail_" + System.currentTimeMillis();
        loginLockService.recordLoginFailure(username);
        loginLockService.clearLoginFailure(username);
        assertEquals(0, loginLockService.getLoginFailCount(username));
    }
}
