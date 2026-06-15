import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/** 订阅活动 */
export const subscribeActivity = (activityId: number) =>
  apiClient.post<ApiResponse<{ activityId: number; subscribed: boolean; subscriptionCount: number }>>(`/activity-subscription/${activityId}`);

/** 取消订阅 */
export const unsubscribeActivity = (activityId: number) =>
  apiClient.delete<ApiResponse<{ activityId: number; subscribed: boolean; subscriptionCount: number }>>(`/activity-subscription/${activityId}`);

/** 获取我的订阅列表 */
export const getMySubscriptions = () =>
  apiClient.get<ApiResponse<any[]>>('/activity-subscription/my');

/** 检查订阅状态 */
export const checkSubscriptionStatus = (activityId: number) =>
  apiClient.get<ApiResponse<{ subscribed: boolean; subscriptionCount: number }>>(`/activity-subscription/${activityId}/status`);
