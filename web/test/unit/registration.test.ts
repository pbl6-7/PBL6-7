/**
 * 活动报名单元测试
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockPost, mockGet, mockPut, mockDelete } = vi.hoisted(() => ({
  mockPost: vi.fn(),
  mockGet: vi.fn(),
  mockPut: vi.fn(),
  mockDelete: vi.fn()
}))

vi.mock('@/utils/request', () => ({
  default: {
    post: mockPost,
    get: mockGet,
    put: mockPut,
    delete: mockDelete
  }
}))

import {
  registerActivity,
  cancelRegistration,
  getMyRegistrations,
  getActivityRegistrations
} from '@/api/registration'

describe('活动报名单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('registerActivity', () => {
    it('应该正确报名活动', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, activityId: 1, status: 'confirmed' }
      }
      mockPost.mockResolvedValue(mockResponse)

      const result = await registerActivity({ activityId: 1 })

      expect(mockPost).toHaveBeenCalledWith('/registrations', { activityId: 1 })
      expect(result).toEqual(mockResponse)
    })

    it('应该处理活动不存在', async () => {
      const error = new Error('活动不存在')
      mockPost.mockRejectedValue(error)

      await expect(registerActivity({ activityId: 99999 })).rejects.toThrow('活动不存在')
    })

    it('应该处理重复报名', async () => {
      const error = new Error('已报名')
      mockPost.mockRejectedValue(error)

      await expect(registerActivity({ activityId: 1 })).rejects.toThrow('已报名')
    })
  })

  describe('cancelRegistration', () => {
    it('应该正确取消报名', async () => {
      const mockResponse = {
        code: 200,
        message: '取消成功'
      }
      mockDelete.mockResolvedValue(mockResponse)

      const result = await cancelRegistration(1)

      expect(mockDelete).toHaveBeenCalledWith('/registrations/activity/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理报名不存在', async () => {
      const error = new Error('报名记录不存在')
      mockDelete.mockRejectedValue(error)

      await expect(cancelRegistration(99999)).rejects.toThrow('报名记录不存在')
    })
  })

  describe('getMyRegistrations', () => {
    it('应该正确获取我的报名列表', async () => {
      const mockResponse = {
        code: 200,
        data: {
          records: [
            { id: 1, activityId: 1, activityTitle: '活动1', status: 'confirmed' },
            { id: 2, activityId: 2, activityTitle: '活动2', status: 'pending' }
          ],
          total: 2
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getMyRegistrations(1, 10)

      expect(mockGet).toHaveBeenCalledWith('/registrations/my', { params: { page: 1, size: 10 } })
      expect(result.data.records.length).toBe(2)
    })

    it('应该处理空列表', async () => {
      const mockResponse = {
        code: 200,
        data: { records: [], total: 0 }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getMyRegistrations(1, 10)

      expect(result.data.records.length).toBe(0)
    })
  })

  describe('getActivityRegistrations', () => {
    it('应该正确获取活动的报名列表', async () => {
      const mockResponse = {
        code: 200,
        data: {
          records: [
            { id: 1, userId: 1, userName: '用户1', status: 'confirmed' },
            { id: 2, userId: 2, userName: '用户2', status: 'pending' }
          ],
          total: 2
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getActivityRegistrations(1, 1, 10)

      expect(mockGet).toHaveBeenCalledWith('/registrations/activity/1', { params: { page: 1, size: 10 } })
      expect(result.data.records.length).toBe(2)
    })
  })
})
