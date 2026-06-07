# 校园活动平台

## 环境要求

- JDK 19+
- Maven 3.6+
- MySQL 8.0+

## 运行步骤

### 1. 初始化数据库

创建数据库 `campus_db`，然后执行 `sql/init.sql` 初始化数据表。

### 2. 配置环境变量

本项目使用环境变量管理敏感配置信息，请按以下步骤配置：

#### 2.1 复制环境变量模板

```bash
# 复制环境变量模板文件
cp .env.example .env
```

#### 2.2 编辑环境变量

编辑 `.env` 文件，填写实际的配置值：

```properties
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/campus_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=你的数据库密码

# JWT配置（重要：生产环境必须使用强随机密钥）
# 生成密钥命令：openssl rand -base64 32
JWT_SECRET_KEY=你的JWT密钥至少32字符

# 其他配置使用默认值即可
```

#### 2.3 重要配置说明

**数据库密码**：
- 开发环境：修改 `DB_PASSWORD` 为你的MySQL密码
- 生产环境：使用强密码，并确保数据库访问权限受限

**JWT密钥**：
- 长度要求：至少32个字符（256位）
- 生成方法：使用 `openssl rand -base64 32` 生成随机密钥
- 安全提示：生产环境必须使用随机生成的强密钥，不要使用默认值

### 3. 配置方式（可选）

除了使用 `.env` 文件，还支持以下配置方式：

#### 方式一：系统环境变量

```bash
# Linux/Mac
export DB_PASSWORD=your_password
export JWT_SECRET_KEY=your_secret_key

# Windows PowerShell
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET_KEY="your_secret_key"
```

#### 方式二：IDE运行配置

在IDEA中配置环境变量：
1. 打开 Run/Debug Configurations
2. 在 Environment variables 中添加配置
3. 格式：`DB_PASSWORD=your_password;JWT_SECRET_KEY=your_secret_key`

### 4. 安装依赖并运行

```bash
# 安装依赖
mvn install

# 启动项目
mvn spring-boot:run
```

### 5. 访问项目

- API 文档：http://localhost:8080/swagger-ui/
- 接口地址：http://localhost:8080/

## 配置加密工具

项目提供了 `ConfigEncryptUtil` 工具类，用于敏感配置的加密和解密：

### AES加密解密

```java
// 加密敏感数据
String encrypted = ConfigEncryptUtil.encrypt("your-secret-key", "sensitive-data");

// 解密数据
String decrypted = ConfigEncryptUtil.decrypt("your-secret-key", encrypted);

// 生成加密密钥
String key = ConfigEncryptUtil.generateKey();
```

### BCrypt密码加密

```java
// 加密密码
String hashedPassword = ConfigEncryptUtil.hashPassword("password123");

// 验证密码
boolean matches = ConfigEncryptUtil.matchesPassword("password123", hashedPassword);
```

## 安全注意事项

1. **不要提交 `.env` 文件**：`.env` 文件已在 `.gitignore` 中配置，请勿提交到版本控制
2. **生产环境配置**：
   - 使用强密码和随机JWT密钥
   - 定期更换密钥
   - 限制数据库访问权限
3. **密钥管理**：
   - 项目已实现JWT密钥动态管理功能
   - 支持密钥自动轮换（默认30天）
   - 详细使用说明请查看 `docs/JWT密钥管理使用说明.md`
   - 生产环境建议使用专业的密钥管理服务（如 Vault、AWS Secrets Manager）
4. **配置加密**：对于特别敏感的配置，使用 `ConfigEncryptUtil` 进行加密存储

## 环境变量列表

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| DB_URL | 数据库连接URL | jdbc:mysql://localhost:3306/campus_db... | 否 |
| DB_USERNAME | 数据库用户名 | root | 否 |
| DB_PASSWORD | 数据库密码 | - | 是 |
| JWT_SECRET_KEY | JWT密钥 | - | 是 |
| JWT_EXPIRATION | JWT过期时间(ms) | 86400000 | 否 |
| JWT_KEY_MANAGEMENT_ENABLED | 是否启用密钥管理 | true | 否 |
| JWT_KEY_ROTATION_DAYS | 密钥轮换周期(天) | 30 | 否 |
| JWT_KEY_RETENTION_DAYS | 旧密钥保留时间(天) | 7 | 否 |
| JWT_KEY_AUTO_ROTATION_ENABLED | 是否启用自动轮换 | true | 否 |
| SERVER_PORT | 服务器端口 | 8080 | 否 |
| PAGINATION_DEFAULT_PAGE_SIZE | 分页默认大小 | 10 | 否 |
| PAGINATION_MAX_PAGE_SIZE | 分页最大大小 | 100 | 否 |
| COMMENT_MAX_CONTENT_LENGTH | 评论最大长度 | 1000 | 否 |
| COMMENT_MAX_REPLY_DEPTH | 评论最大深度 | 10 | 否 |
| ACTIVITY_MAX_TITLE_LENGTH | 活动标题最大长度 | 200 | 否 |
| ACTIVITY_MAX_LOCATION_LENGTH | 活动地点最大长度 | 500 | 否 |
| ACTIVITY_MAX_DESCRIPTION_LENGTH | 活动描述最大长度 | 5000 | 否 |
| ACTIVITY_MAX_PARTICIPANTS | 活动最大参与人数 | 100000 | 否 |
| SEARCH_DEFAULT_LIMIT | 搜索默认限制 | 10 | 否 |
| SEARCH_MAX_LIMIT | 搜索最大限制 | 100 | 否 |
| FILE_UPLOAD_MAX_FILE_SIZE | 文件上传最大大小 | 10485760 | 否 |
| LOGGING_LEVEL | 日志级别 | debug | 否 |