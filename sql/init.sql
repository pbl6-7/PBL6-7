CREATE TABLE IF NOT EXISTS `user_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `real_name` VARCHAR(100) DEFAULT NULL,
    `role` VARCHAR(20) DEFAULT 'user',
    `contact` VARCHAR(100) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `jwt_key` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `key_value` VARCHAR(255) NOT NULL COMMENT '密钥值（Base64编码）',
    `version` INT NOT NULL COMMENT '密钥版本',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    `expire_at` DATETIME COMMENT '过期时间',
    INDEX `idx_version` (`version`),
    INDEX `idx_active` (`is_active`),
    INDEX `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JWT密钥管理表';

CREATE TABLE IF NOT EXISTS `login_lock` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '锁定记录ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `lock_time` DATETIME NOT NULL COMMENT '锁定时间',
    `unlock_time` DATETIME COMMENT '解锁时间（预计）',
    `fail_count` INT DEFAULT 0 COMMENT '失败次数',
    `lock_reason` VARCHAR(255) COMMENT '锁定原因',
    `is_locked` BOOLEAN DEFAULT TRUE COMMENT '是否锁定',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_username` (`username`),
    INDEX `idx_locked` (`is_locked`),
    INDEX `idx_lock_time` (`lock_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录锁定记录表';

CREATE TABLE IF NOT EXISTS `activity` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `title` VARCHAR(200) NOT NULL COMMENT '活动名称',
    `publisher_id` BIGINT NOT NULL COMMENT '发布者ID，关联user_info表',
    `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
    `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
    `location` VARCHAR(200) NOT NULL COMMENT '活动地点',
    `description` TEXT COMMENT '活动描述',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '活动状态: draft-草稿, published-已发布, cancelled-已取消, ended-已结束',
    `approval_status` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态: pending-待审核, approved-已通过, rejected-已拒绝',
    `max_participants` INT DEFAULT 0 COMMENT '最大参与人数，0表示不限',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_status` (`status`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动信息表';

CREATE TABLE IF NOT EXISTS `activity_subscription` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动订阅表';

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `activity_id` BIGINT NOT NULL COMMENT '活动ID，关联activity表',
    `type` VARCHAR(50) NOT NULL COMMENT '通知类型: SUBSCRIPTION_STATUS-订阅状态变更, ACTIVITY_UPDATE-活动更新, ACTIVITY_START-活动开始, ACTIVITY_END-活动结束',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_activity_id` (`activity_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';
