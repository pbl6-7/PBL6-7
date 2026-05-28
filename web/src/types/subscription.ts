/**
 * 订阅类型定义
 */

/**
 * 订阅信息
 */
export interface Subscription {
  activityId: number
  createTime: string
}

/**
 * 订阅详情信息
 */
export interface SubscriptionDetail {
  activityId: number
  activityTitle: string
  activityLocation: string
  activityStartTime: string
  createTime: string
}

/**
 * 订阅状态响应
 */
export interface SubscriptionStatusResponse {
  subscribed: boolean
  subscriptionCount: number
}

/**
 * 订阅响应
 */
export interface SubscriptionResponse {
  activityId: number
  subscribed: boolean
  subscriptionCount: number
}
