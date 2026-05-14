import request from '@/utils/request'
import type { Registration, RegistrationRequest, RegistrationStatusUpdateRequest, RegistrationPageResponse } from '@/types/registration'

export const registerActivity = (data: RegistrationRequest) => {
  return request.post<any, { data: Registration }>('/registrations', data)
}

export const getMyRegistrations = (page = 1, size = 10) => {
  return request.get<any, { data: RegistrationPageResponse }>('/registrations/my', {
    params: { page, size }
  })
}

export const getActivityRegistrations = (activityId: number, page = 1, size = 10) => {
  return request.get<any, { data: RegistrationPageResponse }>(`/registrations/activity/${activityId}`, {
    params: { page, size }
  })
}

export const updateRegistrationStatus = (data: RegistrationStatusUpdateRequest) => {
  return request.put<any, { data: Registration }>('/registrations/status', data)
}

export const cancelRegistration = (activityId: number) => {
  return request.delete<any, { data: null }>(`/registrations/activity/${activityId}`)
}
