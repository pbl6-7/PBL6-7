import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/** 活动类型响应接口 */
export interface ActivityTypeResponse {
  /** 活动类型ID */
  id: number;
  /** 活动类型名称 */
  name: string;
  /** 活动类型描述 */
  description: string;
  /** 活动类型图标 */
  icon: string;
  /** 创建时间 */
  createdAt: string;
}

/** 活动类型创建请求接口 */
export interface ActivityTypeCreateRequest {
  /** 活动类型名称 */
  name: string;
  /** 活动类型描述 */
  description?: string;
  /** 活动类型图标 */
  icon?: string;
}

/** 创建活动类型 */
export const createActivityType = (data: ActivityTypeCreateRequest) =>
  apiClient.post<ApiResponse<ActivityTypeResponse>>('/activity-types', data);

/** 获取所有活动类型 */
export const getAllActivityTypes = () =>
  apiClient.get<ApiResponse<ActivityTypeResponse[]>>('/activity-types');

/** 根据ID获取活动类型 */
export const getActivityTypeById = (id: number) =>
  apiClient.get<ApiResponse<ActivityTypeResponse>>(`/activity-types/${id}`);

/** 更新活动类型 */
export const updateActivityType = (id: number, data: ActivityTypeCreateRequest) =>
  apiClient.put<ApiResponse<ActivityTypeResponse>>(`/activity-types/${id}`, data);

/** 删除活动类型 */
export const deleteActivityType = (id: number) =>
  apiClient.delete<ApiResponse<void>>(`/activity-types/${id}`);
