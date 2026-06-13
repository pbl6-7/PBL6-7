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
 *
 * 对于公开GET接口（如活动详情），允许无Token访问，
 * 但如果有Token仍会解析并设置用户信息
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    private static final String ATTRIBUTE_USER_ID = "currentUserId";
    private static final String ATTRIBUTE_USER_ROLE = "currentUserRole";

    /**
     * 允许无Token访问的GET路径模式
     * 这些路径GET请求不需要登录，但PUT/DELETE仍需登录
     */
    private static final java.util.List<String> PUBLIC_GET_PATTERNS = java.util.Arrays.asList(
            "/api/v1/activities/",   // 活动详情 GET /api/v1/activities/{id}
            "/api/v1/users/"         // 用户信息 GET /api/v1/users/{id}
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // 判断是否为公开GET请求（允许无Token访问）
        boolean isPublicGetRequest = "GET".equals(method) && isPublicGetPath(requestURI);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 公开GET请求允许无Token，不设置用户信息但放行
            if (isPublicGetRequest) {
                return true;
            }
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权或登录已过期\"}");
            return false;
        }

        String token = authorization.substring(7);

        if (!jwtUtils.validateToken(token)) {
            // 公开GET请求Token无效时仍放行（不设置用户信息）
            if (isPublicGetRequest) {
                return true;
            }
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

    /**
     * 判断请求路径是否为公开GET路径
     *
     * @param requestURI 请求URI
     * @return 是否为公开路径
     */
    private boolean isPublicGetPath(String requestURI) {
        for (String pattern : PUBLIC_GET_PATTERNS) {
            if (requestURI.startsWith(pattern)) {
                // 确保路径后面跟的是数字ID，如 /api/v1/activities/123
                String suffix = requestURI.substring(pattern.length());
                if (suffix.matches("\\d+.*")) {
                    return true;
                }
            }
        }
        return false;
    }
}
