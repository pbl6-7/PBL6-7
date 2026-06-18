package com.campus.activity.config;

/**
 * Web配置类（已废弃）
 * 静态资源映射和CORS配置统一在 WebMvcConfig 中管理
 * 此类保留仅为兼容性，不再注册任何配置
 */

//@Configuration  // 已注释，避免与 WebMvcConfig 重复注册资源处理器
public class WebConfig {
    // 所有配置已迁移到 com.campus.core.config.WebMvcConfig
}
