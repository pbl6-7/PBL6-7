import request from '@/utils/request'
import type { Subscription, SubscriptionDetail, SubscriptionStatusResponse } from '@/types/subscription'

/**
 * 订阅活动
 */
export const subscribeActivity = (activityId: number) => {
  return request.post<any, { data: SubscriptionStatusResponse }>(`/activity-subscription/${activityId}`)
}

/**
 * 取消订阅
 */
export const cancelSubscription = (activityId: number) => {
  return request.delete<any, { data: SubscriptionStatusResponse }>(`/activity-subscription/${activityId}`)
}

/**
 * 获取我的订阅列表
 */
export const getMySubscriptions = () => {
  return request.get<any, { data: SubscriptionDetail[] }>('/activity-subscription/my')
}

/**
 * 检查订阅状态
 */
export const checkSubscriptionStatus = (activityId: number) => {
  return request.get<any, { data: SubscriptionStatusResponse }>(`/activity-subscription/${activityId}/status`)
}
