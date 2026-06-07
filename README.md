# 校园活动发布平台

## 项目简介

校园活动发布平台是一个面向高校师生的校园活动管理与发布系统，提供活动发布、报名、评论、收藏、搜索等核心功能，同时包含完善的权限管理、审计日志、缓存管理、限流保护、敏感词过滤等安全与运维特性。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 19 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis | 2.3.2 | ORM框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Caffeine | - | 本地缓存 |
| Spring WebSocket | - | 实时消息推送 |
| Spring Security Crypto | - | 密码加密（BCrypt） |
| JWT (jjwt) | 0.11.5 | 身份认证 |
| Swagger (springfox) | 3.0.0 | API文档 |
| Lombok | - | 代码简化 |
| Maven | 3.6+ | 构建工具 |

## 项目结构

```
src/main/java/com/campus/
├── controller/              # 通用控制器（文件上传、头像、活动图片）
├── core/                    # 核心模块
│   ├── config/              # 配置类（缓存、限流、WebSocket等）
│   ├── controller/          # 核心控制器（审计日志、JWT密钥、权限、限流）
│   ├── constants/           # 常量定义
│   ├── common/              # 通用工具（Result、异常、JwtUtils等）
│   ├── dto/                 # 数据传输对象
│   ├── entity/              # 核心实体
│   ├── service/             # 核心服务
│   ├── util/                # 工具类
│   └── validation/          # 验证分组
├── activity/                # 活动模块
│   ├── controller/          # 活动相关控制器
│   ├── dto/                 # 活动DTO
│   ├── entity/              # 活动实体
│   ├── mapper/              # 活动Mapper
│   ├── service/             # 活动服务
│   └── task/                # 定时任务
├── user/                    # 用户模块
│   ├── controller/          # 用户相关控制器
│   ├── dto/                 # 用户DTO
│   ├── entity/              # 用户实体
│   ├── mapper/              # 用户Mapper
│   └── service/             # 用户服务
├── websocket/               # WebSocket模块
│   ├── controller/          # WebSocket控制器
│   └── ...                  # WebSocket服务与配置
├── entity/                  # 通用实体（FileInfo等）
├── service/                 # 通用服务（文件上传、定时任务等）
├── mapper/                  # 通用Mapper
└── dto/                     # 通用DTO

sql/                         # 数据库脚本
docs/                        # 项目文档
web/                         # 前端项目
```

## 环境要求

- **JDK**: 19+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **操作系统**: Windows / Linux / macOS

## 快速开始

### 1. 环境变量配置

复制环境变量模板并填写实际值：

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```properties
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/campus_activity?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=你的数据库密码

# JWT配置（生产环境必须使用强随机密钥）
# 生成密钥命令：openssl rand -base64 32
JWT_SECRET_KEY=你的JWT密钥至少32字符
```

> **重要提示**：
> - 生产环境必须使用随机生成的强JWT密钥
> - `.env` 文件已在 `.gitignore` 中配置，请勿提交到版本控制

### 2. 数据库初始化

创建数据库 `campus_activity`，然后按顺序执行SQL脚本：

```sql
CREATE DATABASE IF NOT EXISTS campus_activity DEFAULT CHARACTER SET utf8mb4;
```

SQL脚本执行顺序请参考 [部署文档](docs/部署文档.md)。

### 3. 编译与启动

```bash
# 安装依赖
mvn install

# 启动项目
mvn spring-boot:run
```

### 4. 访问项目

- API 文档（Swagger）：http://localhost:8080/swagger-ui/
- 接口基础地址：http://localhost:8080/api/v1/

## 环境变量列表

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| DB_URL | 数据库连接URL | jdbc:mysql://localhost:3306/campus_activity... | 否 |
| DB_USERNAME | 数据库用户名 | root | 否 |
| DB_PASSWORD | 数据库密码 | - | 是 |
| JWT_SECRET_KEY | JWT密钥 | - | 是 |
| SERVER_PORT | 服务器端口 | 8080 | 否 |

完整环境变量列表请参考 [部署文档](docs/部署文档.md)。

## 文档导航

- [API接口文档](API_DOC.md) - 完整的RESTful API接口说明
- [部署文档](docs/部署文档.md) - 环境搭建与部署指南
- [运维文档](docs/运维文档.md) - 系统监控与运维指南

## 安全注意事项

1. **不要提交 `.env` 文件**：已在 `.gitignore` 中配置
2. **生产环境配置**：使用强密码和随机JWT密钥，定期更换密钥
3. **密钥管理**：项目已实现JWT密钥动态管理功能，支持密钥自动轮换（默认30天）
4. **配置加密**：使用 `ConfigEncryptUtil` 对敏感配置进行加密存储
5. **限流保护**：系统内置API限流机制，防止恶意请求
