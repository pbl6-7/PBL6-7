package com.campus.core.service;

import com.campus.BaseIntegrationTest;
import com.campus.core.entity.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审计服务集成测试
 */
class AuditServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Test
    void testQuickRecord() throws InterruptedException {
        auditService.quickRecord(1L, 2L, "TEST_OPERATION", "user", 1L, 0, "测试审计记录");
        Thread.sleep(1500);

        List<AuditLog> logs = auditService.getRecentAuditLogs(10);
        assertNotNull(logs);
    }

    @Test
    void testCountAll() {
        Long count = auditService.countAll();
        assertNotNull(count);
        assertTrue(count >= 0);
    }
}
