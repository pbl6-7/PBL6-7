import apiClient from './client';
import type { NotificationPageResponse } from '@/types/notification';
import type { ApiResponse } from '@/types/common';

/** 获取我的通知列表 */
export const getMyNotifications = (page = 1, size = 10) =>
  apiClient.get<ApiResponse<NotificationPageResponse>>('/v1/notification/my', { params: { page, size } });

/** 标记通知已读 */
export const markAsRead = (notificationId: number) =>
  apiClient.put<ApiResponse<any>>(`/v1/notification/${notificationId}/read`);

/** 获取未读通知数量 */
export const getUnreadCount = () =>
  apiClient.get<ApiResponse<any>>('/v1/notification/unread-count');
