/**
 * 通知类型定义
 */

/**
 * 通知信息
 */
export interface Notification {
  id: number
  activityId: number
  activityTitle: string
  type: string
  content: string
  isRead: boolean
  createTime: string
}

/**
 * 通知分页响应
 */
export interface NotificationPageResponse {
  records: Notification[]
  total: number
  pages: number
  current: number
}

/**
 * 未读数量响应
 */
export interface UnreadCountResponse {
  unreadCount: number
}

/**
 * 通知类型常量
 */
export const NotificationType = {
  SUBSCRIPTION_STATUS: 'SUBSCRIPTION_STATUS',
  ACTIVITY_UPDATE: 'ACTIVITY_UPDATE',
  ACTIVITY_START: 'ACTIVITY_START',
  ACTIVITY_END: 'ACTIVITY_END'
} as const

export type NotificationTypeValue = typeof NotificationType[keyof typeof NotificationType]
