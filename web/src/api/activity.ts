import apiClient from './client';
import type { Activity, ActivityPublishRequest, ActivityQueryRequest, ActivityPageResponse, ActivityApprovalRequest, ActivityApprovalStatistics } from '@/types/activity';
import type { ApiResponse } from '@/types/common';

/** 发布活动 */
export const publishActivity = (data: ActivityPublishRequest) =>
  apiClient.post<ApiResponse<Activity>>('/v1/activities', data);

/** 获取活动详情 */
export const getActivityById = (id: number) =>
  apiClient.get<ApiResponse<Activity>>(`/v1/activities/${id}`);

/** 获取我发布的活动 */
export const getMyActivities = () =>
  apiClient.get<ApiResponse<Activity[]>>('/v1/activities/my');

/** 编辑活动 */
export const updateActivity = (id: number, data: ActivityPublishRequest) =>
  apiClient.put<ApiResponse<Activity>>(`/v1/activities/${id}`, data);

/** 删除活动 */
export const deleteActivity = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/v1/activities/${id}`);

/** 获取活动列表（带筛选分页） */
export const getActivityList = (params: ActivityQueryRequest) =>
  apiClient.get<ApiResponse<ActivityPageResponse>>('/v1/activities/list', { params });

/** 发布活动（状态变更） */
export const publishActivityStatus = (id: number) =>
  apiClient.post<ApiResponse<any>>(`/v1/activities/${id}/publish`);

/** 取消活动 */
export const cancelActivity = (id: number, reason?: string) =>
  apiClient.post<ApiResponse<any>>(`/v1/activities/${id}/cancel`, null, { params: { reason } });

/** 结束活动 */
export const endActivity = (id: number, reason?: string) =>
  apiClient.post<ApiResponse<any>>(`/v1/activities/${id}/end`, null, { params: { reason } });

/** 更新活动状态 */
export const updateActivityStatus = (id: number, newStatus: string, reason?: string) =>
  apiClient.put<ApiResponse<any>>(`/v1/activities/${id}/status`, null, { params: { newStatus, reason } });

/** 获取活动状态信息 */
export const getActivityStatus = (id: number) =>
  apiClient.get<ApiResponse<any>>(`/v1/activities/${id}/status`);

/** 分享活动 */
export const shareActivity = (id: number, shareChannel?: string) =>
  apiClient.post<ApiResponse<any>>(`/v1/activities/${id}/share`, null, { params: { shareChannel } });

/** 获取活动分享统计 */
export const getShareCount = (id: number) =>
  apiClient.get<ApiResponse<any>>(`/v1/activities/${id}/share-count`);

/** 获取我分享的活动 */
export const getMySharedActivities = () =>
  apiClient.get<ApiResponse<any[]>>('/v1/activities/my/shared');

/** 获取活动图片 */
export const getActivityImages = (activityId: number) =>
  apiClient.get<ApiResponse<any[]>>(`/v1/activities/${activityId}/images`);

/** 上传活动图片（userId从JWT获取，无需前端传递） */
export const uploadActivityImages = (activityId: number, files: File[]) => {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  return apiClient.post<ApiResponse<any[]>>(`/v1/activities/${activityId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/** 删除活动图片（userId从JWT获取，无需前端传递） */
export const deleteActivityImage = (activityId: number, imageId: number) =>
  apiClient.delete<ApiResponse<void>>(`/v1/activities/${activityId}/images/${imageId}`);
