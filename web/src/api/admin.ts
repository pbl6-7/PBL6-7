import request from '@/utils/request'
import type { Activity } from '@/types/activity'

export interface UserItem {
  id: number
  username: string
  realName: string
  role: string
  contact: string
  createdAt: string
  updatedAt: string
}

export interface UserQueryParams {
  keyword?: string
  role?: string
  page?: number
  size?: number
}

export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface UpdateRoleRequest {
  role: string
}

export const getUserList = (params: UserQueryParams) => {
  return request.get<any, { data: PageResponse<UserItem> }>('/admin/users', { params })
}

export const getUserDetail = (id: number) => {
  return request.get<any, { data: UserItem }>(`/admin/users/${id}`)
}

export const updateUserRole = (id: number, role: string) => {
  return request.put<any, { data: null }>(`/admin/users/${id}/role`, { role })
}

export const getPendingActivities = () => {
  return request.get<any, { data: Activity[] }>('/admin/activities/pending')
}

export const getActivitiesByApprovalStatus = (status: string) => {
  return request.get<any, { data: Activity[] }>(`/admin/activities/approval-status/${status}`)
}

export const approveActivity = (id: number) => {
  return request.put<any, { data: Activity }>(`/admin/activities/${id}/approve`)
}

export const rejectActivity = (id: number, reason: string) => {
  return request.put<any, { data: Activity }>(`/admin/activities/${id}/reject`, { reason })
}

export const getApprovalStatistics = () => {
  return request.get<any, { data: any }>('/admin/activities/statistics')
}
