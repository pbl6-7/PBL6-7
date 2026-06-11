import apiClient from './client';
import type { CommentRequest, CommentResponse } from '@/types/comment';
import type { ApiResponse } from '@/types/common';

/** 发布评论 */
export const publishComment = (activityId: number, data: CommentRequest) =>
  apiClient.post<ApiResponse<CommentResponse>>(`/v1/activities/${activityId}/comments`, data);

/** 获取评论列表 */
export const getComments = (activityId: number, page?: number, size?: number) =>
  apiClient.get<ApiResponse<CommentResponse[]>>(`/v1/activities/${activityId}/comments`, { params: { page, size } });

/** 删除评论 */
export const deleteComment = (commentId: number) =>
  apiClient.delete<ApiResponse<void>>(`/v1/comments/${commentId}`);
