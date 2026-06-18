import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/** 标签响应接口 */
export interface TagResponse {
  /** 标签ID */
  id: number;
  /** 标签名称 */
  name: string;
  /** 标签颜色 */
  color: string;
  /** 标签类型 */
  type: string;
}

/** 标签创建请求接口 */
export interface TagCreateRequest {
  /** 标签名称 */
  name: string;
  /** 标签颜色 */
  color?: string;
  /** 标签类型 */
  type?: string;
}

/** 活动标签设置请求接口 */
export interface ActivityTagRequest {
  /** 活动ID */
  activityId: number;
  /** 标签ID列表 */
  tagIds: number[];
}

/** 获取所有标签 */
export const getAllTags = () =>
  apiClient.get<ApiResponse<TagResponse[]>>('/tags');

/** 根据ID获取标签 */
export const getTagById = (id: number) =>
  apiClient.get<ApiResponse<TagResponse>>(`/tags/${id}`);

/** 创建标签 */
export const createTag = (data: TagCreateRequest) =>
  apiClient.post<ApiResponse<TagResponse>>('/tags', data);

/** 更新标签 */
export const updateTag = (id: number, data: TagCreateRequest) =>
  apiClient.put<ApiResponse<TagResponse>>(`/tags/${id}`, data);

/** 删除标签 */
export const deleteTag = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/tags/${id}`);

/** 根据活动ID获取标签 */
export const getTagsByActivityId = (activityId: number) =>
  apiClient.get<ApiResponse<TagResponse[]>>(`/tags/activity/${activityId}`);

/** 设置活动标签 */
export const setActivityTags = (data: ActivityTagRequest) =>
  apiClient.post<ApiResponse<void>>('/tags/activity', data);
