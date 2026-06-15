package com.campus.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

    @Value("${file.upload.path:D:/PBL6-7/uploads}")
    private String uploadPath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 uploads 目录到 /uploads/** URL
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:5174", "http://localhost:8080", "http://127.0.0.1:5173", "http://127.0.0.1:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-User-Id")
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
                        // 注意：/api/v1/activities/{id} 和 /api/v1/users/{id} 不再排除
                        // JWT拦截器内部已处理：GET请求允许无Token访问，PUT/DELETE需要Token
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
                        // 注意：/api/v1/activities/{id} 不再排除
                        // PUT/DELETE需要ACTIVITY_UPDATE/ACTIVITY_DELETE权限验证
                        // GET活动详情不需要权限验证，但权限拦截器对空权限集会放行
                        "/api/v1/activities/list",
                        "/api/v1/activities/my",
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
