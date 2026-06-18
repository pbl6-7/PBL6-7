import axios from 'axios';
import type { Activity, ActivityApprovalStatistics } from '@/types/activity';
import type { UserPageRequest, UserPageResponse, UserResponse, UpdateRoleRequest, BatchOperationRequest, BatchOperationResponse, OverviewStatistics, ActivityStatistics, UserStatistics, RegistrationStatistics, TrendData, HotActivity, SensitiveWord, SensitiveWordCreateRequest, SensitiveWordUpdateRequest, SensitiveWordBatchRequest, SensitiveWordPageResponse, SensitiveWordStatistics, SensitiveWordCheckResult, LoginLockPageResponse, LoginLock } from '@/types/admin';
import type { ApiResponse } from '@/types/common';

// Admin API 专用 client（使用 /api 前缀，不带 /v1）
const adminApiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
adminApiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user.userId) {
        config.headers['X-User-Id'] = String(user.userId);
      } else if (user.id) {
        config.headers['X-User-Id'] = String(user.id);
      }
    } catch (e) {}
  }
  return config;
});

// 响应拦截器
adminApiClient.interceptors.response.use(
  (response) => {
    if (response.data.code !== 200) {
      const error = new Error(response.data.message || '请求失败');
      (error as any).code = response.data.code;
      return Promise.reject(error);
    }
    return response;
  },
  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
      const data = error.response.data;
      const err = new Error(data?.message || `请求失败 (${error.response.status})`);
      (err as any).code = data?.code;
      return Promise.reject(err);
    }
    return Promise.reject(error);
  }
);

/** 获取待审核活动 */
export const getPendingActivities = () =>
  adminApiClient.get<ApiResponse<Activity[]>>('/admin/activities/pending');

/** 按审核状态获取活动 */
export const getActivitiesByApprovalStatus = (status: string) =>
  adminApiClient.get<ApiResponse<Activity[]>>(`/admin/activities/approval-status/${status}`);

/** 按活动状态获取活动 */
export const getActivitiesByStatus = (status: string) =>
  adminApiClient.get<ApiResponse<Activity[]>>(`/admin/activities/status/${status}`);

/** 删除活动（管理员） */
export const deleteActivityAdmin = (id: number) =>
  adminApiClient.delete<ApiResponse<void>>(`/admin/activities/${id}`);

/** 审核通过 */
export const approveActivity = (id: number) =>
  adminApiClient.put<ApiResponse<Activity>>(`/admin/activities/${id}/approve`);

/** 审核拒绝 */
export const rejectActivity = (id: number, reason: string) =>
  adminApiClient.put<ApiResponse<Activity>>(`/admin/activities/${id}/reject`, { reason });

/** 活动审核（通过/拒绝） */
export const auditActivity = (id: number, approved: boolean, reason?: string) => {
  if (approved) {
    return adminApiClient.put<ApiResponse<Activity>>(`/admin/activities/${id}/approve`);
  } else {
    return adminApiClient.put<ApiResponse<Activity>>(`/admin/activities/${id}/reject`, { reason });
  }
};

/** 获取审核统计 */
export const getApprovalStatistics = () =>
  adminApiClient.get<ApiResponse<ActivityApprovalStatistics>>('/admin/activities/statistics');

/** 获取用户列表 */
export const getUserPageList = (params: UserPageRequest) =>
  adminApiClient.get<ApiResponse<UserPageResponse>>('/admin/users', { params });

/** 获取所有用户 */
export const getAllUsers = () =>
  adminApiClient.get<ApiResponse<UserResponse[]>>('/admin/users/all');

/** 获取用户详情 */
export const getUserById = (id: number) =>
  adminApiClient.get<ApiResponse<UserResponse>>(`/admin/users/${id}`);

/** 按角色获取用户 */
export const getUsersByRole = (role: string) =>
  adminApiClient.get<ApiResponse<UserResponse[]>>(`/admin/users/role/${role}`);

/** 更新用户角色 */
export const updateUserRole = (id: number, data: UpdateRoleRequest) =>
  adminApiClient.put<ApiResponse<void>>(`/admin/users/${id}/role`, data);

/** 启用用户 - 后端使用 PUT /{id}/status */
export const enableUser = (id: number) =>
  adminApiClient.put<ApiResponse<void>>(`/admin/users/${id}/status`, { status: 'enabled' });

/** 禁用用户 - 后端使用 PUT /{id}/status */
export const disableUser = (id: number) =>
  adminApiClient.put<ApiResponse<void>>(`/admin/users/${id}/status`, { status: 'disabled' });

/** 批量操作用户 - 后端使用 POST /admin/users/batch */
export const batchEnableUsers = (data: BatchOperationRequest) =>
  adminApiClient.post<ApiResponse<BatchOperationResponse>>('/admin/users/batch', { ...data, action: 'enable' });

/** 批量禁用用户 - 后端使用 POST /admin/users/batch */
export const batchDisableUsers = (data: BatchOperationRequest) =>
  adminApiClient.post<ApiResponse<BatchOperationResponse>>('/admin/users/batch', { ...data, action: 'disable' });

/** 获取锁定用户列表 - 后端路径 /admin/users/locked */
export const getDisabledUsers = (params: UserPageRequest) =>
  adminApiClient.get<ApiResponse<UserPageResponse>>('/admin/users/locked', { params });

/** 获取统计概览 */
export const getOverviewStatistics = () =>
  adminApiClient.get<ApiResponse<OverviewStatistics>>('/admin/statistics/overview');

/** 获取活动统计 */
export const getActivityStatistics = () =>
  adminApiClient.get<ApiResponse<ActivityStatistics>>('/admin/statistics/activities');

/** 获取用户统计 */
export const getUserStatistics = () =>
  adminApiClient.get<ApiResponse<UserStatistics>>('/admin/statistics/users');

/** 获取报名统计 */
export const getRegistrationStatistics = () =>
  adminApiClient.get<ApiResponse<RegistrationStatistics>>('/admin/statistics/registrations');

/** 获取趋势数据 */
export const getTrendStatistics = (startDate?: string, endDate?: string, timeUnit = 'month') =>
  adminApiClient.get<ApiResponse<Record<string, TrendData[]>>>('/admin/statistics/trend', { params: { startDate, endDate, timeUnit } });

/** 获取热门活动 */
export const getHotActivities = (limit = 10, sortBy = 'registration') =>
  adminApiClient.get<ApiResponse<HotActivity[]>>('/admin/statistics/hot-activities', { params: { limit, sortBy } });

/** 清除统计缓存 */
export const clearStatisticsCache = () =>
  adminApiClient.post<ApiResponse<void>>('/admin/statistics/clear-cache');

/** 获取系统状态 */
export const getSystemStatus = () =>
  adminApiClient.get<ApiResponse<any>>('/admin/monitor/status');

/** 获取系统指标 */
export const getSystemMetrics = () =>
  adminApiClient.get<ApiResponse<any>>('/admin/monitor/metrics');

/** 获取最近活动 */
export const getRecentActivities = () =>
  adminApiClient.get<ApiResponse<Activity[]>>('/admin/monitor/recent-activities');

/** 获取最近用户 */
export const getRecentUsers = () =>
  adminApiClient.get<ApiResponse<UserResponse[]>>('/admin/monitor/recent-users');

/** 获取缓存统计 - 后端路径 /admin/monitor/cache */
export const getCacheStats = () =>
  adminApiClient.get<ApiResponse<any>>('/admin/monitor/cache');

/** 获取缓存名称列表 - 从缓存信息接口获取 */
export const getCacheNames = () =>
  adminApiClient.get<ApiResponse<string[]>>('/admin/monitor/cache');

/** 清空所有缓存 - 后端使用 DELETE */
export const clearAllCache = () =>
  adminApiClient.delete<ApiResponse<void>>('/admin/monitor/cache/clear');

/** 清空指定缓存 - 后端使用 DELETE */
export const clearCacheByName = (name: string) =>
  adminApiClient.delete<ApiResponse<void>>(`/admin/monitor/cache/clear/${name}`);

/** 获取锁定用户列表 - 后端返回 List<Map>，无分页 */
export const getLockList = () =>
  adminApiClient.get<ApiResponse<any[]>>('/admin/login-lock/list');

/** 解锁用户 - 后端使用 DELETE */
export const unlockUser = (username: string) =>
  adminApiClient.delete<ApiResponse<void>>(`/admin/login-lock/${username}`);

/** 获取所有敏感词 - 后端返回 List<SensitiveWord>，无分页 */
export const getSensitiveWords = () =>
  adminApiClient.get<ApiResponse<SensitiveWord[]>>('/admin/sensitive-words');

/** 按类型获取敏感词 - 后端路径 /type/{type} */
export const getSensitiveWordsByType = (type: string) =>
  adminApiClient.get<ApiResponse<SensitiveWord[]>>(`/admin/sensitive-words/type/${type}`);

/** 获取敏感词详情 */
export const getSensitiveWordById = (id: number) =>
  adminApiClient.get<ApiResponse<SensitiveWord>>(`/admin/sensitive-words/${id}`);

/** 添加敏感词 */
export const createSensitiveWord = (data: SensitiveWordCreateRequest) =>
  adminApiClient.post<ApiResponse<SensitiveWord>>('/admin/sensitive-words', data);

/** 更新敏感词 */
export const updateSensitiveWord = (id: number, data: SensitiveWordUpdateRequest) =>
  adminApiClient.put<ApiResponse<SensitiveWord>>(`/admin/sensitive-words/${id}`, data);

/** 删除敏感词 */
export const deleteSensitiveWord = (id: number) =>
  adminApiClient.delete<ApiResponse<void>>(`/admin/sensitive-words/${id}`);

/** 批量删除敏感词 - 后端使用 DELETE /batch */
export const batchDeleteSensitiveWords = (ids: number[]) =>
  adminApiClient.delete<ApiResponse<void>>('/admin/sensitive-words/batch', { data: ids });

/** 批量添加敏感词 */
export const batchCreateSensitiveWords = (data: SensitiveWordBatchRequest) =>
  adminApiClient.post<ApiResponse<number>>('/admin/sensitive-words/batch', data);

/** 重新加载敏感词库 - 后端路径 /refresh */
export const reloadSensitiveWords = () =>
  adminApiClient.post<ApiResponse<void>>('/admin/sensitive-words/refresh');

/** 获取敏感词库统计信息 - 后端路径 /stats */
export const getSensitiveWordStatistics = () =>
  adminApiClient.get<ApiResponse<SensitiveWordStatistics>>('/admin/sensitive-words/stats');

/** 检查文本是否包含敏感词 - 后端使用 POST + RequestBody */
export const checkSensitiveWord = (text: string) =>
  adminApiClient.post<ApiResponse<SensitiveWordCheckResult>>('/admin/sensitive-words/check', { text });

/** 获取用户自定义权限 */
export const getUserPermissions = (userId: number) =>
  adminApiClient.get<ApiResponse<any>>(`/admin/users/${userId}/permissions`);

/** 更新用户自定义权限 */
export const updateUserPermissions = (userId: number, permissions: string[]) =>
  adminApiClient.put<ApiResponse<void>>(`/admin/users/${userId}/permissions`, { permissions });

/** 解锁用户（管理员用户管理） - 后端路径 PUT /admin/users/{id}/unlock */
export const unlockUserAccount = (id: number) =>
  adminApiClient.put<ApiResponse<void>>(`/admin/users/${id}/unlock`);

/** 获取权限列表 - 后端路径 GET /admin/users/permissions */
export const getPermissions = () =>
  adminApiClient.get<ApiResponse<any[]>>('/admin/users/permissions');

/** 发布系统公告 - 后端路径 POST /admin/announcements */
export const publishAnnouncement = (data: { title: string; content: string }) =>
  adminApiClient.post<ApiResponse<string>>('/admin/announcements', data);

/** 获取所有通知列表（管理员） */
export const getAdminNotifications = (page = 1, size = 20) =>
  adminApiClient.get<ApiResponse<any>>('/admin/announcements/notifications', { params: { page, size } });

/** 管理员删除通知 */
export const deleteAdminNotification = (id: number) =>
  adminApiClient.delete<ApiResponse<void>>(`/admin/announcements/notifications/${id}`);
