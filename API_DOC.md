# 校园活动平台 API 接口文档

## 一、概述

### 1.1 文档说明
本文档描述了校园活动平台的RESTful API接口设计，包括用户管理、活动管理、评论管理、收藏管理、报名管理、智能搜索等模块。

### 1.2 基础信息
- **基础URL**: `/api/v1`
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

## 九、数据模型

### 9.1 用户 (User)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| password | String | 密码（加密存储） |
| realName | String | 真实姓名 |
| contact | String | 联系方式 |
| role | String | 角色：USER / ADMIN |
| createdAt | LocalDateTime | 创建时间 |

### 9.2 活动 (Activity)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 活动ID |
| title | String | 活动标题 |
| publisherId | Long | 发布者ID |
| startTime | LocalDateTime | 开始时间 |
| endTime | LocalDateTime | 结束时间 |
| location | String | 活动地点 |
| description | String | 活动描述 |
| status | String | 状态：PUBLISHED / DRAFT / CANCELLED |
| approvalStatus | String | 审批状态：PENDING / APPROVED / REJECTED |
| maxParticipants | Integer | 最大参与人数 |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 9.3 活动收藏 (ActivityCollect)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 收藏记录ID |
| userId | Long | 用户ID |
| activityId | Long | 活动ID |
| createdAt | LocalDateTime | 收藏时间 |

### 9.4 评论 (Comment)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 评论ID |
| activityId | Long | 活动ID |
| userId | Long | 用户ID |
| content | String | 评论内容 |
| createdAt | LocalDateTime | 评论时间 |

### 9.5 活动报名 (ActivityRegistration)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 报名记录ID |
| activityId | Long | 活动ID |
| userId | Long | 用户ID |
| status | String | 状态：PENDING / APPROVED / REJECTED / CANCELLED |
| registeredAt | LocalDateTime | 报名时间 |

---

## 十、错误处理

### 10.1 通用错误响应格式
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

### 10.2 业务异常说明
| 异常类型 | HTTP状态码 | 错误码 | 说明 |
|----------|------------|--------|------|
| BusinessException | 400 | 4xxx | 业务逻辑错误 |
| MethodArgumentNotValidException | 422 | 422 | 参数校验失败 |
| UnauthorizedException | 401 | 401 | 未授权 |
| GlobalExceptionHandler | 500 | 500 | 服务器内部错误 |

---

## 十一、版本历史

| 版本号 | 日期 | 说明 |
|--------|------|------|
| 1.0.0 | 2026-05-07 | 初始版本，包含所有基础接口 |
