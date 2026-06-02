/**
 * 用户管理单元测试
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

import { userLogin, userRegister, getUserProfile, updateProfile, changePassword } from '@/api/user'

describe('用户管理单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('userLogin', () => {
    it('应该正确调用登录接口', async () => {
      const mockResponse = {
        code: 200,
        data: {
          userId: 1,
          username: 'testuser',
          realName: '测试用户',
          role: 'user',
          token: 'mock-token'
        }
      }
      mockPost.mockResolvedValue(mockResponse)

      const result = await userLogin({ username: 'testuser', password: '123456' })

      expect(mockPost).toHaveBeenCalledWith('/users/login', { username: 'testuser', password: '123456' })
      expect(result).toEqual(mockResponse)
    })

    it('应该处理登录失败', async () => {
      const error = new Error('密码错误')
      mockPost.mockRejectedValue(error)

      await expect(userLogin({ username: 'testuser', password: 'wrong' })).rejects.toThrow('密码错误')
    })

    it('应该处理用户不存在', async () => {
      const error = new Error('用户不存在')
      mockPost.mockRejectedValue(error)

      await expect(userLogin({ username: 'notexist', password: '123456' })).rejects.toThrow('用户不存在')
    })
  })

  describe('userRegister', () => {
    it('应该正确调用注册接口', async () => {
      const mockResponse = {
        code: 200,
        data: { userId: 10, username: 'newuser' }
      }
      mockPost.mockResolvedValue(mockResponse)

      const result = await userRegister({
        username: 'newuser',
        password: '123456',
        realName: '新用户',
        contact: '13800138000',
        securityQuestionId: 1,
        securityAnswer: '答案'
      })

      expect(mockPost).toHaveBeenCalledWith('/users/register', expect.any(Object))
      expect(result).toEqual(mockResponse)
    })

    it('应该拒绝重复用户名', async () => {
      const error = new Error('用户名已存在')
      mockPost.mockRejectedValue(error)

      await expect(userRegister({ username: 'existing' } as any)).rejects.toThrow('用户名已存在')
    })
  })

  describe('getUserProfile', () => {
    it('应该正确获取用户资料', async () => {
      const mockResponse = {
        code: 200,
        data: {
          userId: 1,
          username: 'testuser',
          realName: '测试用户',
          email: 'test@example.com'
        }
      }
      mockGet.mockResolvedValue(mockResponse)

      const result = await getUserProfile()

      expect(mockGet).toHaveBeenCalledWith('/users/profile')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('updateProfile', () => {
    it('应该正确更新用户资料', async () => {
      const mockResponse = {
        code: 200,
        message: '更新成功'
      }
      mockPut.mockResolvedValue(mockResponse)

      const result = await updateProfile({ realName: '新名字', contact: '13800138000' })

      expect(mockPut).toHaveBeenCalledWith('/users/profile', { realName: '新名字', contact: '13800138000' })
      expect(result).toEqual(mockResponse)
    })
  })

  describe('changePassword', () => {
    it('应该正确修改密码', async () => {
      const mockResponse = {
        code: 200,
        message: '密码修改成功'
      }
      mockPut.mockResolvedValue(mockResponse)

      const result = await changePassword({ oldPassword: 'old123', newPassword: 'new456' })

      expect(mockPut).toHaveBeenCalledWith('/users/password', {
        oldPassword: 'old123',
        newPassword: 'new456'
      })
      expect(result).toEqual(mockResponse)
    })

    it('应该处理原密码错误', async () => {
      const error = new Error('原密码错误')
      mockPut.mockRejectedValue(error)

      await expect(changePassword({ oldPassword: 'wrong', newPassword: 'new456' })).rejects.toThrow('原密码错误')
    })
  })
})
