/**
 * API集成测试配置
 * 此类包含测试所需的配置信息和辅助函数
 */

export const API_BASE_URL = 'http://localhost:8080/api/v1'

export const TEST_USER = {
  username: 'testuser',
  password: '123456'
}

export const TEST_ADMIN = {
  username: 'admin',
  password: 'admin123'
}

let authToken: string | null = null

/**
 * 设置认证Token
 */
export function setAuthToken(token: string) {
  authToken = token
  localStorage.setItem('token', token)
}

/**
 * 获取认证Token
 */
export function getAuthToken(): string | null {
  if (!authToken) {
    authToken = localStorage.getItem('token')
  }
  return authToken
}

/**
 * 清除认证Token
 */
export function clearAuthToken() {
  authToken = null
  localStorage.removeItem('token')
}

/**
 * 获取HTTP头（带认证）
 */
export function getAuthHeaders(): Record<string, string> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json'
  }
  const token = getAuthToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

/**
 * 通用GET请求
 */
export async function apiGet(endpoint: string, params?: Record<string, any>): Promise<any> {
  const url = new URL(`${API_BASE_URL}${endpoint}`)
  if (params) {
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null) {
        url.searchParams.append(key, String(params[key]))
      }
    })
  }

  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: getAuthHeaders()
  })
  return response.json()
}

/**
 * 通用POST请求
 */
export async function apiPost(endpoint: string, data?: any): Promise<any> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: data ? JSON.stringify(data) : undefined
  })
  return response.json()
}

/**
 * 通用PUT请求
 */
export async function apiPut(endpoint: string, data?: any): Promise<any> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: data ? JSON.stringify(data) : undefined
  })
  return response.json()
}

/**
 * 通用DELETE请求
 */
export async function apiDelete(endpoint: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'DELETE',
    headers: getAuthHeaders()
  })
  return response.json()
}

/**
 * 登录并获取Token
 */
export async function loginAndGetToken(username: string, password: string): Promise<string> {
  const result = await apiPost('/users/login', { username, password })
  if (result.code === 200 && result.data?.token) {
    setAuthToken(result.data.token)
    return result.data.token
  }
  throw new Error(result.message || '登录失败')
}

/**
 * 等待指定毫秒数
 */
export function wait(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}
