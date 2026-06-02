/**
 * 活动管理单元测试 - 使用Mock测试业务逻辑
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
  getActivityDetail,
  getActivityList,
  publishActivity,
  updateActivity,
  deleteActivity
} from '@/api/activity'

describe('活动管理单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getActivityDetail', () => {
    it('应该正确获取活动详情', async () => {
      const mockResponse = {
        code: 200,
        data: {
          id: 1,
          title: '校园宣讲会',
          description: '企业宣讲',
          location: '图书馆报告厅',
          startTime: '2026-05-20T10:00:00',
          endTime: '2026-05-20T12:00:00',
          status: 'published'
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getActivityDetail(1)

      expect(mockGet).toHaveBeenCalledWith('/activities/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理获取不存在的活动', async () => {
      const error = new Error('活动不存在')
      mockGet.mockRejectedValue(error)

      await expect(getActivityDetail(99999)).rejects.toThrow('活动不存在')
    })
  })

  describe('getActivityList', () => {
    it('应该正确获取活动列表', async () => {
      const mockResponse = {
        code: 200,
        data: {
          list: [
            { id: 1, title: '活动1' },
            { id: 2, title: '活动2' }
          ],
          total: 2
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getActivityList({ page: 1, pageSize: 10 })

      expect(mockGet).toHaveBeenCalledWith('/activities/list', { params: { page: 1, pageSize: 10 } })
      expect(result).toEqual(mockResponse)
    })

    it('应该按状态筛选活动', async () => {
      const mockResponse = {
        code: 200,
        data: {
          list: [{ id: 1, title: '活动1', status: 'published' }],
          total: 1
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getActivityList({ status: 'published' })

      expect(mockGet).toHaveBeenCalled()
      expect(result.data.list[0].status).toBe('published')
    })
  })

  describe('publishActivity', () => {
    it('应该正确发布活动', async () => {
      const mockResponse = {
        code: 200,
        message: '活动发布成功',
        data: { id: 10 }
      }
      mockPost.mockResolvedValue(mockResponse)

      const activityData = {
        title: '新活动',
        description: '活动描述',
        location: '活动地点',
        startTime: '2026-06-01T10:00:00',
        endTime: '2026-06-01T12:00:00'
      }
      const result = await publishActivity(activityData)

      expect(mockPost).toHaveBeenCalledWith('/activities', activityData)
      expect(result).toEqual(mockResponse)
    })

    it('应该处理发布失败', async () => {
      const error = new Error('参数错误')
      mockPost.mockRejectedValue(error)

      await expect(publishActivity({})).rejects.toThrow('参数错误')
    })
  })

  describe('updateActivity', () => {
    it('应该正确更新活动', async () => {
      const mockResponse = {
        code: 200,
        message: '活动更新成功'
      }
      mockPut.mockResolvedValue(mockResponse)

      const updateData = { title: '更新后的标题' }
      const result = await updateActivity(1, updateData)

      expect(mockPut).toHaveBeenCalledWith('/activities/1', updateData)
      expect(result).toEqual(mockResponse)
    })
  })

  describe('deleteActivity', () => {
    it('应该正确删除活动', async () => {
      const mockResponse = {
        code: 200,
        message: '活动删除成功'
      }
      mockDelete.mockResolvedValue(mockResponse)

      const result = await deleteActivity(1)

      expect(mockDelete).toHaveBeenCalledWith('/activities/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理删除不存在的活动', async () => {
      const error = new Error('活动不存在')
      mockDelete.mockRejectedValue(error)

      await expect(deleteActivity(99999)).rejects.toThrow('活动不存在')
    })
  })
})
