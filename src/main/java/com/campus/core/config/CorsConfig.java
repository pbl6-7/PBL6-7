package com.campus.core.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS跨域配置
 * CORS配置已统一到WebMvcConfig中管理，此类保留作为配置入口
 */
@Configuration
public class CorsConfig {
    // CORS配置已统一到WebMvcConfig.addCorsMappings()中管理
    // 避免多个CORS配置源导致的安全问题
}
