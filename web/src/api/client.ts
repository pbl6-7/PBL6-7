import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios';

// API 响应接口
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  requestId?: string;
}

// 分页响应接口
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

// 请求配置接口
export interface RequestConfig extends InternalAxiosRequestConfig {
  hideLoading?: boolean;
}

// 创建 axios 实例
const apiClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config: RequestConfig) => {
    // 添加 Token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // 添加用户ID（后端需要）
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        // 登录返回的是 userId 字段
        if (user.userId) {
          config.headers['X-User-Id'] = String(user.userId);
        } else if (user.id) {
          config.headers['X-User-Id'] = String(user.id);
        }
      } catch (e) {
        // ignore
      }
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 如果业务 code 不是 200，抛出错误
    if (response.data.code !== 200) {
      const error = new Error(response.data.message || '请求失败');
      (error as any).code = response.data.code;
      (error as any).response = response;
      return Promise.reject(error);
    }
    return response;
  },
  (error: AxiosError) => {
    if (error.response) {
      // 服务器返回错误状态码
      const data = error.response.data as ApiResponse | undefined;
      const status = error.response.status;
      
      switch (status) {
        case 401:
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
          break;
        case 429:
          console.error('请求过于频繁，请稍后再试');
          break;
      }
      
      // 构建错误对象
      const err = new Error(data?.message || `请求失败 (${status})`);
      (err as any).code = data?.code;
      (err as any).response = error.response;
      return Promise.reject(err);
    } else if (error.request) {
      const err = new Error('网络连接失败，请检查网络');
      return Promise.reject(err);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
