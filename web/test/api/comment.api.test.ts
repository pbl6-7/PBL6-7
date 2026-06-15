/**
 * 评论功能API集成测试
 */
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import {
  API_BASE_URL,
  TEST_USER,
  clearAuthToken,
  loginAndGetToken
} from './config'

describe('评论功能API集成测试', () => {
  let activityId: number
  let commentId: number

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

  describe('POST /activities/:activityId/comments', () => {
    it('应该成功添加评论', async () => {
      if (!activityId) {
        console.log('没有可用的活动ID，跳过评论测试')
        return
      }

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities/${activityId}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          content: `测试评论内容_${Date.now()}`
        })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      if (result.data) {
        commentId = result.data.id
      }
    })

    it('应该处理空评论内容', async () => {
      if (!activityId) return

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities/${activityId}/comments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ content: '' })
      })
      const result = await response.json()

      expect(result.code).not.toBe(200)
    })
  })

  describe('GET /activities/:activityId/comments', () => {
    it('应该获取活动评论列表', async () => {
      if (!activityId) return

      const response = await fetch(`${API_BASE_URL}/activities/${activityId}/comments?page=1&size=10`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('records')
      expect(Array.isArray(result.data.records)).toBe(true)
    })
  })

  describe('DELETE /activities/comments/:commentId', () => {
    it('应该成功删除评论', async () => {
      if (!commentId) {
        console.log('没有评论ID，跳过删除测试')
        return
      }

      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities/comments/${commentId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })

    it('应该处理删除不存在的评论', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/activities/comments/999999`, {
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
