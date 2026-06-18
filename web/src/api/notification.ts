import apiClient from './client';
import type { NotificationPageResponse } from '@/types/notification';
import type { ApiResponse } from '@/types/common';

/**
 * 获取通知列表
 * @param page - 页码，默认1
 * @param size - 每页数量，默认10
 */
export const getNotifications = (page = 1, size = 10) =>
  apiClient.get<ApiResponse<NotificationPageResponse>>('/notifications', { params: { page, size } });

/**
 * 获取未读通知数量
 */
export const getUnreadCount = () =>
  apiClient.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count');

/**
 * 标记通知已读
 * @param id - 通知ID
 */
export const markAsRead = (id: number) =>
  apiClient.patch<ApiResponse<void>>(`/notifications/${id}/read`);

/**
 * 标记全部通知已读
 */
export const markAllAsRead = () =>
  apiClient.patch<ApiResponse<void>>('/notifications/read-all');

/**
 * 删除通知
 * @param id - 通知ID
 */
export const deleteNotification = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/notifications/${id}`);
