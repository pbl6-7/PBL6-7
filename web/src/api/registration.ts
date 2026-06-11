import apiClient from './client';
import type { RegistrationRequest, RegistrationResponse, RegistrationStatusUpdateRequest, RegistrationPageResponse } from '@/types/registration';
import type { ApiResponse } from '@/types/common';

/** 报名活动 */
export const registerForActivity = (data: RegistrationRequest) =>
  apiClient.post<ApiResponse<RegistrationResponse>>('/v1/registrations', data);

/** 获取我的报名记录 */
export const getMyRegistrations = (page = 1, size = 10) =>
  apiClient.get<ApiResponse<RegistrationPageResponse>>('/v1/registrations/my', { params: { page, size } });

/** 获取活动报名列表 */
export const getActivityRegistrations = (activityId: number, page = 1, size = 10) =>
  apiClient.get<ApiResponse<RegistrationPageResponse>>(`/v1/registrations/activity/${activityId}`, { params: { page, size } });

/** 更新报名状态 */
export const updateRegistrationStatus = (data: RegistrationStatusUpdateRequest) =>
  apiClient.put<ApiResponse<RegistrationResponse>>('/v1/registrations/status', data);

/** 取消报名 */
export const cancelRegistration = (activityId: number) =>
  apiClient.delete<ApiResponse<void>>(`/v1/registrations/activity/${activityId}`);
