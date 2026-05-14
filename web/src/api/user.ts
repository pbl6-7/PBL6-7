import request from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserInfo, SecurityQuestion, ChangePasswordRequest, UpdateProfileRequest } from '@/types/user'

export const userLogin = (data: LoginRequest) => {
  return request.post<any, { data: LoginResponse }>('/users/login', data)
}

export const userRegister = (data: RegisterRequest) => {
  return request.post<any, { data: null }>('/users/register', data)
}

export const getUserInfo = (id: number) => {
  return request.get<any, { data: UserInfo }>(`/users/${id}`)
}

export const getUserProfile = () => {
  return request.get<any, { data: UserInfo }>('/users/profile')
}

export const updateProfile = (data: UpdateProfileRequest) => {
  return request.put<any, { data: null }>('/users/profile', data)
}

export const changePassword = (data: ChangePasswordRequest) => {
  return request.put<any, { data: null }>('/users/password', data)
}

export const getSecurityQuestions = () => {
  return request.get<any, { data: SecurityQuestion[] }>('/users/security/questions')
}

export const getSecurityQuestionByUsername = (username: string) => {
  return request.get<any, { data: SecurityQuestion }>(`/users/security/username/${username}`)
}

export const verifySecurityAnswer = (data: { username: string; securityAnswer: string }) => {
  return request.post<any, { data: null }>('/users/security/verify', data)
}

export const resetPassword = (data: { username: string; securityAnswer: string; newPassword: string }) => {
  return request.post<any, { data: null }>('/users/security/reset-password', data)
}
