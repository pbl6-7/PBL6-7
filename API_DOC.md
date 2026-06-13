# 校园活动平台 API 接口文档

## 一、概述

### 1.1 文档说明
本文档描述了校园活动平台的RESTful API接口设计，包括用户管理、活动管理、评论管理、收藏管理、报名管理、智能搜索、审计日志、缓存管理、限流管理、JWT密钥管理、权限管理、文件上传、用户头像、活动图片、活动分享、活动状态管理、搜索历史、WebSocket、统计报表、定时任务、登录锁定、敏感词等模块。

### 1.2 基础信息
- **基础URL**: `/api/v1`（业务接口）、`/api/admin`（管理接口）
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: Bearer Token (JWT)

### 1.3 通用响应格式
所有接口响应均采用统一格式：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {},
    "timestamp": "2026-05-07T10:30:00",
    "requestId": null
}
```

**响应码说明**:
| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权或登录已过期 |
| 403 | 没有操作权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 422 | 数据验证失败 |
| 429 | 请求过于频繁（限流） |
| 500 | 服务器内部错误 |

**业务错误码说明**:
| 错误码 | 说明 |
|--------|------|
| 4001 | 用户不存在 |
| 4002 | 用户名已存在 |
| 4003 | 密码错误 |
| 4004 | 密码格式不正确 |
| 4005 | 该用户未设置密保问题 |
| 4006 | 无效的密保问题编号 |
| 4007 | 密保答案错误 |
| 4008 | 无效的令牌 |
| 4009 | 令牌已过期 |

### 1.4 认证说明
除登录、注册、获取密保问题等公开接口外，大多数接口需要在请求头中携带JWT Token：

```
Authorization: Bearer <token>
```

---

## 二、用户管理模块 (User)

### 2.1 用户登录
- **接口路径**: `POST /api/v1/users/login`
- **标签**: 用户管理
- **描述**: 用户登录系统，获取JWT令牌
- **是否需要认证**: 否

**请求参数**:
```json
{
    "username": "string",
    "password": "string"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "userId": 1,
        "username": "zhangsan",
        "realName": "张三",
        "role": "USER",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 2.2 用户注册
- **接口路径**: `POST /api/v1/users/register`
- **标签**: 用户管理
- **描述**: 用户注册新账号
- **是否需要认证**: 否

**请求参数**:
```json
{
    "id": 0,
    "username": "string",
    "password": "string",
    "realName": "string",
    "contact": "string",
    "role": "USER",
    "securityQuestionId": 1,
    "securityAnswer": "答案"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "注册成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 2.3 获取用户信息
- **接口路径**: `GET /api/v1/users/{id}`
- **标签**: 用户管理
- **描述**: 根据用户ID获取用户基本信息
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "zhangsan",
        "realName": "张三",
        "contact": "13800138000",
        "role": "USER",
        "createdAt": "2026-05-01T10:00:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 2.4 修改密码
- **接口路径**: `PUT /api/v1/users/password`
- **标签**: 用户管理
- **描述**: 已登录用户修改密码
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
    "oldPassword": "string",
    "newPassword": "string"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "密码修改成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 2.5 获取当前用户个人信息
- **接口路径**: `GET /api/v1/users/profile`
- **标签**: 用户管理
- **描述**: 获取当前登录用户的详细个人信息
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "zhangsan",
        "realName": "张三",
        "contact": "13800138000",
        "role": "USER",
        "createdAt": "2026-05-01T10:00:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 2.6 修改个人资料
- **接口路径**: `PUT /api/v1/users/profile`
- **标签**: 用户管理
- **描述**: 修改当前登录用户的个人资料
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
    "realName": "string",
    "contact": "string"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "个人资料修改成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 三、密保管理模块 (UserSecurity)

### 3.1 获取密保问题列表
- **接口路径**: `GET /api/v1/users/security/questions`
- **标签**: 密保管理
- **描述**: 获取系统中所有可用的密保问题
- **是否需要认证**: 否

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "question": "您最喜欢的宠物名字是？"
        },
        {
            "id": 2,
            "question": "您的出生地是？"
        }
    ],
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 3.2 获取当前用户的密保问题
- **接口路径**: `GET /api/v1/users/security/user/{userId}`
- **标签**: 密保管理
- **描述**: 获取指定用户的密保问题（用于修改密保时展示）
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "question": "您最喜欢的宠物名字是？"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 3.3 根据用户名获取密保问题
- **接口路径**: `GET /api/v1/users/security/username/{username}`
- **标签**: 密保管理
- **描述**: 根据用户名获取密保问题（用于找回密码第一步）
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "question": "您最喜欢的宠物名字是？"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 3.4 设置密保问题
- **接口路径**: `POST /api/v1/users/security/set`
- **标签**: 密保管理
- **描述**: 设置或修改用户的密保问题和答案
- **是否需要认证**: 否

**请求参数**:
```json
{
    "userId": 1,
    "securityQuestionId": 1,
    "securityAnswer": "小花"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "密保设置成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 3.5 验证密保答案
- **接口路径**: `POST /api/v1/users/security/verify`
- **标签**: 密保管理
- **描述**: 验证用户输入的密保答案是否正确
- **是否需要认证**: 否

**请求参数**:
```json
{
    "username": "string",
    "securityAnswer": "string"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "验证成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 3.6 重置密码
- **接口路径**: `POST /api/v1/users/security/reset-password`
- **标签**: 密保管理
- **描述**: 通过密保验证后重置密码
- **是否需要认证**: 否

**请求参数**:
```json
{
    "username": "string",
    "securityAnswer": "string",
    "newPassword": "string"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "密码重置成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 四、活动管理模块 (Activity)

### 4.1 发布活动
- **接口路径**: `POST /api/v1/activities`
- **标签**: 活动管理
- **描述**: 创建并发布一个新的校园活动
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
    "title": "校园马拉松比赛",
    "startTime": "2026-05-20T09:00:00",
    "endTime": "2026-05-20T17:00:00",
    "location": "学校操场",
    "description": "全校师生参与的马拉松活动",
    "maxParticipants": 500
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 活动名称 |
| startTime | LocalDateTime | 是 | 活动开始时间 |
| endTime | LocalDateTime | 是 | 活动结束时间 |
| location | String | 是 | 活动地点 |
| description | String | 否 | 活动描述 |
| maxParticipants | Integer | 否 | 最大参与人数 |

**响应示例**:
```json
{
    "code": 200,
    "message": "活动发布成功",
    "data": {
        "id": 1,
        "title": "校园马拉松比赛",
        "publisherId": 1,
        "publisherName": "张三",
        "startTime": "2026-05-20T09:00:00",
        "endTime": "2026-05-20T17:00:00",
        "location": "学校操场",
        "description": "全校师生参与的马拉松活动",
        "status": "PUBLISHED",
        "approvalStatus": "APPROVED",
        "maxParticipants": 500,
        "createdAt": "2026-05-07T10:30:00",
        "updatedAt": "2026-05-07T10:30:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 4.2 获取活动详情
- **接口路径**: `GET /api/v1/activities/{id}`
- **标签**: 活动管理
- **描述**: 根据活动ID获取活动详细信息
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "title": "校园马拉松比赛",
        "publisherId": 1,
        "publisherName": "张三",
        "startTime": "2026-05-20T09:00:00",
        "endTime": "2026-05-20T17:00:00",
        "location": "学校操场",
        "description": "全校师生参与的马拉松活动",
        "status": "PUBLISHED",
        "approvalStatus": "APPROVED",
        "maxParticipants": 500,
        "createdAt": "2026-05-07T10:30:00",
        "updatedAt": "2026-05-07T10:30:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 4.3 获取我发布的活动列表
- **接口路径**: `GET /api/v1/activities/my`
- **标签**: 活动管理
- **描述**: 获取当前用户发布的所有活动列表
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "title": "校园马拉松比赛",
            "publisherId": 1,
            "publisherName": "张三",
            "startTime": "2026-05-20T09:00:00",
            "endTime": "2026-05-20T17:00:00",
            "location": "学校操场",
            "description": "全校师生参与的马拉松活动",
            "status": "PUBLISHED",
            "approvalStatus": "APPROVED",
            "maxParticipants": 500,
            "createdAt": "2026-05-07T10:30:00",
            "updatedAt": "2026-05-07T10:30:00"
        }
    ],
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 4.4 编辑活动
- **接口路径**: `PUT /api/v1/activities/{id}`
- **标签**: 活动管理
- **描述**: 更新指定活动的信息（仅活动发布者可操作）
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 活动ID |

**请求参数**:
```json
{
    "title": "校园马拉松比赛（第二届）",
    "startTime": "2026-05-25T09:00:00",
    "endTime": "2026-05-25T17:00:00",
    "location": "学校操场及周边道路",
    "description": "全校师生参与的马拉松活动",
    "maxParticipants": 600
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "活动更新成功",
    "data": {
        "id": 1,
        "title": "校园马拉松比赛（第二届）",
        "publisherId": 1,
        "publisherName": "张三",
        "startTime": "2026-05-25T09:00:00",
        "endTime": "2026-05-25T17:00:00",
        "location": "学校操场及周边道路",
        "description": "全校师生参与的马拉松活动",
        "status": "PUBLISHED",
        "approvalStatus": "APPROVED",
        "maxParticipants": 600,
        "createdAt": "2026-05-07T10:30:00",
        "updatedAt": "2026-05-07T11:00:00"
    },
    "timestamp": "2026-05-07T11:00:00"
}
```

---

### 4.5 删除活动
- **接口路径**: `DELETE /api/v1/activities/{id}`
- **标签**: 活动管理
- **描述**: 删除指定活动（仅活动发布者可操作）
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "活动删除成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 4.6 获取活动列表（带筛选和分页）
- **接口路径**: `GET /api/v1/activities/list`
- **标签**: 活动管理
- **描述**: 获取活动列表，支持筛选和分页
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| status | String | 否 | 活动状态 |
| startDate | String | 否 | 开始日期 (yyyy-MM-dd) |
| endDate | String | 否 | 结束日期 (yyyy-MM-dd) |
| location | String | 否 | 活动地点 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "title": "校园马拉松比赛",
                "publisherId": 1,
                "publisherName": "张三",
                "startTime": "2026-05-20T09:00:00",
                "endTime": "2026-05-20T17:00:00",
                "location": "学校操场",
                "description": "全校师生参与的马拉松活动",
                "status": "PUBLISHED",
                "approvalStatus": "APPROVED",
                "maxParticipants": 500,
                "createdAt": "2026-05-07T10:30:00",
                "updatedAt": "2026-05-07T10:30:00"
            }
        ],
        "total": 100,
        "page": 1,
        "size": 10
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 五、活动收藏模块 (ActivityCollect)

### 5.1 收藏活动
- **接口路径**: `POST /api/v1/activity-collect/{activityId}`
- **标签**: 活动收藏管理
- **描述**: 用户收藏指定的活动
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "收藏成功",
    "data": {
        "activityId": 1,
        "collected": true
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 5.2 取消收藏
- **接口路径**: `DELETE /api/v1/activity-collect/{activityId}`
- **标签**: 活动收藏管理
- **描述**: 取消对指定活动的收藏
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "取消收藏成功",
    "data": {
        "activityId": 1,
        "collected": false
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 5.3 获取我的收藏列表
- **接口路径**: `GET /api/v1/activity-collect/my`
- **标签**: 活动收藏管理
- **描述**: 获取当前用户收藏的所有活动列表
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "userId": 1,
            "activityId": 1,
            "createdAt": "2026-05-07T10:30:00"
        }
    ],
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 5.4 检查是否已收藏
- **接口路径**: `GET /api/v1/activity-collect/{activityId}/status`
- **标签**: 活动收藏管理
- **描述**: 检查当前用户是否已收藏指定活动，并返回收藏数
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "collected": true,
        "collectCount": 50
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 六、活动评论模块 (Comment)

### 6.1 发布评论
- **接口路径**: `POST /api/v1/activities/{activityId}/comments`
- **标签**: 评论管理
- **描述**: 对指定活动发布评论
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**请求参数**:
```json
{
    "content": "这个活动太棒了！"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "评论发布成功",
    "data": {
        "id": 1,
        "activityId": 1,
        "userId": 1,
        "username": "张三",
        "content": "这个活动太棒了！",
        "createdAt": "2026-05-07T10:30:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 6.2 获取评论列表
- **接口路径**: `GET /api/v1/activities/{activityId}/comments`
- **标签**: 评论管理
- **描述**: 获取指定活动的评论列表
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "activityId": 1,
            "userId": 1,
            "username": "张三",
            "content": "这个活动太棒了！",
            "createdAt": "2026-05-07T10:30:00"
        }
    ],
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 6.3 删除评论
- **接口路径**: `DELETE /api/v1/comments/{commentId}`
- **标签**: 评论管理
- **描述**: 删除指定评论（仅评论发布者可操作）
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | Long | 是 | 评论ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "评论删除成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 七、活动报名模块 (Registration)

### 7.1 报名活动
- **接口路径**: `POST /api/v1/registrations`
- **标签**: 活动报名
- **描述**: 用户报名参加指定活动
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
    "activityId": 1
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "报名成功",
    "data": {
        "id": 1,
        "activityId": 1,
        "userId": 1,
        "username": "张三",
        "status": "PENDING",
        "registeredAt": "2026-05-07T10:30:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 7.2 获取我的报名记录
- **接口路径**: `GET /api/v1/registrations/my`
- **标签**: 活动报名
- **描述**: 获取当前用户的报名记录列表
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "activityId": 1,
                "userId": 1,
                "username": "张三",
                "activityTitle": "校园马拉松比赛",
                "status": "APPROVED",
                "registeredAt": "2026-05-07T10:30:00"
            }
        ],
        "total": 5,
        "page": 1,
        "size": 10
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 7.3 获取活动的报名人员列表
- **接口路径**: `GET /api/v1/registrations/activity/{activityId}`
- **标签**: 活动报名
- **描述**: 获取指定活动的报名人员列表（仅活动发布者可查看）
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "activityId": 1,
                "userId": 1,
                "username": "张三",
                "status": "APPROVED",
                "registeredAt": "2026-05-07T10:30:00"
            }
        ],
        "total": 50,
        "page": 1,
        "size": 10
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 7.4 更新报名状态
- **接口路径**: `PUT /api/v1/registrations/status`
- **标签**: 活动报名
- **描述**: 活动发布者更新报名者的状态（审批通过/拒绝）
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**请求参数**:
```json
{
    "registrationId": 1,
    "status": "APPROVED"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| registrationId | Long | 是 | 报名记录ID |
| status | String | 是 | 状态：APPROVED（通过）/ REJECTED（拒绝）/ CANCELLED（取消） |

**响应示例**:
```json
{
    "code": 200,
    "message": "状态更新成功",
    "data": {
        "id": 1,
        "activityId": 1,
        "userId": 1,
        "username": "张三",
        "status": "APPROVED",
        "registeredAt": "2026-05-07T10:30:00"
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 7.5 取消报名
- **接口路径**: `DELETE /api/v1/registrations/activity/{activityId}`
- **标签**: 活动报名
- **描述**: 当前用户取消对指定活动的报名
- **是否需要认证**: 是

**请求头**:
```
Authorization: Bearer <token>
```

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "取消报名成功",
    "data": null,
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 八、智能搜索模块 (Search)

### 8.1 获取搜索建议和热门搜索
- **接口路径**: `GET /api/v1/search/suggestions`
- **标签**: 智能搜索
- **描述**: 获取搜索建议词和热门搜索词
- **是否需要认证**: 否

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| prefix | String | 否 | 搜索前缀，用于自动补全 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "suggestions": ["篮球赛", "篮球比赛", "篮球队"],
        "hotSearches": ["马拉松", "篮球赛", "音乐会", "志愿者"]
    },
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 8.2 搜索自动补全
- **接口路径**: `GET /api/v1/search/autocomplete`
- **标签**: 智能搜索
- **描述**: 根据前缀返回搜索建议词列表
- **是否需要认证**: 否

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| prefix | String | 否 | 搜索前缀 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": ["篮球赛", "篮球比赛", "篮球队招募"],
    "timestamp": "2026-05-07T10:30:00"
}
```

---

### 8.3 获取热门搜索
- **接口路径**: `GET /api/v1/search/hot`
- **标签**: 智能搜索
- **描述**: 获取当前热门搜索词列表
- **是否需要认证**: 否

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": ["马拉松", "篮球赛", "音乐会", "志愿者", "读书会"],
    "timestamp": "2026-05-07T10:30:00"
}
```

---

## 九、审计日志管理模块 (AuditLog)

> 所有接口需要管理员权限

### 9.1 获取审计日志列表（分页）
- **接口路径**: `GET /api/admin/audit-logs`
- **标签**: 审计日志管理
- **描述**: 管理员查询审计日志列表，支持多条件筛选和分页
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 否 | 用户ID |
| operation | String | 否 | 操作类型 |
| resourceType | String | 否 | 资源类型 |
| resourceId | Long | 否 | 资源ID |
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd HH:mm:ss） |
| responseStatus | Integer | 否 | 响应状态码 |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "userId": 1,
                "username": "admin",
                "operation": "LOGIN",
                "resourceType": "user",
                "resourceId": 1,
                "ipAddress": "192.168.1.1",
                "responseStatus": 200,
                "createdAt": "2026-05-07T10:30:00"
            }
        ],
        "total": 100,
        "page": 1,
        "size": 20,
        "totalPages": 5
    }
}
```

---

### 9.2 获取审计日志详情
- **接口路径**: `GET /api/admin/audit-logs/{id}`
- **标签**: 审计日志管理
- **描述**: 根据ID获取审计日志详情
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 审计日志ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "operation": "LOGIN",
        "resourceType": "user",
        "resourceId": 1,
        "ipAddress": "192.168.1.1",
        "responseStatus": 200,
        "createdAt": "2026-05-07T10:30:00"
    }
}
```

---

### 9.3 获取用户审计日志列表
- **接口路径**: `GET /api/admin/audit-logs/user/{userId}`
- **标签**: 审计日志管理
- **描述**: 查询指定用户的审计日志列表
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20 |

---

### 9.4 获取审计日志统计
- **接口路径**: `GET /api/admin/audit-logs/statistics`
- **标签**: 审计日志管理
- **描述**: 获取审计日志统计数据（按操作类型、资源类型、响应状态、日期分组）
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd HH:mm:ss） |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "operationStats": {"LOGIN": 50, "CREATE_ACTIVITY": 20},
        "resourceTypeStats": {"user": 50, "activity": 30},
        "responseStatusStats": {"200": 90, "401": 5},
        "dailyStats": {"2026-05-07": 15},
        "totalCount": 100
    }
}
```

---

### 9.5 获取最近的审计日志
- **接口路径**: `GET /api/admin/audit-logs/recent`
- **标签**: 审计日志管理
- **描述**: 获取最近的审计日志记录
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 返回数量限制，默认10，最大100 |

---

## 十、缓存管理模块 (Cache)

> 所有接口需要管理员权限

### 10.1 获取缓存统计信息
- **接口路径**: `GET /api/admin/cache/stats`
- **标签**: 缓存管理
- **描述**: 获取所有缓存的统计信息
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "获取缓存统计信息成功",
    "data": {
        "hotActivity": {"hitRate": 0.85, "size": 50},
        "userInfo": {"hitRate": 0.92, "size": 200}
    }
}
```

---

### 10.2 获取指定缓存统计信息
- **接口路径**: `GET /api/admin/cache/stats/{cacheName}`
- **标签**: 缓存管理
- **描述**: 获取指定缓存的统计信息
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cacheName | String | 是 | 缓存名称 |

---

### 10.3 获取所有缓存名称
- **接口路径**: `GET /api/admin/cache/names`
- **标签**: 缓存管理
- **描述**: 获取系统中所有缓存的名称列表
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "获取缓存名称成功",
    "data": ["hotActivity", "userInfo", "searchSuggestion"]
}
```

---

### 10.4 清空所有缓存
- **接口路径**: `POST /api/admin/cache/clear`
- **标签**: 缓存管理
- **描述**: 清空系统中所有缓存
- **是否需要认证**: 是（管理员）

---

### 10.5 清空指定缓存
- **接口路径**: `POST /api/admin/cache/clear/{cacheName}`
- **标签**: 缓存管理
- **描述**: 清空指定名称的缓存
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cacheName | String | 是 | 缓存名称 |

---

### 10.6 清空热门活动缓存
- **接口路径**: `POST /api/admin/cache/clear/hotActivity`
- **标签**: 缓存管理
- **描述**: 清空热门活动缓存
- **是否需要认证**: 是（管理员）

---

### 10.7 清空用户信息缓存
- **接口路径**: `POST /api/admin/cache/clear/userInfo`
- **标签**: 缓存管理
- **描述**: 清空用户信息缓存
- **是否需要认证**: 是（管理员）

---

### 10.8 清空搜索建议缓存
- **接口路径**: `POST /api/admin/cache/clear/searchSuggestion`
- **标签**: 缓存管理
- **描述**: 清空搜索建议缓存
- **是否需要认证**: 是（管理员）

---

## 十一、限流管理模块 (RateLimit)

> 所有接口需要管理员权限

### 11.1 获取限流配置信息
- **接口路径**: `GET /api/admin/rate-limit/config`
- **标签**: 管理员-限流管理
- **描述**: 获取当前限流配置信息
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "enabled": true,
        "ipLimit": 100,
        "userLimit": 50,
        "loginIpLimit": 5
    }
}
```

---

### 11.2 获取限流统计信息
- **接口路径**: `GET /api/admin/rate-limit/stats`
- **标签**: 管理员-限流管理
- **描述**: 获取当前限流统计数据
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "ipRateLimit": {},
        "userRateLimit": {},
        "loginRateLimit": {}
    }
}
```

---

### 11.3 清空限流缓存
- **接口路径**: `POST /api/admin/rate-limit/clear`
- **标签**: 管理员-限流管理
- **描述**: 清空所有限流缓存，重置限流计数器
- **是否需要认证**: 是（管理员）

---

### 11.4 更新限流配置
- **接口路径**: `POST /api/admin/rate-limit/update`
- **标签**: 管理员-限流管理
- **描述**: 动态更新限流配置参数
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| enabled | Boolean | 否 | 是否启用限流 |
| ipLimit | Integer | 否 | IP限流阈值（每分钟请求数） |
| userLimit | Integer | 否 | 用户限流阈值（每分钟请求数） |
| loginIpLimit | Integer | 否 | 登录限流阈值（每分钟请求数） |

---

## 十二、JWT密钥管理模块 (JwtKey)

> 所有接口需要管理员权限

### 12.1 获取当前密钥信息
- **接口路径**: `GET /api/admin/jwt-key/current`
- **标签**: JWT密钥管理
- **描述**: 获取当前激活的JWT密钥信息
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "version": 3,
        "createdAt": "2026-05-01T10:00:00",
        "isActive": true,
        "expireAt": "2026-06-01T10:00:00",
        "keyLength": 256,
        "status": "激活"
    }
}
```

---

### 12.2 获取密钥历史
- **接口路径**: `GET /api/admin/jwt-key/history`
- **标签**: JWT密钥管理
- **描述**: 获取所有密钥的历史记录
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "total": 3,
        "keys": [],
        "currentVersion": 3,
        "needsRotation": false
    }
}
```

---

### 12.3 手动轮换密钥
- **接口路径**: `POST /api/admin/jwt-key/rotate`
- **标签**: JWT密钥管理
- **描述**: 手动触发JWT密钥轮换，生成新密钥并停用旧密钥
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "success": true,
        "newVersion": 4,
        "oldVersion": 3,
        "message": "密钥轮换成功",
        "rotationTime": "2026-05-07T10:30:00"
    }
}
```

---

### 12.4 生成新密钥
- **接口路径**: `POST /api/admin/jwt-key/generate`
- **标签**: JWT密钥管理
- **描述**: 生成新的JWT密钥（不轮换，仅生成）
- **是否需要认证**: 是（管理员）

---

### 12.5 清理过期密钥
- **接口路径**: `POST /api/admin/jwt-key/clean`
- **标签**: JWT密钥管理
- **描述**: 清理已过期的JWT密钥记录
- **是否需要认证**: 是（管理员）

---

### 12.6 获取密钥管理状态
- **接口路径**: `GET /api/admin/jwt-key/status`
- **标签**: JWT密钥管理
- **描述**: 获取JWT密钥管理功能的当前状态
- **是否需要认证**: 是（管理员）

---

## 十三、权限管理模块 (Permission)

> 所有接口需要管理员权限

### 13.1 获取所有权限列表
- **接口路径**: `GET /api/admin/permissions`
- **标签**: 管理员-权限管理
- **描述**: 获取系统中所有可用的权限列表
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "code": "activity:create",
            "name": "创建活动",
            "description": "允许创建新的校园活动"
        }
    ]
}
```

---

### 13.2 获取所有角色列表
- **接口路径**: `GET /api/admin/permissions/roles`
- **标签**: 管理员-权限管理
- **描述**: 获取系统中所有角色列表
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {"role": "admin", "description": "系统管理员"},
        {"role": "user", "description": "普通用户"}
    ]
}
```

---

### 13.3 获取角色权限
- **接口路径**: `GET /api/admin/permissions/role/{role}`
- **标签**: 管理员-权限管理
- **描述**: 获取指定角色的权限列表
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| role | String | 是 | 角色名称 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "role": "admin",
        "roleDescription": "系统管理员",
        "permissionCodes": ["activity:create", "activity:delete", "user:manage"],
        "permissions": [
            {"code": "activity:create", "name": "创建活动", "description": "允许创建新的校园活动"}
        ]
    }
}
```

---

### 13.4 更新角色权限
- **接口路径**: `PUT /api/admin/permissions/role/{role}`
- **标签**: 管理员-权限管理
- **描述**: 为角色添加动态权限
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "permissionCodes": ["activity:create", "activity:delete"]
}
```

---

### 13.5 移除角色动态权限
- **接口路径**: `DELETE /api/admin/permissions/role/{role}/dynamic`
- **标签**: 管理员-权限管理
- **描述**: 移除角色的动态权限
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "permissionCodes": ["activity:create"]
}
```

---

### 13.6 重置角色权限
- **接口路径**: `DELETE /api/admin/permissions/role/{role}/reset`
- **标签**: 管理员-权限管理
- **描述**: 重置角色权限，清除所有动态权限
- **是否需要认证**: 是（管理员）

---

### 13.7 获取用户权限
- **接口路径**: `GET /api/admin/permissions/user/{userId}`
- **标签**: 管理员-权限管理
- **描述**: 获取指定用户的权限信息
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "userId": 1,
        "username": "zhangsan",
        "role": "user",
        "roleDescription": "普通用户",
        "permissionCodes": ["activity:view", "activity:register"],
        "permissions": []
    }
}
```

---

### 13.8 验证用户权限
- **接口路径**: `GET /api/admin/permissions/user/{userId}/check/{permissionCode}`
- **标签**: 管理员-权限管理
- **描述**: 验证指定用户是否拥有某权限
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| permissionCode | String | 是 | 权限码 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "userId": 1,
        "username": "zhangsan",
        "role": "user",
        "permissionCode": "activity:create",
        "hasPermission": false
    }
}
```

---

### 13.9 获取所有角色权限映射
- **接口路径**: `GET /api/admin/permissions/all-role-permissions`
- **标签**: 管理员-权限管理
- **描述**: 获取所有角色的权限映射关系
- **是否需要认证**: 是（管理员）

---

## 十四、文件上传模块 (FileUpload)

### 14.1 上传文件
- **接口路径**: `POST /api/v1/files/upload`
- **标签**: 文件上传管理
- **描述**: 上传文件到服务器
- **是否需要认证**: 是
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 上传的文件（最大10MB） |

**响应示例**:
```json
{
    "code": 200,
    "message": "文件上传成功",
    "data": {
        "fileId": 1,
        "originalName": "photo.jpg",
        "storedName": "uuid_photo.jpg",
        "filePath": "/uploads/2026/05/07/uuid_photo.jpg",
        "fileSize": 1024000,
        "fileType": "jpg",
        "mimeType": "image/jpeg",
        "uploadTime": "2026-05-07T10:30:00",
        "downloadUrl": "/api/v1/files/download/1"
    }
}
```

---

### 14.2 删除文件
- **接口路径**: `DELETE /api/v1/files/{fileId}`
- **标签**: 文件上传管理
- **描述**: 删除指定文件（仅上传者可操作）
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | Long | 是 | 文件ID |

---

### 14.3 获取文件信息
- **接口路径**: `GET /api/v1/files/{fileId}`
- **标签**: 文件上传管理
- **描述**: 根据文件ID获取文件信息
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | Long | 是 | 文件ID |

---

### 14.4 下载文件
- **接口路径**: `GET /api/v1/files/download/{fileId}`
- **标签**: 文件上传管理
- **描述**: 下载指定文件
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileId | Long | 是 | 文件ID |

---

### 14.5 获取我上传的文件列表
- **接口路径**: `GET /api/v1/files/my`
- **标签**: 文件上传管理
- **描述**: 获取当前用户上传的所有文件列表
- **是否需要认证**: 是

---

## 十五、用户头像模块 (Avatar)

### 15.1 上传用户头像
- **接口路径**: `POST /api/v1/users/avatar`
- **标签**: 用户头像管理
- **描述**: 上传或更新用户头像（仅允许jpg、jpeg、png、gif格式）
- **是否需要认证**: 是
- **Content-Type**: `multipart/form-data`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 头像文件 |

**响应示例**:
```json
{
    "code": 200,
    "message": "头像上传成功",
    "data": {
        "fileId": 1,
        "avatarUrl": "/api/v1/files/download/1",
        "originalName": "avatar.jpg",
        "fileSize": 51200
    }
}
```

---

### 15.2 获取用户头像
- **接口路径**: `GET /api/v1/users/{userId}/avatar`
- **标签**: 用户头像管理
- **描述**: 获取指定用户的头像URL
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "userId": 1,
        "avatarUrl": "/api/v1/files/download/1"
    }
}
```

---

### 15.3 删除用户头像
- **接口路径**: `DELETE /api/v1/users/avatar`
- **标签**: 用户头像管理
- **描述**: 删除当前用户的头像
- **是否需要认证**: 是

---

### 15.4 验证头像URL
- **接口路径**: `GET /api/v1/users/{userId}/avatar/validate`
- **标签**: 用户头像管理
- **描述**: 验证用户头像URL是否有效
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

---

## 十六、活动图片模块 (ActivityImage)

### 16.1 上传活动图片（多图）
- **接口路径**: `POST /api/v1/activities/{activityId}/images`
- **标签**: 活动图片管理
- **描述**: 批量上传活动图片
- **是否需要认证**: 是
- **Content-Type**: `multipart/form-data`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| files | MultipartFile[] | 是 | 图片文件数组 |

**响应示例**:
```json
{
    "code": 200,
    "message": "活动图片上传成功",
    "data": [
        {
            "imageId": 1,
            "imageUrl": "/api/v1/files/download/1",
            "displayOrder": 1
        }
    ]
}
```

---

### 16.2 上传单张活动图片
- **接口路径**: `POST /api/v1/activities/{activityId}/images/single`
- **标签**: 活动图片管理
- **描述**: 上传单张活动图片
- **是否需要认证**: 是
- **Content-Type**: `multipart/form-data`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 图片文件 |

---

### 16.3 获取活动图片列表
- **接口路径**: `GET /api/v1/activities/{activityId}/images`
- **标签**: 活动图片管理
- **描述**: 获取指定活动的图片列表
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

---

### 16.4 删除活动图片
- **接口路径**: `DELETE /api/v1/activities/{activityId}/images/{imageId}`
- **标签**: 活动图片管理
- **描述**: 删除指定活动图片（仅活动发布者可操作）
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |
| imageId | Long | 是 | 图片ID |

---

### 16.5 更新图片显示顺序
- **接口路径**: `PUT /api/v1/activities/{activityId}/images/{imageId}/order`
- **标签**: 活动图片管理
- **描述**: 更新活动图片的显示顺序
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |
| imageId | Long | 是 | 图片ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| displayOrder | Integer | 是 | 显示顺序 |

---

## 十七、活动分享模块 (ActivityShare)

### 17.1 分享活动
- **接口路径**: `POST /api/v1/activities/{activityId}/share`
- **标签**: 活动分享管理
- **描述**: 分享指定活动
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| shareChannel | String | 否 | 分享渠道（wechat、qq、weibo、link、other） |

**响应示例**:
```json
{
    "code": 200,
    "message": "活动分享成功",
    "data": {
        "shareId": 1,
        "activityId": 1,
        "shareChannel": "wechat",
        "shareTime": "2026-05-07T10:30:00"
    }
}
```

---

### 17.2 获取活动分享次数统计
- **接口路径**: `GET /api/v1/activities/{activityId}/share-count`
- **标签**: 活动分享管理
- **描述**: 获取指定活动的分享次数统计
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "activityId": 1,
        "totalShares": 50,
        "channelStats": {"wechat": 20, "qq": 15, "weibo": 10, "link": 5}
    }
}
```

---

### 17.3 获取活动分享历史记录
- **接口路径**: `GET /api/v1/activities/{activityId}/share-history`
- **标签**: 活动分享管理
- **描述**: 获取指定活动的分享历史记录
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 限制数量 |

---

### 17.4 获取我分享的活动列表
- **接口路径**: `GET /api/v1/activities/my/shared`
- **标签**: 活动分享管理
- **描述**: 获取当前用户分享过的活动列表
- **是否需要认证**: 是

---

## 十八、活动状态管理模块 (ActivityStatus)

### 18.1 发布活动
- **接口路径**: `POST /api/v1/activities/{activityId}/publish`
- **标签**: 活动状态管理
- **描述**: 将活动从草稿状态转为已发布状态
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "活动发布成功",
    "data": {
        "activityId": 1,
        "status": "published",
        "updatedAt": "2026-05-07T10:30:00"
    }
}
```

---

### 18.2 取消活动
- **接口路径**: `POST /api/v1/activities/{activityId}/cancel`
- **标签**: 活动状态管理
- **描述**: 取消指定活动
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 否 | 取消原因 |

---

### 18.3 结束活动
- **接口路径**: `POST /api/v1/activities/{activityId}/end`
- **标签**: 活动状态管理
- **描述**: 手动结束指定活动
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | 否 | 结束原因 |

---

### 18.4 更新活动状态
- **接口路径**: `PUT /api/v1/activities/{activityId}/status`
- **标签**: 活动状态管理
- **描述**: 通用活动状态更新接口
- **是否需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| newStatus | String | 是 | 新状态（draft、published、cancelled、ended） |
| reason | String | 否 | 变更原因 |

---

### 18.5 获取活动状态信息
- **接口路径**: `GET /api/v1/activities/{activityId}/status`
- **标签**: 活动状态管理
- **描述**: 获取指定活动的状态信息
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| activityId | Long | 是 | 活动ID |

---

## 十九、搜索历史模块 (SearchHistory)

> 搜索历史功能集成在搜索服务中，通过 SearchHistoryService 提供以下能力：

### 19.1 记录搜索历史
- **描述**: 用户执行搜索时自动异步记录搜索历史，不影响搜索性能
- **触发方式**: 搜索接口自动调用

---

### 19.2 获取用户搜索历史
- **描述**: 获取指定用户的搜索历史记录
- **调用方式**: 通过 SearchHistoryService.getUserSearchHistory(userId, limit)

---

### 19.3 获取用户最近搜索关键词
- **描述**: 获取用户最近搜索的关键词列表（去重）
- **调用方式**: 通过 SearchHistoryService.getUserRecentKeywords(userId, limit)

---

### 19.4 清理搜索历史
- **描述**: 清理指定用户指定时间之前的搜索历史
- **调用方式**: 通过 SearchHistoryService.clearUserSearchHistory(userId, days)

---

### 19.5 获取热门搜索关键词
- **描述**: 获取最近一段时间的热门搜索关键词（带缓存）
- **调用方式**: 通过 SearchHistoryService.getHotKeywords(limit, days)

---

## 二十、WebSocket接口模块 (WebSocket)

### 20.1 WebSocket连接
- **连接地址**: `ws://localhost:8080/ws/websocket`
- **描述**: 建立WebSocket连接，用于接收实时消息推送
- **认证方式**: 连接时在URL参数中携带token：`?token=<jwt_token>`

---

### 20.2 向单个用户推送消息
- **接口路径**: `POST /api/websocket/push/{userId}`
- **描述**: 向指定用户推送消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "type": "notification",
    "data": {"message": "您有新的通知"}
}
```

**响应示例**:
```json
{
    "userId": 1,
    "success": true,
    "message": "推送成功"
}
```

---

### 20.3 批量推送消息
- **接口路径**: `POST /api/websocket/push/batch`
- **描述**: 向多个用户批量推送消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "userIds": [1, 2, 3],
    "type": "notification",
    "data": {"message": "系统维护通知"}
}
```

**响应示例**:
```json
{
    "totalCount": 3,
    "successCount": 2,
    "message": "批量推送完成"
}
```

---

### 20.4 广播消息
- **接口路径**: `POST /api/websocket/broadcast`
- **描述**: 向所有在线用户广播消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "type": "system",
    "data": {"message": "系统公告"}
}
```

---

### 20.5 推送通知消息
- **接口路径**: `POST /api/websocket/notification/{userId}`
- **描述**: 向指定用户推送通知消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "notificationId": 1,
    "title": "活动提醒",
    "content": "您报名的活动即将开始"
}
```

---

### 20.6 推送提醒消息
- **接口路径**: `POST /api/websocket/reminder/{userId}`
- **描述**: 向指定用户推送提醒消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "title": "活动提醒",
    "content": "您报名的活动即将开始"
}
```

---

### 20.7 推送系统消息
- **接口路径**: `POST /api/websocket/system/{userId}`
- **描述**: 向指定用户推送系统消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "content": "系统维护通知"
}
```

---

### 20.8 广播系统消息
- **接口路径**: `POST /api/websocket/system/broadcast`
- **描述**: 向所有在线用户广播系统消息
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "content": "系统将于今晚22:00进行维护"
}
```

---

### 20.9 获取在线用户数量
- **接口路径**: `GET /api/online-users/count`
- **描述**: 获取当前在线用户数量
- **是否需要认证**: 否

**响应示例**:
```json
{
    "count": 50
}
```

---

### 20.10 获取在线用户ID列表
- **接口路径**: `GET /api/online-users/ids`
- **描述**: 获取所有在线用户ID列表
- **是否需要认证**: 是（管理员）

---

### 20.11 检查用户是否在线
- **接口路径**: `GET /api/online-users/check/{userId}`
- **描述**: 检查指定用户是否在线
- **是否需要认证**: 否

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

---

### 20.12 获取在线用户详细信息
- **接口路径**: `GET /api/online-users/info/{userId}`
- **描述**: 获取指定在线用户的详细信息
- **是否需要认证**: 是（管理员）

---

### 20.13 获取所有在线用户列表
- **接口路径**: `GET /api/online-users/list`
- **描述**: 获取所有在线用户的详细信息列表
- **是否需要认证**: 是（管理员）

---

## 二十一、统计报表模块 (Statistics)

> 所有接口需要管理员权限

### 21.1 获取系统概览统计
- **接口路径**: `GET /api/admin/statistics/overview`
- **标签**: 管理员-数据统计
- **描述**: 获取系统整体统计数据（活动、用户、报名概览）
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "totalActivities": 150,
        "totalUsers": 500,
        "totalRegistrations": 1200,
        "todayNewUsers": 10,
        "todayNewActivities": 3
    }
}
```

---

### 21.2 获取活动统计
- **接口路径**: `GET /api/admin/statistics/activities`
- **标签**: 管理员-数据统计
- **描述**: 获取活动统计数据（状态分布、类型分布、趋势）
- **是否需要认证**: 是（管理员）

---

### 21.3 获取用户统计
- **接口路径**: `GET /api/admin/statistics/users`
- **标签**: 管理员-数据统计
- **描述**: 获取用户统计数据（角色分布、注册趋势、活跃用户）
- **是否需要认证**: 是（管理员）

---

### 21.4 获取报名统计
- **接口路径**: `GET /api/admin/statistics/registrations`
- **标签**: 管理员-数据统计
- **描述**: 获取报名统计数据（状态分布、趋势、热门活动）
- **是否需要认证**: 是（管理员）

---

### 21.5 获取趋势统计
- **接口路径**: `GET /api/admin/statistics/trend`
- **标签**: 管理员-数据统计
- **描述**: 获取按时间维度的趋势统计数据
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | String | 否 | 开始日期（默认最近12个月） |
| endDate | String | 否 | 结束日期 |
| timeUnit | String | 否 | 时间单位（month、week、day），默认month |

---

### 21.6 获取热门活动统计
- **接口路径**: `GET /api/admin/statistics/hot-activities`
- **标签**: 管理员-数据统计
- **描述**: 获取热门活动排行
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 返回数量限制，默认10 |
| sortBy | String | 否 | 排序方式（registration、collection、view），默认registration |

---

### 21.7 清除统计缓存
- **接口路径**: `POST /api/admin/statistics/clear-cache`
- **标签**: 管理员-数据统计
- **描述**: 清除统计数据缓存，强制刷新
- **是否需要认证**: 是（管理员）

---

## 二十二、定时任务管理模块 (ScheduledTask)

> 所有接口需要管理员权限

### 22.1 获取定时任务列表
- **接口路径**: `GET /api/admin/scheduled-tasks`
- **描述**: 获取系统中所有定时任务列表
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
[
    {
        "taskName": "activityReminderTask",
        "description": "活动开始前提醒",
        "cronExpression": "0 0 * * * ?",
        "status": "RUNNING",
        "lastExecutionTime": "2026-05-07T10:00:00"
    }
]
```

---

### 22.2 获取定时任务详情
- **接口路径**: `GET /api/admin/scheduled-tasks/{taskName}`
- **描述**: 根据任务名称获取定时任务详情
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskName | String | 是 | 任务名称 |

---

### 22.3 手动触发任务执行
- **接口路径**: `POST /api/admin/scheduled-tasks/{taskName}/execute`
- **描述**: 手动触发指定定时任务的执行
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskName | String | 是 | 任务名称 |

**响应示例**:
```json
{
    "taskName": "activityReminderTask",
    "success": true,
    "message": "任务执行完成"
}
```

---

### 22.4 暂停任务
- **接口路径**: `POST /api/admin/scheduled-tasks/{taskName}/pause`
- **描述**: 暂停指定定时任务
- **是否需要认证**: 是（管理员）

---

### 22.5 恢复任务
- **接口路径**: `POST /api/admin/scheduled-tasks/{taskName}/resume`
- **描述**: 恢复已暂停的定时任务
- **是否需要认证**: 是（管理员）

---

### 22.6 获取任务执行历史
- **接口路径**: `GET /api/admin/scheduled-tasks/{taskName}/history`
- **描述**: 获取指定任务的执行历史记录
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskName | String | 是 | 任务名称 |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 返回数量限制，默认10 |

---

## 二十三、登录锁定管理模块 (LoginLock)

> 所有接口需要管理员权限

### 23.1 获取锁定列表（分页）
- **接口路径**: `GET /api/admin/login-lock/list`
- **标签**: 管理员-登录锁定管理
- **描述**: 获取当前被锁定的账户列表
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "username": "testuser",
                "lockTime": "2026-05-07T10:00:00",
                "unlockTime": "2026-05-07T10:15:00",
                "failCount": 5,
                "lockReason": "连续登录失败",
                "isLocked": true,
                "createdAt": "2026-05-07T10:00:00"
            }
        ],
        "total": 10,
        "page": 1,
        "size": 10
    }
}
```

---

### 23.2 获取用户锁定信息
- **接口路径**: `GET /api/admin/login-lock/user/{username}`
- **标签**: 管理员-登录锁定管理
- **描述**: 获取指定用户的锁定信息
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |

---

### 23.3 解锁用户
- **接口路径**: `POST /api/admin/login-lock/unlock/{username}`
- **标签**: 管理员-登录锁定管理
- **描述**: 手动解锁被锁定的用户
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |

---

### 23.4 获取用户锁定历史
- **接口路径**: `GET /api/admin/login-lock/history/{username}`
- **标签**: 管理员-登录锁定管理
- **描述**: 获取指定用户的锁定历史记录
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

## 二十四、敏感词管理模块 (SensitiveWord)

> 所有接口需要管理员权限

### 24.1 添加敏感词
- **接口路径**: `POST /api/v1/admin/sensitive-words`
- **标签**: 管理员-敏感词管理
- **描述**: 添加新的敏感词
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "word": "违禁词",
    "category": "politics",
    "level": "high"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "敏感词添加成功",
    "data": {
        "id": 1,
        "word": "违禁词",
        "category": "politics",
        "level": "high",
        "createdAt": "2026-05-07T10:30:00"
    }
}
```

---

### 24.2 获取敏感词列表
- **接口路径**: `GET /api/v1/admin/sensitive-words`
- **标签**: 管理员-敏感词管理
- **描述**: 获取敏感词列表（支持分页和筛选）
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |
| category | String | 否 | 分类筛选 |
| level | String | 否 | 级别筛选 |

---

### 24.3 获取敏感词详情
- **接口路径**: `GET /api/v1/admin/sensitive-words/{id}`
- **标签**: 管理员-敏感词管理
- **描述**: 根据ID获取敏感词详情
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 敏感词ID |

---

### 24.4 更新敏感词
- **接口路径**: `PUT /api/v1/admin/sensitive-words/{id}`
- **标签**: 管理员-敏感词管理
- **描述**: 更新指定敏感词
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 敏感词ID |

**请求参数**:
```json
{
    "word": "更新后的词",
    "category": "other",
    "level": "medium"
}
```

---

### 24.5 删除敏感词
- **接口路径**: `DELETE /api/v1/admin/sensitive-words/{id}`
- **标签**: 管理员-敏感词管理
- **描述**: 删除指定敏感词
- **是否需要认证**: 是（管理员）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 敏感词ID |

---

### 24.6 批量添加敏感词
- **接口路径**: `POST /api/v1/admin/sensitive-words/batch`
- **标签**: 管理员-敏感词管理
- **描述**: 批量添加敏感词
- **是否需要认证**: 是（管理员）

**请求参数**:
```json
{
    "words": ["词1", "词2", "词3"],
    "category": "politics",
    "level": "high"
}
```

**响应示例**:
```json
{
    "code": 200,
    "message": "批量添加敏感词成功，共添加 3 个",
    "data": 3
}
```

---

### 24.7 重新加载敏感词库
- **接口路径**: `POST /api/v1/admin/sensitive-words/reload`
- **标签**: 管理员-敏感词管理
- **描述**: 重新加载敏感词库到内存
- **是否需要认证**: 是（管理员）

---

### 24.8 获取敏感词库统计信息
- **接口路径**: `GET /api/v1/admin/sensitive-words/statistics`
- **标签**: 管理员-敏感词管理
- **描述**: 获取敏感词库的统计信息
- **是否需要认证**: 是（管理员）

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "treeSize": 500,
        "whitelistSize": 20
    }
}
```

---

### 24.9 检查文本是否包含敏感词
- **接口路径**: `GET /api/v1/admin/sensitive-words/check`
- **标签**: 管理员-敏感词管理
- **描述**: 检查指定文本是否包含敏感词（用于测试）
- **是否需要认证**: 是（管理员）

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| text | String | 是 | 待检查的文本 |

**响应示例**:
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "containsSensitiveWord": true,
        "sensitiveWords": ["违禁词1"],
        "filteredText": "***文本内容***"
    }
}
```

---

## 二十五、数据模型

### 25.1 用户 (User)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| password | String | 密码（加密存储） |
| realName | String | 真实姓名 |
| contact | String | 联系方式 |
| role | String | 角色：USER / ADMIN |
| avatarUrl | String | 头像URL |
| createdAt | LocalDateTime | 创建时间 |

### 25.2 活动 (Activity)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 活动ID |
| title | String | 活动标题 |
| publisherId | Long | 发布者ID |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| location | String | 活动地点 |
| description | String | 活动描述 |
| status | String | 状态：PUBLISHED / DRAFT / CANCELLED / ENDED |
| approvalStatus | String | 审批状态：PENDING / APPROVED / REJECTED |
| maxParticipants | Integer | 最大参与人数 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 25.3 活动收藏 (ActivityCollect)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 收藏记录ID |
| userId | Long | 用户ID |
| activityId | Long | 活动ID |
| createdAt | LocalDateTime | 收藏时间 |

### 25.4 评论 (Comment)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 评论ID |
| activityId | Long | 活动ID |
| userId | Long | 用户ID |
| content | String | 评论内容 |
| createdAt | LocalDateTime | 评论时间 |

### 25.5 活动报名 (ActivityRegistration)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 报名记录ID |
| activityId | Long | 活动ID |
| userId | Long | 用户ID |
| status | String | 状态：PENDING / APPROVED / REJECTED / CANCELLED |
| registeredAt | LocalDateTime | 报名时间 |

### 25.6 审计日志 (AuditLog)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 日志ID |
| userId | Long | 操作用户ID |
| username | String | 操作用户名 |
| operation | String | 操作类型 |
| resourceType | String | 资源类型 |
| resourceId | Long | 资源ID |
| ipAddress | String | IP地址 |
| responseStatus | Integer | 响应状态码 |
| createdAt | LocalDateTime | 创建时间 |

### 25.7 JWT密钥 (JwtKey)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键ID |
| keyValue | String | 密钥值（Base64编码） |
| version | Integer | 密钥版本 |
| createdAt | LocalDateTime | 创建时间 |
| isActive | Boolean | 是否激活 |
| expireAt | LocalDateTime | 过期时间 |

### 25.8 登录锁定 (LoginLock)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 锁定记录ID |
| username | String | 用户名 |
| lockTime | LocalDateTime | 锁定时间 |
| unlockTime | LocalDateTime | 解锁时间 |
| failCount | Integer | 失败次数 |
| lockReason | String | 锁定原因 |
| isLocked | Boolean | 是否锁定 |

### 25.9 搜索历史 (SearchHistory)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键ID |
| userId | Long | 用户ID |
| searchKeyword | String | 搜索关键词 |
| searchTime | LocalDateTime | 搜索时间 |
| resultCount | Integer | 搜索结果数量 |
| searchType | String | 搜索类型 |

### 25.10 敏感词 (SensitiveWord)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键ID |
| word | String | 敏感词 |
| category | String | 分类 |
| level | String | 级别 |
| createdAt | LocalDateTime | 创建时间 |

### 25.11 文件信息 (FileInfo)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 文件ID |
| originalName | String | 原始文件名 |
| storedName | String | 存储文件名 |
| filePath | String | 文件路径 |
| fileSize | Long | 文件大小 |
| fileType | String | 文件类型 |
| mimeType | String | MIME类型 |
| uploaderId | Long | 上传者ID |
| uploadTime | LocalDateTime | 上传时间 |

---

## 二十六、错误处理

### 26.1 通用错误响应格式
当接口调用失败时，响应体格式如下：

```json
{
    "code": 401,
    "message": "未授权或登录已过期",
    "data": null,
    "timestamp": "2026-05-07T10:30:00",
    "requestId": null
}
```

### 26.2 业务异常说明
| 异常类型 | HTTP状态码 | 错误码 | 说明 |
|----------|------------|--------|------|
| BusinessException | 400 | 4xxx | 业务逻辑错误 |
| MethodArgumentNotValidException | 422 | 422 | 参数校验失败 |
| UnauthorizedException | 401 | 401 | 未授权 |
| RateLimitExceededException | 429 | 429 | 请求过于频繁 |
| GlobalExceptionHandler | 500 | 500 | 服务器内部错误 |

---

## 二十七、版本历史

| 版本号 | 日期 | 说明 |
|--------|------|------|
| 1.0.0 | 2026-05-07 | 初始版本，包含所有基础接口 |
| 2.0.0 | 2026-06-07 | 新增审计日志、缓存管理、限流管理、JWT密钥管理、权限管理、文件上传、用户头像、活动图片、活动分享、活动状态管理、搜索历史、WebSocket、统计报表、定时任务、登录锁定、敏感词管理接口 |
