import apiClient from './client';
import type { ApiResponse } from '@/types/common';
import type { Activity } from '@/types/activity';

/** 添加收藏 */
export const addFavorite = (activityId: number) =>
  apiClient.post<ApiResponse<{ activityId: number; collected: boolean }>>(`/activity-collect/${activityId}`);

/** 取消收藏 */
export const removeFavorite = (activityId: number) =>
  apiClient.delete<ApiResponse<{ activityId: number; collected: boolean }>>(`/activity-collect/${activityId}`);

/** 获取我的收藏列表 */
export const getFavorites = () =>
  apiClient.get<ApiResponse<Activity[]>>('/activity-collect/my');

/** 检查收藏状态 */
export const isFavorited = (activityId: number) =>
  apiClient.get<ApiResponse<{ collected: boolean }>>(`/activity-collect/${activityId}/status`);
