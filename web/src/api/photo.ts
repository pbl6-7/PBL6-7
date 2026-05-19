import request from '@/utils/request'
import type { ActivityPhoto } from '@/types/activity'

export const uploadPhoto = (activityId: number, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, { data: ActivityPhoto }>(
    `/activities/${activityId}/photos`,
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' }
    }
  )
}

export const uploadPhotos = (activityId: number, files: File[]) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post<any, { data: ActivityPhoto[] }>(
    `/activities/${activityId}/photos/batch`,
    formData,
    {
      headers: { 'Content-Type': 'multipart/form-data' }
    }
  )
}

export const getActivityPhotos = (activityId: number) => {
  return request.get<any, { data: ActivityPhoto[] }>(`/activities/${activityId}/photos`)
}

export const deletePhoto = (photoId: number) => {
  return request.delete<any, { data: null }>(`/photos/${photoId}`)
}