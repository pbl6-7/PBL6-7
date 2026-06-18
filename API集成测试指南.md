# 前后端API集成测试

## 测试前提
- 后端服务运行在 http://localhost:8080
- MySQL数据库已启动
- 已注册测试用户（用户名: testuser, 密码: 123456）

## 测试用例

### 1. 用户登录API测试

```bash
# 登录接口测试
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

**检查点：**
- 返回是否包含 token
- 返回用户信息是否完整（userId, username, realName, role）

---

### 2. 活动CRUD API测试

```bash
# 获取Token（先登录）
TOKEN=$(curl -s -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' | jq -r '.data.token')

# 获取活动列表
curl -X GET "http://localhost:8080/activities/list?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"

# 发布活动
curl -X POST http://localhost:8080/activities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "测试活动",
    "description": "活动描述",
    "location": "图书馆",
    "startTime": "2026-06-01T10:00:00",
    "endTime": "2026-06-01T12:00:00"
  }'

# 获取单个活动详情
curl -X GET http://localhost:8080/activities/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

### 3. 活动报名API测试

```bash
# 获取Token
TOKEN=$(curl -s -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' | jq -r '.data.token')

# 报名活动
curl -X POST http://localhost:8080/registrations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"activityId": 1}'

# 获取我的报名列表
curl -X GET "http://localhost:8080/registrations/my?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

### 4. 收藏功能API测试

```bash
# 获取Token
TOKEN=$(curl -s -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' | jq -r '.data.token')

# 收藏活动
curl -X POST http://localhost:8080/activity-collect/1 \
  -H "Authorization: Bearer $TOKEN"

# 检查收藏状态
curl -X GET http://localhost:8080/activity-collect/1/status \
  -H "Authorization: Bearer $TOKEN"

# 获取收藏列表
curl -X GET "http://localhost:8080/activity-collect/my?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"

# 取消收藏
curl -X DELETE http://localhost:8080/activity-collect/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

### 5. 评论功能API测试

```bash
# 获取Token
TOKEN=$(curl -s -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' | jq -r '.data.token')

# 发布评论
curl -X POST http://localhost:8080/activities/1/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"content": "这是一条测试评论"}'

# 获取评论列表
curl -X GET "http://localhost:8080/activities/1/comments?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

### 6. 搜索功能API测试

```bash
# 获取搜索建议
curl -X GET "http://localhost:8080/search/suggestions?prefix=学术"

# 获取自动补全
curl -X GET "http://localhost:8080/search/autocomplete?prefix=校园"

# 获取热门搜索
curl -X GET http://localhost:8080/search/hot
```

---

## 常见问题排查

### 1. Token相关问题
```bash
# 检查Token是否生成
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}' | jq .

# 如果token为null，检查：
# - 用户是否存在
# - 密码是否正确（SHA-256加密）
# - 数据库连接是否正常
```

### 2. 403权限问题
```bash
# 检查用户角色
curl -X GET http://localhost:8080/users/profile \
  -H "Authorization: Bearer YOUR_TOKEN" | jq .

# 某些API需要admin角色，如果用普通用户调用会返回403
```

### 3. 404问题
```bash
# 检查API路径是否正确
# 对比前端API调用路径和后端Controller路径

# 常见错误：
# 前端: /activity/1
# 后端: /activities/1
```

### 4. 500服务器错误
```bash
# 查看后端日志
# 检查：
# - 数据库字段是否匹配
# - 必填参数是否传递
# - 时间格式是否正确（ISO 8601格式）
```

---

## 使用Postman/Insomnia测试

如果不想用curl，可以导入以下JSON到Postman：

```json
{
  "info": {
    "name": "校园活动平台API测试",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "登录",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/users/login",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {"mode": "raw", "raw": "{\"username\":\"testuser\",\"password\":\"123456\"}"}
      }
    }
  ]
}
```
