package com.campus.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录锁定记录实体类
 * 用于记录用户登录锁定信息
 */
@Data
public class LoginLock {

    /**
     * 锁定记录ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 锁定时间
     */
    private LocalDateTime lockTime;

    /**
     * 解锁时间（预计）
     */
    private LocalDateTime unlockTime;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 锁定原因
     */
    private String lockReason;

    /**
     * 是否锁定
     */
    private Boolean isLocked;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
