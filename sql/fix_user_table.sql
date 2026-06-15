-- 修复 user_info 表结构 - 添加缺失的字段
-- 执行此 SQL 修复数据库表结构
-- 注意: 如果字段已存在会报错,可以忽略错误或先检查字段是否存在

-- 方法1: 直接添加字段(如果字段不存在)
ALTER TABLE user_info 
ADD COLUMN `status` VARCHAR(20) DEFAULT 'enabled' COMMENT '用户状态: enabled-启用, disabled-禁用' AFTER `role`,
ADD COLUMN `avatar` VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL' AFTER `status`;

-- 方法2: 如果需要安全执行,可以先检查字段是否存在
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'user_info' AND COLUMN_NAME IN ('status', 'avatar');
