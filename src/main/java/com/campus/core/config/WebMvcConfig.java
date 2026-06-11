package com.campus.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置拦截器、CORS等
 *
 * @author Campus Team
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 限流拦截器 - 优先级最高，拦截所有API请求
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .order(0);

        // JWT认证拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/users/login",
                        "/api/v1/users/register",
                        // 密保公开接口（无需登录）
                        "/api/v1/users/security/questions",
                        "/api/v1/users/security/username/**",
                        "/api/v1/users/security/verify",
                        "/api/v1/users/security/reset-password",
                        // 注意：/api/v1/users/security/set 需要登录验证，不排除
                        "/api/v1/users/security/user/**",
                        "/api/v1/users/{id:[\\d]+}",
                        "/api/v1/activities/{id:[\\d]+}",
                        // 搜索公开端点 - 无需登录即可访问
                        "/api/v1/search/suggestions",
                        "/api/v1/search/autocomplete",
                        "/api/v1/search/hot"
                )
                .order(1);

        // 权限验证拦截器 - 在JWT认证之后执行
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/users/login",
                        "/api/v1/users/register",
                        "/api/v1/users/security/**",
                        "/api/v1/users/{id:[\\d]+}",
                        "/api/v1/activities/{id:[\\d]+}",
                        "/api/v1/activities/search",
                        "/api/v1/activities/types",
                        "/api/v1/activities/tags",
                        // 搜索公开端点 - 无需权限验证
                        "/api/v1/search/suggestions",
                        "/api/v1/search/autocomplete",
                        "/api/v1/search/hot",
                        "/api/v1/search/history"  // 用户搜索历史需要登录但不需要特殊权限
                        // 注意：评论接口不排除，DELETE /api/v1/comments/{id} 需要权限验证
                )
                .order(2);
    }
}
