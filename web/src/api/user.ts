import apiClient from './client';
import type { LoginRequest, LoginResponse, RegisterRequest, ChangePasswordRequest, UpdateProfileRequest, SecurityQuestion, SetSecurityRequest, VerifySecurityRequest, ResetPasswordRequest } from '@/types/user';
import type { User } from '@/types/user';
import type { ApiResponse } from '@/types/common';

/** 用户登录 */
export const login = (data: LoginRequest) =>
  apiClient.post<ApiResponse<LoginResponse>>('/users/login', data);

/** 用户注册 */
export const register = (data: RegisterRequest) =>
  apiClient.post<ApiResponse<void>>('/users/register', data);

/** 获取当前用户信息 */
export const getProfile = () =>
  apiClient.get<ApiResponse<User>>('/users/profile');

/** 修改个人资料 */
export const updateProfile = (data: UpdateProfileRequest) =>
  apiClient.put<ApiResponse<void>>('/users/profile', data);

/** 修改密码 - 后端使用 PUT /users/password */
export const changePassword = (data: ChangePasswordRequest) =>
  apiClient.put<ApiResponse<void>>('/users/password', data);

/** 获取用户信息 */
export const getUserById = (id: number) =>
  apiClient.get<ApiResponse<User>>(`/users/${id}`);

/** 上传头像 */
export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return apiClient.post<ApiResponse<any>>('/users/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/** 获取用户头像 */
export const getUserAvatar = (id: number) =>
  apiClient.get<ApiResponse<any>>(`/users/${id}/avatar`);

/** 获取密保问题列表 */
export const getSecurityQuestions = () =>
  apiClient.get<ApiResponse<SecurityQuestion[]>>('/users/security/questions');

/** 获取当前用户的密保问题 */
export const getUserSecurityQuestion = (userId: number) =>
  apiClient.get<ApiResponse<SecurityQuestion>>(`/users/security/user/${userId}`);

/** 根据用户名获取密保问题 */
export const getSecurityQuestionByUsername = (username: string) =>
  apiClient.get<ApiResponse<SecurityQuestion>>(`/users/security/username/${username}`);

/** 设置密保 */
export const setSecurity = (data: SetSecurityRequest) =>
  apiClient.post<ApiResponse<void>>('/users/security/set', data);

/** 验证密保 - 后端返回验证结果 Map */
export const verifySecurity = (data: VerifySecurityRequest) =>
  apiClient.post<ApiResponse<any>>('/users/security/verify', data);

/** 重置密码 */
export const resetPassword = (data: ResetPasswordRequest) =>
  apiClient.post<ApiResponse<void>>('/users/security/reset-password', data);
