import request from '@/utils/request'
import type { Comment, CommentRequest } from '@/types/comment'

export const publishComment = (activityId: number, data: CommentRequest) => {
  return request.post<any, { data: Comment }>(`/activities/${activityId}/comments`, data)
}

export const getCommentList = (activityId: number, page = 1, size = 20) => {
  return request.get<any, { data: Comment[] }>(`/activities/${activityId}/comments`, {
    params: { page, size }
  })
}

export const deleteComment = (commentId: number) => {
  return request.delete<any, { data: null }>(`/comments/${commentId}`)
}
