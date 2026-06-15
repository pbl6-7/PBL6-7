/**
 * 活动报名API集成测试
 */
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import {
  API_BASE_URL,
  TEST_USER,
  clearAuthToken,
  loginAndGetToken
} from './config'

describe('活动报名API集成测试', () => {
  let activityId: number
  let registrationId: number

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

  describe('POST /registrations', () => {
    it('应该成功报名活动', async () => {
      if (!activityId) {
        console.log('没有可用的活动ID，跳过报名测试')
        return
      }

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/registrations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ activityId })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      if (result.data) {
        registrationId = result.data.id || result.data.registrationId
      }
    })

    it('应该处理重复报名', async () => {
      if (!activityId) return

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/registrations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ activityId })
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })

    it('应该处理不存在的活动', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/registrations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ activityId: 999999 })
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })

  describe('GET /registrations/my', () => {
    it('应该获取我的报名列表', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/registrations/my?page=1&size=10`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('records')
      expect(Array.isArray(result.data.records)).toBe(true)
    })
  })

  describe('DELETE /registrations/:id', () => {
    it('应该成功取消报名', async () => {
      if (!registrationId) {
        console.log('没有报名ID，跳过取消测试')
        return
      }

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/registrations/${registrationId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })

    it('应该处理取消不存在的报名', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/registrations/999999`, {
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
