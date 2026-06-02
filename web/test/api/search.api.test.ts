/**
 * 搜索功能API集成测试
 */
import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import {
  API_BASE_URL,
  TEST_USER,
  clearAuthToken,
  loginAndGetToken
} from './config'

describe('搜索功能API集成测试', () => {
  beforeAll(async () => {
    clearAuthToken()
    await loginAndGetToken(TEST_USER.username, TEST_USER.password)
  })

  afterAll(() => {
    clearAuthToken()
  })

  describe('GET /search/suggestions', () => {
    it('应该获取搜索建议', async () => {
      const response = await fetch(`${API_BASE_URL}/search/suggestions?prefix=校园`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(result.data).toHaveProperty('suggestions')
      expect(Array.isArray(result.data.suggestions)).toBe(true)
    })

    it('应该处理空前缀', async () => {
      const response = await fetch(`${API_BASE_URL}/search/suggestions?prefix=`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })
  })

  describe('GET /search/autocomplete', () => {
    it('应该获取自动补全建议', async () => {
      const response = await fetch(`${API_BASE_URL}/search/autocomplete?prefix=学术`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(Array.isArray(result.data)).toBe(true)
    })

    it('应该处理无匹配结果', async () => {
      const response = await fetch(`${API_BASE_URL}/search/autocomplete?prefix=xyz123`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(Array.isArray(result.data)).toBe(true)
    })
  })

  describe('GET /search/hot', () => {
    it('应该获取热门搜索词', async () => {
      const response = await fetch(`${API_BASE_URL}/search/hot`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      expect(result.code).toBe(200)
      expect(Array.isArray(result.data)).toBe(true)
    })
  })

  describe('POST /search/count', () => {
    it('应该增加搜索次数', async () => {
      const token = localStorage.getItem('token')

      const response = await fetch(`${API_BASE_URL}/search/count`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ keyword: '测试关键词' })
      })
      const result = await response.json()

      expect(result.code).toBe(200)
    })
  })
})
