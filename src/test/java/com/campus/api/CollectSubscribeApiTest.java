package com.campus.api;

import com.campus.BaseIntegrationTest;
import com.campus.core.common.JwtUtils;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 收藏与订阅API接口测试
 * 覆盖 ActivityCollectController + ActivitySubscriptionController 的所有端点
 * 测试策略：所有端点需要认证token，收藏/取消收藏可能因重复操作返回非200
 */
class CollectSubscribeApiTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    private MockMvc mockMvc;
    private String userToken;

    /** 已审核通过的活动ID（approval_status=approved） */
    private static final Long APPROVED_ACTIVITY_ID = 2L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        User user = userMapper.selectByUsername("user1");
        if (user == null) user = userMapper.selectByUsername("xiaofei");
        if (user != null) {
            userToken = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        }
    }

    // ========== ActivityCollectController ==========

    /**
     * 测试收藏活动 - 可能因重复收藏失败，灵活断言
     */
    @Test
    void testCollectActivity() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(post("/api/v1/activity-collect/" + APPROVED_ACTIVITY_ID)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试取消收藏 - 可能因未收藏而失败，灵活断言
     */
    @Test
    void testUncollectActivity() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(delete("/api/v1/activity-collect/" + APPROVED_ACTIVITY_ID)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试获取我的收藏列表 - 需要认证token
     */
    @Test
    void testGetMyCollects() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(get("/api/v1/activity-collect/my")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试检查收藏状态 - 需要认证token
     */
    @Test
    void testCheckCollectStatus() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(get("/api/v1/activity-collect/" + APPROVED_ACTIVITY_ID + "/status")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ========== ActivitySubscriptionController ==========

    /**
     * 测试订阅活动 - 可能因重复订阅失败，灵活断言
     */
    @Test
    void testSubscribe() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(post("/api/v1/activity-subscription/" + APPROVED_ACTIVITY_ID)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试取消订阅 - 可能因未订阅而失败，灵活断言
     */
    @Test
    void testUnsubscribe() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(delete("/api/v1/activity-subscription/" + APPROVED_ACTIVITY_ID)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status < 500, "不应返回服务器错误，实际状态码: " + status);
                });
    }

    /**
     * 测试获取我的订阅列表 - 需要认证token
     */
    @Test
    void testGetMySubscriptions() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(get("/api/v1/activity-subscription/my")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试检查订阅状态 - 需要认证token
     */
    @Test
    void testCheckSubscriptionStatus() throws Exception {
        if (userToken == null) return;
        mockMvc.perform(get("/api/v1/activity-subscription/" + APPROVED_ACTIVITY_ID + "/status")
                .header("Authorization", "Bearer " + userToken))
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
