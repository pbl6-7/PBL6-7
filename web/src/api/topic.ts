import request from '@/utils/request'

export interface TopicResponse {
  id: number
  activityId: number
  title: string
  creatorId: number
  creatorName: string
  createdAt: string
  updatedAt: string
}

export interface TopicCreateRequest {
  activityId: number
  title: string
}

export interface TopicUpdateRequest {
  title: string
}

export const createTopic = (data: TopicCreateRequest) => {
  return request.post<any, { data: TopicResponse }>('/topics', data)
}

export const getTopicsByActivityId = (activityId: number) => {
  return request.get<any, { data: TopicResponse[] }>(`/topics/activity/${activityId}`)
}

export const getTopicById = (id: number) => {
  return request.get<any, { data: TopicResponse }>(`/topics/${id}`)
}

export const updateTopic = (id: number, data: TopicUpdateRequest) => {
  return request.put<any, { data: TopicResponse }>(`/topics/${id}`, data)
}

export const deleteTopic = (id: number) => {
  return request.delete<any, { data: null }>(`/topics/${id}`)
}
