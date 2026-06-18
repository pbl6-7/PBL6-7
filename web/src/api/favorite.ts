import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/**
 * 收藏相关 API
 * 后端路径: FavoriteController - /api/v1/activities/{id}/favorite
 */

/** 添加收藏 */
export const addFavorite = (activityId: number) =>
  apiClient.post<ApiResponse<{ activityId: number; collected: boolean }>>(`/activities/${activityId}/favorite`);

/** 取消收藏 */
export const removeFavorite = (activityId: number) =>
  apiClient.delete<ApiResponse<{ activityId: number; collected: boolean }>>(`/activities/${activityId}/favorite`);

/** 获取收藏列表 */
export const getFavorites = () =>
  apiClient.get<ApiResponse<any[]>>('/users/favorites');

/** 检查收藏状态 */
export const isFavorited = (activityId: number) =>
  apiClient.get<ApiResponse<{ favorited: boolean; collectCount: number }>>(`/activities/${activityId}/favorite/status`);
