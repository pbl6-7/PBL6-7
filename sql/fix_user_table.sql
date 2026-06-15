-- 修复 user_info 表结构 - 添加缺失的字段
-- 执行此 SQL 修复数据库表结构

ALTER TABLE user_info 
ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT 'enabled' COMMENT '用户状态: enabled-启用, disabled-禁用' AFTER `role`,
ADD COLUMN IF NOT EXISTS `avatar` VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL' AFTER `status`;
