import request from '@/utils/request'
import type { Notification, NotificationPageResponse, UnreadCountResponse } from '@/types/notification'

/**
 * 获取我的通知列表
 */
export const getMyNotifications = (page: number = 1, size: number = 10) => {
  return request.get<any, { data: NotificationPageResponse }>(`/notification/my?page=${page}&size=${size}`)
}

/**
 * 标记通知已读
 */
export const markAsRead = (notificationId: number) => {
  return request.put<any, { data: { notificationId: number; isRead: boolean } }>(`/notification/${notificationId}/read`)
}

/**
 * 获取未读通知数量
 */
export const getUnreadCount = () => {
  return request.get<any, { data: UnreadCountResponse }>('/notification/unread-count')
}
