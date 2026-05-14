export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  userId: number
  username: string
  realName: string
  role: string
  token: string
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  contact: string
  role: string
  createdAt: string
}

export interface RegisterRequest {
  username: string
  password: string
  realName: string
  contact: string
  securityQuestionId: number
  securityAnswer: string
}

export interface SecurityQuestion {
  id: number
  question: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface UpdateProfileRequest {
  realName: string
  contact: string
}
