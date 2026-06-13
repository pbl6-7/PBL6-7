import apiClient from './client';
import type { Activity, ActivityApprovalStatistics } from '@/types/activity';
import type { UserPageRequest, UserPageResponse, UserResponse, UpdateRoleRequest, BatchOperationRequest, BatchOperationResponse, OverviewStatistics, ActivityStatistics, UserStatistics, RegistrationStatistics, TrendData, HotActivity, SensitiveWord, SensitiveWordCreateRequest, SensitiveWordUpdateRequest, SensitiveWordBatchRequest, SensitiveWordPageResponse, SensitiveWordStatistics, SensitiveWordCheckResult, LoginLockPageResponse, LoginLock } from '@/types/admin';
import type { ApiResponse } from '@/types/common';

/** 获取待审核活动 */
export const getPendingActivities = () =>
  apiClient.get<ApiResponse<Activity[]>>('/admin/activities/pending');

/** 按审核状态获取活动 */
export const getActivitiesByApprovalStatus = (status: string) =>
  apiClient.get<ApiResponse<Activity[]>>(`/admin/activities/approval-status/${status}`);

/** 审核通过 */
export const approveActivity = (id: number) =>
  apiClient.put<ApiResponse<Activity>>(`/admin/activities/${id}/approve`);

/** 审核拒绝 */
export const rejectActivity = (id: number, reason: string) =>
  apiClient.put<ApiResponse<Activity>>(`/admin/activities/${id}/reject`, { reason });

/** 获取审核统计 */
export const getApprovalStatistics = () =>
  apiClient.get<ApiResponse<ActivityApprovalStatistics>>('/admin/activities/statistics');

/** 获取用户列表 */
export const getUserPageList = (params: UserPageRequest) =>
  apiClient.get<ApiResponse<UserPageResponse>>('/admin/users', { params });

/** 获取所有用户 */
export const getAllUsers = () =>
  apiClient.get<ApiResponse<UserResponse[]>>('/admin/users/all');

/** 获取用户详情 */
export const getUserById = (id: number) =>
  apiClient.get<ApiResponse<UserResponse>>(`/admin/users/${id}`);

/** 按角色获取用户 */
export const getUsersByRole = (role: string) =>
  apiClient.get<ApiResponse<UserResponse[]>>(`/admin/users/role/${role}`);

/** 更新用户角色 */
export const updateUserRole = (id: number, data: UpdateRoleRequest) =>
  apiClient.put<ApiResponse<void>>(`/admin/users/${id}/role`, data);

/** 启用用户 */
export const enableUser = (id: number) =>
  apiClient.post<ApiResponse<void>>(`/admin/users/${id}/enable`);

/** 禁用用户 */
export const disableUser = (id: number) =>
  apiClient.post<ApiResponse<void>>(`/admin/users/${id}/disable`);

/** 批量启用用户 */
export const batchEnableUsers = (data: BatchOperationRequest) =>
  apiClient.post<ApiResponse<BatchOperationResponse>>('/admin/users/batch/enable', data);

/** 批量禁用用户 */
export const batchDisableUsers = (data: BatchOperationRequest) =>
  apiClient.post<ApiResponse<BatchOperationResponse>>('/admin/users/batch/disable', data);

/** 获取禁用用户列表 */
export const getDisabledUsers = (params: UserPageRequest) =>
  apiClient.get<ApiResponse<UserPageResponse>>('/admin/users/disabled', { params });

/** 获取统计概览 */
export const getOverviewStatistics = () =>
  apiClient.get<ApiResponse<OverviewStatistics>>('/admin/statistics/overview');

/** 获取活动统计 */
export const getActivityStatistics = () =>
  apiClient.get<ApiResponse<ActivityStatistics>>('/admin/statistics/activities');

/** 获取用户统计 */
export const getUserStatistics = () =>
  apiClient.get<ApiResponse<UserStatistics>>('/admin/statistics/users');

/** 获取报名统计 */
export const getRegistrationStatistics = () =>
  apiClient.get<ApiResponse<RegistrationStatistics>>('/admin/statistics/registrations');

/** 获取趋势数据 */
export const getTrendStatistics = (startDate?: string, endDate?: string, timeUnit = 'month') =>
  apiClient.get<ApiResponse<Record<string, TrendData[]>>>('/admin/statistics/trend', { params: { startDate, endDate, timeUnit } });

/** 获取热门活动 */
export const getHotActivities = (limit = 10, sortBy = 'registration') =>
  apiClient.get<ApiResponse<HotActivity[]>>('/admin/statistics/hot-activities', { params: { limit, sortBy } });

/** 清除统计缓存 */
export const clearStatisticsCache = () =>
  apiClient.post<ApiResponse<void>>('/admin/statistics/clear-cache');

/** 获取系统状态 */
export const getSystemStatus = () =>
  apiClient.get<ApiResponse<any>>('/admin/monitor/status');

/** 获取系统指标 */
export const getSystemMetrics = () =>
  apiClient.get<ApiResponse<any>>('/admin/monitor/metrics');

/** 获取最近活动 */
export const getRecentActivities = () =>
  apiClient.get<ApiResponse<Activity[]>>('/admin/monitor/recent-activities');

/** 获取最近用户 */
export const getRecentUsers = () =>
  apiClient.get<ApiResponse<UserResponse[]>>('/admin/monitor/recent-users');

/** 获取缓存统计 */
export const getCacheStats = () =>
  apiClient.get<ApiResponse<any>>('/admin/cache/stats');

/** 获取缓存名称列表 */
export const getCacheNames = () =>
  apiClient.get<ApiResponse<string[]>>('/admin/cache/names');

/** 清空所有缓存 */
export const clearAllCache = () =>
  apiClient.post<ApiResponse<void>>('/admin/cache/clear');

/** 清空指定缓存 */
export const clearCacheByName = (name: string) =>
  apiClient.post<ApiResponse<void>>(`/admin/cache/clear/${name}`);

/** 获取锁定列表 */
export const getLockList = (page = 1, size = 10) =>
  apiClient.get<ApiResponse<LoginLockPageResponse>>('/admin/login-lock/list', { params: { page, size } });

/** 获取用户锁定信息 */
export const getUserLockInfo = (username: string) =>
  apiClient.get<ApiResponse<LoginLock>>(`/admin/login-lock/user/${username}`);

/** 解锁用户 */
export const unlockUser = (username: string) =>
  apiClient.post<ApiResponse<void>>(`/admin/login-lock/unlock/${username}`);

/** 获取用户锁定历史 */
export const getUserLockHistory = (username: string, page = 1, size = 10) =>
  apiClient.get<ApiResponse<LoginLockPageResponse>>(`/admin/login-lock/history/${username}`, { params: { page, size } });

/** 获取敏感词列表 */
export const getSensitiveWords = (params?: any) =>
  apiClient.get<ApiResponse<SensitiveWordPageResponse>>('/v1/admin/sensitive-words', { params });

/** 获取敏感词详情 */
export const getSensitiveWordById = (id: number) =>
  apiClient.get<ApiResponse<SensitiveWord>>(`/v1/admin/sensitive-words/${id}`);

/** 添加敏感词 */
export const createSensitiveWord = (data: SensitiveWordCreateRequest) =>
  apiClient.post<ApiResponse<SensitiveWord>>('/v1/admin/sensitive-words', data);

/** 更新敏感词 */
export const updateSensitiveWord = (id: number, data: SensitiveWordUpdateRequest) =>
  apiClient.put<ApiResponse<SensitiveWord>>(`/v1/admin/sensitive-words/${id}`, data);

/** 删除敏感词 */
export const deleteSensitiveWord = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/v1/admin/sensitive-words/${id}`);

/** 批量添加敏感词 */
export const batchCreateSensitiveWords = (data: SensitiveWordBatchRequest) =>
  apiClient.post<ApiResponse<number>>('/v1/admin/sensitive-words/batch', data);

/** 重新加载敏感词库 */
export const reloadSensitiveWords = () =>
  apiClient.post<ApiResponse<void>>('/v1/admin/sensitive-words/reload');

/** 获取敏感词库统计信息 */
export const getSensitiveWordStatistics = () =>
  apiClient.get<ApiResponse<SensitiveWordStatistics>>('/v1/admin/sensitive-words/statistics');

/** 检查文本是否包含敏感词 */
export const checkSensitiveWord = (text: string) =>
  apiClient.get<ApiResponse<SensitiveWordCheckResult>>('/v1/admin/sensitive-words/check', { params: { text } });

/** 获取用户自定义权限 */
export const getUserPermissions = (userId: number) =>
  apiClient.get<ApiResponse<any>>(`/admin/users/${userId}/permissions`);

/** 更新用户自定义权限 */
export const updateUserPermissions = (userId: number, permissions: string[]) =>
  apiClient.put<ApiResponse<void>>(`/admin/users/${userId}/permissions`, { permissions });
