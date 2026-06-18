import apiClient from './client';
import type { Activity, ActivityPublishRequest, ActivityQueryRequest, ActivityPageResponse, ActivityApprovalRequest, ActivityApprovalStatistics, TagResponse, Registration } from '@/types/activity';
import type { RegistrationPageResponse } from '@/types/registration';
import type { ApiResponse } from '@/types/common';

/** 创建活动 */
export const createActivity = (data: ActivityPublishRequest) =>
  apiClient.post<ApiResponse<Activity>>('/activities', data);

/** 获取活动详情 */
export const getActivityById = (id: number) =>
  apiClient.get<ApiResponse<Activity>>(`/activities/${id}`);

/** 获取我发布的活动 */
export const getMyActivities = () =>
  apiClient.get<ApiResponse<Activity[]>>('/activities/my');

/** 编辑活动 */
export const updateActivity = (id: number, data: ActivityPublishRequest) =>
  apiClient.put<ApiResponse<Activity>>(`/activities/${id}`, data);

/** 删除活动 */
export const deleteActivity = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/activities/${id}`);

/** 获取活动列表（带筛选分页） */
export const getActivityList = (params: ActivityQueryRequest) =>
  apiClient.get<ApiResponse<ActivityPageResponse>>('/activities/list', { params });

/** 发布活动（状态变更） - 后端使用 PUT */
export const publishActivityStatus = (id: number) =>
  apiClient.put<ApiResponse<any>>(`/activities/${id}/publish`);

/** 取消活动 - 后端使用 PUT */
export const cancelActivity = (id: number, reason?: string) =>
  apiClient.put<ApiResponse<any>>(`/activities/${id}/cancel`, reason ? { reason } : undefined);

/** 结束活动 - 后端使用 PUT */
export const endActivity = (id: number, reason?: string) =>
  apiClient.put<ApiResponse<any>>(`/activities/${id}/end`, reason ? { reason } : undefined);

/** 更新活动状态 - 后端使用 RequestBody */
export const updateActivityStatus = (id: number, newStatus: string, reason?: string) =>
  apiClient.put<ApiResponse<any>>(`/activities/${id}/status`, { status: newStatus, reason });

/** 获取活动状态信息 */
export const getActivityStatus = (id: number) =>
  apiClient.get<ApiResponse<any>>(`/activities/${id}/status`);

/** 分享活动 */
export const shareActivity = (id: number, shareChannel?: string) =>
  apiClient.post<ApiResponse<any>>(`/activities/${id}/share`, null, { params: { shareChannel } });

/** 获取活动分享统计 */
export const getShareCount = (id: number) =>
  apiClient.get<ApiResponse<any>>(`/activities/${id}/share-count`);

/** 获取活动图片 */
export const getActivityImages = (activityId: number) =>
  apiClient.get<ApiResponse<any[]>>(`/activities/${activityId}/images`);

/** 上传活动图片 - 后端接受单文件 + description + sortOrder */
export const uploadActivityImage = (
  activityId: number,
  file: File,
  description: string = '',
  sortOrder: number = 0
) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('description', description);
  formData.append('sortOrder', String(sortOrder));
  return apiClient.post<ApiResponse<any>>(`/activities/${activityId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/** 删除活动图片 */
export const deleteActivityImage = (activityId: number, imageId: number) =>
  apiClient.delete<ApiResponse<void>>(`/activities/${activityId}/images/${imageId}`);

/** 获取所有标签 */
export const getAllTags = () =>
  apiClient.get<ApiResponse<TagResponse[]>>('/tags');

/** 获取我的报名记录 */
export const getMyRegistrations = () =>
  apiClient.get<ApiResponse<RegistrationPageResponse>>('/registrations/my');

/** 获取热门活动 */
export const getHotActivities = (limit: number = 10, sortBy: string = 'registration') =>
  apiClient.get<ApiResponse<any>>('/activities/hot', { params: { limit, sortBy } });

/** 获取活动列表（根路径） */
export const getActivityListRoot = (params?: any) =>
  apiClient.get<ApiResponse<any>>('/activities', { params });
