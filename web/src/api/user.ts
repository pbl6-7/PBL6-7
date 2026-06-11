import apiClient from './client';
import type { LoginRequest, LoginResponse, RegisterRequest, ChangePasswordRequest, UpdateProfileRequest, SecurityQuestion, SetSecurityRequest, VerifySecurityRequest, ResetPasswordRequest } from '@/types/user';
import type { User } from '@/types/user';
import type { ApiResponse } from '@/types/common';

/** 用户登录 */
export const login = (data: LoginRequest) =>
  apiClient.post<ApiResponse<LoginResponse>>('/v1/users/login', data);

/** 用户注册 */
export const register = (data: RegisterRequest) =>
  apiClient.post<ApiResponse<void>>('/v1/users/register', data);

/** 获取当前用户信息 */
export const getProfile = () =>
  apiClient.get<ApiResponse<User>>('/v1/users/profile');

/** 修改个人资料 */
export const updateProfile = (data: UpdateProfileRequest) =>
  apiClient.put<ApiResponse<void>>('/v1/users/profile', data);

/** 修改密码 */
export const changePassword = (data: ChangePasswordRequest) =>
  apiClient.put<ApiResponse<void>>('/v1/users/password', data);

/** 获取用户信息 */
export const getUserById = (id: number) =>
  apiClient.get<ApiResponse<User>>(`/v1/users/${id}`);

/** 上传头像 */
export const uploadAvatar = (userId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('userId', userId.toString());
  return apiClient.post<ApiResponse<any>>('/v1/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/** 获取密保问题列表 */
export const getSecurityQuestions = () =>
  apiClient.get<ApiResponse<SecurityQuestion[]>>('/v1/users/security/questions');

/** 根据用户名获取密保问题 */
export const getSecurityQuestionByUsername = (username: string) =>
  apiClient.get<ApiResponse<SecurityQuestion>>(`/v1/users/security/username/${username}`);

/** 设置密保 */
export const setSecurity = (data: SetSecurityRequest) =>
  apiClient.post<ApiResponse<void>>('/v1/users/security/set', data);

/** 验证密保 */
export const verifySecurity = (data: VerifySecurityRequest) =>
  apiClient.post<ApiResponse<void>>('/v1/users/security/verify', data);

/** 重置密码 */
export const resetPassword = (data: ResetPasswordRequest) =>
  apiClient.post<ApiResponse<void>>('/v1/users/security/reset-password', data);
