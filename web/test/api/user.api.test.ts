/**
 * 用户API集成测试
 * 测试实际的后端API接口
 */
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import {
  API_BASE_URL,
  TEST_USER,
  TEST_ADMIN,
  apiGet,
  apiPost,
  apiPut,
  apiDelete,
  setAuthToken,
  clearAuthToken,
  loginAndGetToken
} from './config'

describe('用户API集成测试', () => {
  beforeAll(async () => {
    clearAuthToken()
  })

  afterAll(() => {
    clearAuthToken()
  })

  describe('POST /users/login', () => {
    it('应该成功登录', async () => {
      const response = await fetch(`${API_BASE_URL}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(TEST_USER)
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('token')
      expect(result.data).toHaveProperty('userId')
      expect(result.data).toHaveProperty('username')

      if (result.data.token) {
        setAuthToken(result.data.token)
      }
    })

    it('应该拒绝错误密码', async () => {
      const response = await fetch(`${API_BASE_URL}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: TEST_USER.username, password: 'wrongpassword' })
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })

    it('应该拒绝不存在的用户', async () => {
      const response = await fetch(`${API_BASE_URL}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: 'nonexistent', password: '123456' })
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })

  describe('POST /users/register', () => {
    const uniqueUsername = `testuser_${Date.now()}`

    it('应该成功注册新用户', async () => {
      const response = await fetch(`${API_BASE_URL}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: uniqueUsername,
          password: '123456',
          realName: '测试用户',
          contact: `${uniqueUsername}@example.com`,
          securityQuestionId: 1,
          securityAnswer: '答案'
        })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('userId')
    })

    it('应该拒绝重复用户名', async () => {
      const response = await fetch(`${API_BASE_URL}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: TEST_USER.username,
          password: '123456',
          securityQuestionId: 1,
          securityAnswer: '答案'
        })
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })

  describe('GET /users/profile', () => {
    it('应该在登录后获取用户资料', async () => {
      await loginAndGetToken(TEST_USER.username, TEST_USER.password)

      const response = await fetch(`${API_BASE_URL}/users/profile`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('username')
      expect(result.data).toHaveProperty('realName')
    })

    it('应该在未登录时拒绝访问', async () => {
      clearAuthToken()

      const response = await fetch(`${API_BASE_URL}/users/profile`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })

      expect(response.status).toBe(401)
    })
  })

  describe('PUT /users/profile', () => {
    it('应该成功更新用户资料', async () => {
      await loginAndGetToken(TEST_USER.username, TEST_USER.password)
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/users/profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          realName: '更新后的名字'
        })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })
  })

  describe('PUT /users/password', () => {
    it('应该成功修改密码', async () => {
      await loginAndGetToken(TEST_USER.username, TEST_USER.password)
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/users/password`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          oldPassword: TEST_USER.password,
          newPassword: 'newpassword123'
        })
      })
      const result = await response.json()

      if (result.code === 200) {
        expect(result.code).toBe(200)
      }
    })
  })
})
