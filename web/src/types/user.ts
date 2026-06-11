/** 用户角色枚举 */
export type UserRole = 'user' | 'admin' | 'publisher';

/** 用户状态 */
export type UserStatus = 'enabled' | 'disabled';

/** 用户信息接口 */
export interface User {
  /** 用户ID */
  id: number;
  /** 用户名 */
  username: string;
  /** 真实姓名 */
  realName: string;
  /** 角色 */
  role: UserRole;
  /** 状态 */
  status: UserStatus;
  /** 头像URL */
  avatar: string | null;
  /** 联系方式 */
  contact: string | null;
  /** 密保问题ID */
  securityQuestionId: number | null;
  /** 密保答案 */
  securityAnswer: string | null;
  /** 创建时间 */
  createdAt: string;
  /** 更新时间 */
  updatedAt: string;
}

/** 登录响应接口 */
export interface LoginResponse {
  /** JWT Token */
  token: string;
  /** 用户ID */
  userId: number;
  /** 用户名 */
  username: string;
  /** 真实姓名 */
  realName: string;
  /** 角色 */
  role: string;
}

/** 登录请求接口 */
export interface LoginRequest {
  /** 用户名 */
  username: string;
  /** 密码 */
  password: string;
}

/** 注册请求接口 */
export interface RegisterRequest {
  /** 用户名 */
  username: string;
  /** 密码 */
  password: string;
  /** 真实姓名 */
  realName: string;
  /** 密保问题ID */
  securityQuestionId: number;
  /** 密保答案 */
  securityAnswer: string;
}

/** 修改密码请求接口 */
export interface ChangePasswordRequest {
  /** 旧密码 */
  oldPassword: string;
  /** 新密码 */
  newPassword: string;
}

/** 更新个人资料请求接口 */
export interface UpdateProfileRequest {
  /** 真实姓名 */
  realName?: string;
  /** 联系方式 */
  contact?: string;
}

/** 密保问题接口 */
export interface SecurityQuestion {
  /** 问题ID */
  questionId: number;
  /** 问题内容 */
  question: string;
}

/** 设置密保请求接口 */
export interface SetSecurityRequest {
  /** 密保问题ID */
  securityQuestionId: number;
  /** 密保答案 */
  securityAnswer: string;
  /** 当前密码 */
  password: string;
}

/** 验证密保请求接口 */
export interface VerifySecurityRequest {
  /** 用户名 */
  username: string;
  /** 密保问题ID */
  securityQuestionId: number;
  /** 密保答案 */
  securityAnswer: string;
}

/** 重置密码请求接口 */
export interface ResetPasswordRequest {
  /** 用户名 */
  username: string;
  /** 密保问题ID */
  securityQuestionId: number;
  /** 密保答案 */
  securityAnswer: string;
  /** 新密码 */
  newPassword: string;
}
