/**
 * 搜索功能单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockGet, mockPost } = vi.hoisted(() => ({
  mockGet: vi.fn(),
  mockPost: vi.fn()
}))

vi.mock('@/utils/request', () => ({
  default: {
    get: mockGet,
    post: mockPost
  }
}))

import { getSearchSuggestions, getAutocomplete, getHotSearches } from '@/api/search'

describe('搜索功能单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getSearchSuggestions', () => {
    it('应该正确获取搜索建议', async () => {
      const mockResponse = {
        code: 200,
        data: {
          suggestions: ['校园宣讲会', '校园招聘会', '校园体育赛事'],
          hotSearches: []
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getSearchSuggestions('校园')

      expect(mockGet).toHaveBeenCalledWith('/search/suggestions', { params: { prefix: '校园' } })
      expect(result.data.suggestions.length).toBe(3)
    })

    it('应该处理空前缀', async () => {
      const mockResponse = {
        code: 200,
        data: { suggestions: [], hotSearches: [] }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getSearchSuggestions('')

      expect(result.data.suggestions.length).toBe(0)
    })
  })

  describe('getAutocomplete', () => {
    it('应该正确获取自动补全建议', async () => {
      const mockResponse = {
        code: 200,
        data: ['学术讲座', '学术会议', '学术交流']
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getAutocomplete('学术')

      expect(mockGet).toHaveBeenCalledWith('/search/autocomplete', { params: { prefix: '学术' } })
      expect(result.data.length).toBe(3)
    })

    it('应该处理无匹配结果', async () => {
      const mockResponse = {
        code: 200,
        data: []
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getAutocomplete('xyz')

      expect(result.data.length).toBe(0)
    })
  })

  describe('getHotSearches', () => {
    it('应该正确获取热门搜索词', async () => {
      const mockResponse = {
        code: 200,
        data: ['招聘会', '宣讲会', '体育赛事', '文艺演出']
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getHotSearches()

      expect(mockGet).toHaveBeenCalledWith('/search/hot')
      expect(result.data.length).toBe(4)
      expect(result.data[0]).toBe('招聘会')
    })

    it('应该处理空热门列表', async () => {
      const mockResponse = {
        code: 200,
        data: []
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getHotSearches()

      expect(result.data.length).toBe(0)
    })
  })
})
