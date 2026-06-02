/**
 * 活动API集成测试
 * 测试实际的后端API接口
 */
import { describe, it, expect, beforeAll, afterAll, beforeEach } from 'vitest'
import {
  API_BASE_URL,
  TEST_USER,
  apiGet,
  apiPost,
  apiPut,
  apiDelete,
  loginAndGetToken,
  clearAuthToken,
  getAuthHeaders
} from './config'

describe('活动API集成测试', () => {
  let activityId: number

  beforeAll(async () => {
    clearAuthToken()
    await loginAndGetToken(TEST_USER.username, TEST_USER.password)
  })

  afterAll(() => {
    clearAuthToken()
  })

  describe('GET /activities/list', () => {
    it('应该获取活动列表', async () => {
      const response = await fetch(`${API_BASE_URL}/activities/list?page=1&pageSize=10`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('records')
      expect(Array.isArray(result.data.records)).toBe(true)
    })

    it('应该支持分页参数', async () => {
      const response = await fetch(`${API_BASE_URL}/activities/list?page=1&pageSize=5`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data.records.length).toBeLessThanOrEqual(5)
    })

    it('应该支持状态筛选', async () => {
      const response = await fetch(`${API_BASE_URL}/activities/list?status=published`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })
  })

  describe('POST /activities', () => {
    it('应该成功发布活动', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          title: `测试活动_${Date.now()}`,
          description: '这是一条测试活动描述',
          location: '测试地点',
          startTime: '2026-06-15T10:00:00',
          endTime: '2026-06-15T12:00:00'
        })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('id')
      activityId = result.data.id
    })

    it('应该在未登录时拒绝发布', async () => {
      localStorage.removeItem('token')

      const response = await fetch(`${API_BASE_URL}/activities`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: '测试活动',
          description: '描述',
          location: '地点',
          startTime: '2026-06-15T10:00:00',
          endTime: '2026-06-15T12:00:00'
        })
      })

      expect(response.status).toBe(401)
    })
  })

  describe('GET /activities/:id', () => {
    it('应该获取活动详情', async () => {
      if (!activityId) {
        const listResponse = await fetch(`${API_BASE_URL}/activities/list?page=1&pageSize=1`, {
          method: 'GET',
          headers: { 'Content-Type': 'application/json' }
        })
        const listResult = await listResponse.json()
        if (listResult.data.records.length > 0) {
          activityId = listResult.data.records[0].id
        }
      }

      const response = await fetch(`${API_BASE_URL}/activities/${activityId}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('id')
      expect(result.data).toHaveProperty('title')
    })

    it('应该处理不存在的活动', async () => {
      const response = await fetch(`${API_BASE_URL}/activities/999999`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })

  describe('PUT /activities/:id', () => {
    it('应该成功更新活动', async () => {
      if (!activityId) return

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities/${activityId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          title: '更新后的标题',
          description: '更新后的描述'
        })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })
  })

  describe('DELETE /activities/:id', () => {
    it('应该成功删除活动', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities/${activityId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })
  })
})
