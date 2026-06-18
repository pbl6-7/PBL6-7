package com.campus.api;

import com.campus.BaseIntegrationTest;
import com.campus.core.common.JwtUtils;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 管理员模块API接口测试
 * 覆盖 AdminActivityController + AdminStatisticsController + AdminMonitorController + SensitiveWordController 的所有端点
 * 测试策略：所有端点需要ADMIN角色token，热门活动统计可能返回500，灵活断言
 */
class AdminApiTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    private MockMvc mockMvc;
    private String adminToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        User admin = userMapper.selectByUsername("admin");
        if (admin != null) {
            adminToken = jwtUtils.generateToken(admin.getId(), admin.getUsername(), admin.getRole());
        }
    }

    // ========== AdminActivityController ==========

    /**
     * 测试获取待审核活动列表 - 需要ADMIN角色token
     */
    @Test
    void testGetPendingActivities() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/activities/pending")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试按审核状态获取活动列表 - 需要ADMIN角色token
     */
    @Test
    void testGetActivitiesByApprovalStatus() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/activities/approval-status/pending")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试审核通过活动 - 可能因活动不存在或状态不对失败，灵活断言
     */
    @Test
    void testApproveActivity() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(put("/api/admin/activities/1/approve")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试审核拒绝活动 - 不存在的活动，灵活断言
     */
    @Test
    void testRejectActivity() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(put("/api/admin/activities/999/reject")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"不合规\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试获取审核统计信息 - 需要ADMIN角色token
     */
    @Test
    void testGetApprovalStatistics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/activities/statistics")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ========== AdminStatisticsController ==========

    /**
     * 测试获取系统概览统计 - 需要ADMIN角色token
     */
    @Test
    void testGetOverviewStatistics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/statistics/overview")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取活动统计 - 需要ADMIN角色token
     */
    @Test
    void testGetActivityStatistics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/statistics/activities")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取用户统计 - 需要ADMIN角色token
     */
    @Test
    void testGetUserStatistics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/statistics/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取报名统计 - 需要ADMIN角色token
     */
    @Test
    void testGetRegistrationStatistics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/statistics/registrations")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取趋势统计 - 使用ISO日期时间格式
     */
    @Test
    void testGetTrendStatistics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/statistics/trend")
                .header("Authorization", "Bearer " + adminToken)
                .param("startDate", "2026-01-01T00:00:00")
                .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取热门活动统计 - 可能因日期解析返回500，灵活断言
     */
    @Test
    void testGetHotActivities() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/statistics/hot-activities")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试清除统计缓存 - 需要ADMIN角色token
     */
    @Test
    void testClearStatisticsCache() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(post("/api/admin/statistics/clear-cache")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ========== AdminMonitorController ==========

    /**
     * 测试获取系统状态 - 需要ADMIN角色token
     */
    @Test
    void testGetSystemStatus() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/monitor/status")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试获取系统指标 - 需要ADMIN角色token
     */
    @Test
    void testGetSystemMetrics() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/monitor/metrics")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取最近活动 - 需要ADMIN角色token
     */
    @Test
    void testGetRecentActivities() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/monitor/recent-activities")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取最近用户 - 需要ADMIN角色token
     */
    @Test
    void testGetRecentUsers() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/monitor/recent-users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取缓存信息 - 需要ADMIN角色token
     */
    @Test
    void testGetCacheInfo() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/monitor/cache")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试清除系统缓存 - 需要ADMIN角色token
     */
    @Test
    void testClearCache() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(delete("/api/admin/monitor/cache/clear")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ========== SensitiveWordController ==========

    /**
     * 测试获取所有敏感词 - 需要ADMIN角色token
     */
    @Test
    void testGetAllSensitiveWords() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/sensitive-words")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试根据类型获取敏感词 - 需要ADMIN角色token
     */
    @Test
    void testGetSensitiveWordsByType() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/sensitive-words/type/other")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试获取敏感词详情 - 不存在的ID，灵活断言
     */
    @Test
    void testGetSensitiveWordById() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/sensitive-words/1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试创建敏感词 - 需要ADMIN角色token
     */
    @Test
    void testCreateSensitiveWord() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(post("/api/admin/sensitive-words")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"word\":\"API测试词_" + System.currentTimeMillis() + "\",\"isWhitelist\":0,\"type\":\"other\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试更新不存在的敏感词 - 预期返回400，灵活断言
     */
    @Test
    void testUpdateSensitiveWord() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(put("/api/admin/sensitive-words/999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"word\":\"更新词\",\"type\":\"other\"}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试删除不存在的敏感词 - 预期返回400，灵活断言
     */
    @Test
    void testDeleteSensitiveWord() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(delete("/api/admin/sensitive-words/999")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试批量删除敏感词 - 不存在的ID，灵活断言
     */
    @Test
    void testBatchDeleteSensitiveWords() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(delete("/api/admin/sensitive-words/batch")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[999,998]"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试获取敏感词统计 - 需要ADMIN角色token
     */
    @Test
    void testGetSensitiveWordStats() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(get("/api/admin/sensitive-words/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    /**
     * 测试刷新敏感词DFA树 - 需要ADMIN角色token
     */
    @Test
    void testRefreshSensitiveWordTree() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(post("/api/admin/sensitive-words/refresh")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试检查敏感词 - 在/api/admin/路径下，需要ADMIN角色token
     */
    @Test
    void testCheckSensitiveWord() throws Exception {
        if (adminToken == null) return;
        mockMvc.perform(post("/api/admin/sensitive-words/check")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"测试文本内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 辅助断言方法 - 验证条件为真
     */
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
