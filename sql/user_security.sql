-- 密保问题表
-- 用于存储用户的密保问题信息，支持密码找回功能
CREATE TABLE IF NOT EXISTS `user_security` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联user_info表',
    `security_question_id` INT NOT NULL COMMENT '密保问题编号(1-8)',
    `security_answer` VARCHAR(255) NOT NULL COMMENT '密保答案(加密存储)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户密保信息表';
