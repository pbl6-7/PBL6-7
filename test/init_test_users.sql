-- ============================================================
-- 接口测试用户初始化脚本
-- 创建三个角色的测试用户：管理员、发布者、普通用户
-- 密码统一为: Test@1234 (符合强密码要求：8位+大小写+数字+特殊字符)
-- ============================================================

-- 清理旧测试用户（如果存在）
DELETE FROM user_info WHERE username IN ('test_admin', 'test_publisher', 'test_user');

-- 插入管理员用户
-- 用户名: test_admin  密码: Test@1234
INSERT INTO user_info (username, password, real_name, role, contact)
VALUES ('test_admin', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', '测试管理员', 'ADMIN', '13800000001');

-- 插入发布者用户
-- 用户名: test_publisher  密码: Test@1234
INSERT INTO user_info (username, password, real_name, role, contact)
VALUES ('test_publisher', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', '测试发布者', 'PUBLISHER', '13800000002');

-- 插入普通用户
-- 用户名: test_user  密码: Test@1234
INSERT INTO user_info (username, password, real_name, role, contact)
VALUES ('test_user', 'jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI=', '测试用户', 'USER', '13800000003');
