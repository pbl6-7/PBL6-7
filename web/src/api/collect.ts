import request from '@/utils/request'

export const collectActivity = (activityId: number) => {
  return request.post<any, { data: { activityId: number; collected: boolean } }>(`/activity-collect/${activityId}`)
}

export const cancelCollect = (activityId: number) => {
  return request.delete<any, { data: { activityId: number; collected: boolean } }>(`/activity-collect/${activityId}`)
}

export const getMyCollections = () => {
  return request.get<any, { data: any[] }>('/activity-collect/my')
}

export const checkCollectStatus = (activityId: number) => {
  return request.get<any, { data: { collected: boolean; collectCount: number } }>(`/activity-collect/${activityId}/status`)
}
