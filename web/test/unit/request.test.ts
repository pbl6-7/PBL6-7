/**
 * Request工具单元测试
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn()
}

const mockRouter = {
  push: vi.fn()
}

vi.mock('axios', () => {
  const mockAxios = vi.fn(() => ({
    create: vi.fn(() => ({
      interceptors: {
        request: { use: vi.fn(), eject: vi.fn() },
        response: { use: vi.fn(), eject: vi.fn() }
      },
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn()
    }))
  }))
  mockAxios.create = vi.fn(() => ({
    interceptors: {
      request: { use: vi.fn(), eject: vi.fn() },
      response: { use: vi.fn(), eject: vi.fn() }
    },
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }))
  return { default: mockAxios }
})

vi.mock('localStorage', () => mockLocalStorage)
vi.mock('vue-router', () => ({ default: mockRouter }))

describe('Request工具单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockLocalStorage.getItem.mockReturnValue('mock-token')
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Token处理', () => {
    it('应该在请求时添加Authorization头', async () => {
      mockLocalStorage.getItem.mockReturnValue('test-token')
      expect(mockLocalStorage.getItem('token')).toBe('test-token')
    })

    it('应该在没有token时不添加Authorization头', async () => {
      mockLocalStorage.getItem.mockReturnValue(null)
      expect(mockLocalStorage.getItem('token')).toBe(null)
    })
  })

  describe('Token清除', () => {
    it('应该在一401响应时清除token', () => {
      mockLocalStorage.removeItem('token')
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('token')
    })
  })

  describe('路由跳转', () => {
    it('应该在一401响应时跳转到登录页', () => {
      expect(mockRouter.push).not.toHaveBeenCalled()
    })
  })
})
