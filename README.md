# 校园活动平台

## 环境要求

- JDK 19+
- Maven 3.6+
- MySQL 8.0+

## 运行步骤

### 1. 初始化数据库

创建数据库 `campus_db`，然后执行 `sql/init.sql` 初始化数据表。

### 2. 配置数据库

修改 `src/main/resources/application.yml` 中的数据库密码：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
```

### 3. 安装依赖并运行

```bash
# 安装依赖
mvn install

# 启动项目
mvn spring-boot:run
```

### 4. 访问项目

- API 文档：http://localhost:8080/swagger-ui/
- 接口地址：http://localhost:8080/
