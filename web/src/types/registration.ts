/** 报名状态 */
export type RegistrationStatus = 'pending' | 'confirmed' | 'cancelled';

/** 报名信息 */
export interface Registration {
  id: number;
  userId: number;
  activityId: number;
  status: RegistrationStatus;
  createdAt: string;
}

/** 报名请求 */
export interface RegistrationRequest {
  activityId: number;
}

/** 报名响应 */
export interface RegistrationResponse {
  id: number;
  activityId: number;
  activityTitle: string;
  activityStartTime: string;
  activityEndTime: string;
  activityLocation: string;
  userId: number;
  userName: string;
  registrationTime: string;
  status: string;
}

/** 报名状态更新请求 */
export interface RegistrationStatusUpdateRequest {
  registrationId: number;
  status: string;
}

/** 报名分页响应 */
export interface RegistrationPageResponse {
  list: RegistrationResponse[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}
