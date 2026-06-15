-- 修复 user_info 表结构
ALTER TABLE user_info ADD COLUMN status VARCHAR(20) DEFAULT 'enabled' COMMENT '用户状态: enabled-禁用' AFTER role;
ALTER TABLE user_info ADD COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL' AFTER status;
