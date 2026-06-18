package com.campus.core.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * JWT密钥实体类
 * 用于存储JWT密钥信息
 */
@Data
public class JwtKey {

    /**
     * 密钥ID
     */
    private Long id;

    /**
     * 密钥值（Base64编码）
     */
    private String keyValue;

    /**
     * 密钥版本
     */
    private Integer version;

    /**
     * 是否激活
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 激活时间
     */
    private LocalDateTime activatedAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 创建者ID
     */
    private Long createdBy;
}
