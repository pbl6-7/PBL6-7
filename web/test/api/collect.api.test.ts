/**
 * 收藏功能API集成测试
 */
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import {
  API_BASE_URL,
  TEST_USER,
  clearAuthToken,
  loginAndGetToken
} from './config'

describe('收藏功能API集成测试', () => {
  let activityId: number

  beforeAll(async () => {
    clearAuthToken()
    await loginAndGetToken(TEST_USER.username, TEST_USER.password)

    const listResponse = await fetch(`${API_BASE_URL}/activities/list?page=1&pageSize=1`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    const listResult = await listResponse.json()
    if (listResult.data.records && listResult.data.records.length > 0) {
      activityId = listResult.data.records[0].id
    }
  })

  afterAll(() => {
    clearAuthToken()
  })

  describe('POST /activity-collect/:activityId', () => {
    it('应该成功收藏活动', async () => {
      if (!activityId) {
        console.log('没有可用的活动ID，跳过收藏测试')
        return
      }

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activity-collect/${activityId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })

    it('应该处理重复收藏', async () => {
      if (!activityId) return

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activity-collect/${activityId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })

  describe('GET /activity-collect/:activityId/status', () => {
    it('应该获取收藏状态', async () => {
      if (!activityId) return

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activity-collect/${activityId}/status`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(typeof result.data).toBe('boolean')
    })
  })

  describe('GET /activity-collect/my', () => {
    it('应该获取我的收藏列表', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activity-collect/my?page=1&size=10`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(Array.isArray(result.data)).toBe(true)
    })
  })

  describe('DELETE /activity-collect/:activityId', () => {
    it('应该成功取消收藏', async () => {
      if (!activityId) return

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activity-collect/${activityId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })

    it('应该处理取消不存在的收藏', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activity-collect/999999`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })
})
