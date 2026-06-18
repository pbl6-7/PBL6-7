import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/** 话题响应接口 */
export interface TopicResponse {
  /** 话题ID */
  id: number;
  /** 活动ID */
  activityId: number;
  /** 话题标题 */
  title: string;
  /** 创建者ID */
  creatorId: number;
  /** 创建者名称 */
  creatorName: string;
  /** 创建时间 */
  createdAt: string;
  /** 更新时间 */
  updatedAt: string;
}

/** 话题创建请求接口 */
export interface TopicCreateRequest {
  /** 活动ID */
  activityId: number;
  /** 话题标题 */
  title: string;
}

/** 话题更新请求接口 */
export interface TopicUpdateRequest {
  /** 话题标题 */
  title: string;
}

/** 创建话题 */
export const createTopic = (data: TopicCreateRequest) =>
  apiClient.post<ApiResponse<TopicResponse>>('/topics', data);

/** 根据活动ID获取话题列表 */
export const getTopicsByActivityId = (activityId: number) =>
  apiClient.get<ApiResponse<TopicResponse[]>>(`/topics/activity/${activityId}`);

/** 根据ID获取话题 */
export const getTopicById = (id: number) =>
  apiClient.get<ApiResponse<TopicResponse>>(`/topics/${id}`);

/** 更新话题 */
export const updateTopic = (id: number, data: TopicUpdateRequest) =>
  apiClient.put<ApiResponse<TopicResponse>>(`/topics/${id}`, data);

/** 删除话题 */
export const deleteTopic = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/topics/${id}`);

/** 获取所有话题 */
export const getAllTopics = () =>
  apiClient.get<ApiResponse<TopicResponse[]>>('/topics');
