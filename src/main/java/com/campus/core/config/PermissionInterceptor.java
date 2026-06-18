package com.campus.core.config;

import com.campus.core.constants.UserRoleConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限拦截器
 * 用于验证用户权限，保护管理员等需要特定角色的接口
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 获取当前用户角色
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        Long currentUserId = (Long) request.getAttribute("currentUserId");

        // 管理员路径权限校验
        if (requestURI.startsWith("/api/admin/")) {
            if (currentUserId == null || currentUserRole == null) {
                log.warn("未登录用户尝试访问管理员路径: {}", requestURI);
                sendForbiddenResponse(response, "请先登录");
                return false;
            }
            if (!UserRoleConstants.ADMIN.equalsIgnoreCase(currentUserRole)) {
                log.warn("非管理员用户(userId={}, role={})尝试访问管理员路径: {}", currentUserId, currentUserRole, requestURI);
                sendForbiddenResponse(response, "权限不足，需要管理员权限");
                return false;
            }
        }

        return true;
    }

    /**
     * 发送权限不足的JSON响应
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", message);
        result.put("success", false);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
