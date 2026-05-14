import request from '@/utils/request'
import type { Activity, ActivityPublishRequest, ActivityQueryRequest, PageResponse } from '@/types/activity'

export const publishActivity = (data: ActivityPublishRequest) => {
  return request.post<any, { data: Activity }>('/activities', data)
}

export const getActivityDetail = (id: number) => {
  return request.get<any, { data: Activity }>(`/activities/${id}`)
}

export const getMyActivities = () => {
  return request.get<any, { data: Activity[] }>('/activities/my')
}

export const updateActivity = (id: number, data: ActivityPublishRequest) => {
  return request.put<any, { data: Activity }>(`/activities/${id}`, data)
}

export const deleteActivity = (id: number) => {
  return request.delete<any, { data: null }>(`/activities/${id}`)
}

export const getActivityList = (params: ActivityQueryRequest) => {
  return request.get<any, { data: PageResponse<Activity> }>('/activities/list', { params })
}
