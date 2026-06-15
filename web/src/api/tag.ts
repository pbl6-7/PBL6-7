import request from '@/utils/request'

export interface TagResponse {
  id: number
  name: string
  color: string
  type: string
}

export interface TagCreateRequest {
  name: string
  color?: string
  type?: string
}

export interface ActivityTagRequest {
  activityId: number
  tagIds: number[]
}

export const getAllTags = () => {
  return request.get<any, { data: TagResponse[] }>('/tags')
}

export const getTagById = (id: number) => {
  return request.get<any, { data: TagResponse }>(`/tags/${id}`)
}

export const createTag = (data: TagCreateRequest) => {
  return request.post<any, { data: TagResponse }>('/tags', data)
}

export const deleteTag = (id: number) => {
  return request.delete<any, { data: null }>(`/tags/${id}`)
}

export const getTagsByActivityId = (activityId: number) => {
  return request.get<any, { data: TagResponse[] }>(`/tags/activity/${activityId}`)
}

export const setActivityTags = (data: ActivityTagRequest) => {
  return request.post<any, { data: null }>('/tags/activity', data)
}
