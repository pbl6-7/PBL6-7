package com.campus.core.config;

import com.campus.core.common.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT认证拦截器
 * 统一处理Token验证，避免在Controller中重复代码
 * 验证通过后，将userId和role存入request attribute供后续使用
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    private static final String ATTRIBUTE_USER_ID = "currentUserId";
    private static final String ATTRIBUTE_USER_ROLE = "currentUserRole";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method) && requestURI.matches(".*/api/v1/activities/\\d+/comments$")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权或登录已过期\"}");
            return false;
        }

        String token = authorization.substring(7);

        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":4008,\"message\":\"无效的令牌\"}");
            return false;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);

        request.setAttribute(ATTRIBUTE_USER_ID, userId);
        request.setAttribute(ATTRIBUTE_USER_ROLE, role);

        return true;
    }
}
