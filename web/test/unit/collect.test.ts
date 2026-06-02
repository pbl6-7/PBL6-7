/**
 * 收藏功能单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockPost, mockGet, mockDelete } = vi.hoisted(() => ({
  mockPost: vi.fn(),
  mockGet: vi.fn(),
  mockDelete: vi.fn()
}))

vi.mock('@/utils/request', () => ({
  default: {
    post: mockPost,
    get: mockGet,
    delete: mockDelete
  }
}))

import { collectActivity, cancelCollect, getMyCollections, checkCollectStatus } from '@/api/collect'

describe('收藏功能单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('collectActivity', () => {
    it('应该正确收藏活动', async () => {
      const mockResponse = {
        code: 200,
        data: { activityId: 1, collected: true }
      }
      mockPost.mockResolvedValue(mockResponse)

      const result = await collectActivity(1)

      expect(mockPost).toHaveBeenCalledWith('/activity-collect/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理活动不存在', async () => {
      const error = new Error('活动不存在')
      mockPost.mockRejectedValue(error)

      await expect(collectActivity(99999)).rejects.toThrow('活动不存在')
    })

    it('应该处理重复收藏', async () => {
      const error = new Error('已收藏')
      mockPost.mockRejectedValue(error)

      await expect(collectActivity(1)).rejects.toThrow('已收藏')
    })
  })

  describe('cancelCollect', () => {
    it('应该正确取消收藏', async () => {
      const mockResponse = {
        code: 200,
        data: { activityId: 1, collected: false }
      }
      mockDelete.mockResolvedValue(mockResponse)

      const result = await cancelCollect(1)

      expect(mockDelete).toHaveBeenCalledWith('/activity-collect/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理收藏记录不存在', async () => {
      const error = new Error('收藏记录不存在')
      mockDelete.mockRejectedValue(error)

      await expect(cancelCollect(99999)).rejects.toThrow('收藏记录不存在')
    })
  })

  describe('checkCollectStatus', () => {
    it('应该正确获取收藏状态-已收藏', async () => {
      const mockResponse = {
        code: 200,
        data: { collected: true, collectCount: 5 }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await checkCollectStatus(1)

      expect(mockGet).toHaveBeenCalledWith('/activity-collect/1/status')
      expect(result.data.collected).toBe(true)
    })

    it('应该正确获取收藏状态-未收藏', async () => {
      const mockResponse = {
        code: 200,
        data: { collected: false, collectCount: 0 }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await checkCollectStatus(1)

      expect(result.data.collected).toBe(false)
    })
  })

  describe('getMyCollections', () => {
    it('应该正确获取我的收藏列表', async () => {
      const mockResponse = {
        code: 200,
        data: [
          { id: 1, activityId: 1, activityTitle: '活动1' },
          { id: 2, activityId: 2, activityTitle: '活动2' }
        ]
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getMyCollections()

      expect(mockGet).toHaveBeenCalledWith('/activity-collect/my')
      expect(result.data.length).toBe(2)
    })

    it('应该处理空列表', async () => {
      const mockResponse = {
        code: 200,
        data: []
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getMyCollections()

      expect(result.data.length).toBe(0)
    })
  })
})
