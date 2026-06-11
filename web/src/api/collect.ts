import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/** 收藏活动 */
export const collectActivity = (activityId: number) =>
  apiClient.post<ApiResponse<{ activityId: number; collected: boolean }>>(`/v1/activity-collect/${activityId}`);

/** 取消收藏 */
export const uncollectActivity = (activityId: number) =>
  apiClient.delete<ApiResponse<{ activityId: number; collected: boolean }>>(`/v1/activity-collect/${activityId}`);

/** 获取我的收藏列表 */
export const getMyCollects = () =>
  apiClient.get<ApiResponse<any[]>>('/v1/activity-collect/my');

/** 检查收藏状态 */
export const checkCollectStatus = (activityId: number) =>
  apiClient.get<ApiResponse<{ collected: boolean; collectCount: number }>>(`/v1/activity-collect/${activityId}/status`);
