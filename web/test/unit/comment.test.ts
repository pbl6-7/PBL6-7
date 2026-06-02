/**
 * 评论功能单元测试
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

import { publishComment, getCommentList, deleteComment } from '@/api/comment'

describe('评论功能单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('publishComment', () => {
    it('应该正确发布评论', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, content: '测试评论', activityId: 1 }
      }
      mockPost.mockResolvedValue(mockResponse)

      const result = await publishComment(1, { content: '测试评论' })

      expect(mockPost).toHaveBeenCalledWith('/activities/1/comments', { content: '测试评论' })
      expect(result).toEqual(mockResponse)
    })

    it('应该处理活动不存在', async () => {
      const error = new Error('活动不存在')
      mockPost.mockRejectedValue(error)

      await expect(publishComment(99999, { content: '测试' })).rejects.toThrow('活动不存在')
    })

    it('应该处理评论内容为空', async () => {
      const error = new Error('评论内容不能为空')
      mockPost.mockRejectedValue(error)

      await expect(publishComment(1, { content: '' })).rejects.toThrow('评论内容不能为空')
    })
  })

  describe('getCommentList', () => {
    it('应该正确获取活动评论列表', async () => {
      const mockResponse = {
        code: 200,
        data: [
          { id: 1, content: '评论1', userId: 1, userName: '用户1' },
          { id: 2, content: '评论2', userId: 2, userName: '用户2' }
        ]
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getCommentList(1, 1, 20)

      expect(mockGet).toHaveBeenCalledWith('/activities/1/comments', { params: { page: 1, size: 20 } })
      expect(result.data.length).toBe(2)
    })

    it('应该处理空列表', async () => {
      const mockResponse = {
        code: 200,
        data: []
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getCommentList(1, 1, 20)

      expect(result.data.length).toBe(0)
    })
  })

  describe('deleteComment', () => {
    it('应该正确删除评论', async () => {
      const mockResponse = {
        code: 200,
        message: '删除成功'
      }
      mockDelete.mockResolvedValue(mockResponse)

      const result = await deleteComment(1)

      expect(mockDelete).toHaveBeenCalledWith('/comments/1')
      expect(result).toEqual(mockResponse)
    })

    it('应该处理评论不存在', async () => {
      const error = new Error('评论不存在')
      mockDelete.mockRejectedValue(error)

      await expect(deleteComment(99999)).rejects.toThrow('评论不存在')
    })
  })
})
