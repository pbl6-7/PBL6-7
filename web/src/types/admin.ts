// 用户角色
export type UserRole = 'USER' | 'ADMIN' | 'PUBLISHER';

// 用户状态
export type UserStatus = 'enabled' | 'disabled';

// 用户信息
export interface User {
  id: number;
  username: string;
  realName: string;
  role: UserRole;
  status: UserStatus;
  avatar: string | null;
  contact: string | null;
  securityQuestionId: number | null;
  securityAnswer: string | null;
  createdAt: string;
  updatedAt: string;
}

// 登录响应
export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  realName: string;
  role: string;
}

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
}

// 注册请求
export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  securityQuestionId: number;
  securityAnswer: string;
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// 更新个人资料请求
export interface UpdateProfileRequest {
  realName?: string;
  contact?: string;
}

// 密保问题
export interface SecurityQuestion {
  questionId: number;
  question: string;
}

// 设置密保请求
export interface SetSecurityRequest {
  securityQuestionId: number;
  securityAnswer: string;
  password: string;
}

// 验证密保请求
export interface VerifySecurityRequest {
  username: string;
  securityAnswer: string;
}

// 重置密码请求
export interface ResetPasswordRequest {
  username: string;
  securityAnswer: string;
  newPassword: string;
}

// 用户分页请求
export interface UserPageRequest {
  page?: number;
  size?: number;
  role?: string;
  keyword?: string;
}

// 用户分页响应
export interface UserPageResponse {
  list: UserResponse[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

// 用户响应
export interface UserResponse {
  id: number;
  username: string;
  realName: string;
  role: UserRole;
  status: UserStatus;
  avatar: string | null;
  contact: string | null;
  createdAt: string;
}

// 更新角色请求
export interface UpdateRoleRequest {
  role: UserRole;
}

// 批量操作请求
export interface BatchOperationRequest {
  ids: number[];
}

// 批量操作响应
export interface BatchOperationResponse {
  successCount: number;
  failedCount: number;
  failedIds?: number[];
  failureReasons?: string[];
}

// 概览统计
export interface OverviewStatistics {
  totalUsers: number;
  totalActivities: number;
  totalRegistrations: number;
  todayActivities: number;
  todayRegistrations: number;
  newActivities7Days: number;
  newRegistrations7Days: number;
  newUsers7Days: number;
  pendingActivities: number;
  newActivities30Days: number;
  newUsers30Days: number;
  newRegistrations30Days: number;
  activeUsers: number;
  systemHealthScore: number;
  updateTime: string;
}

// 活动统计
export interface ActivityStatistics {
  totalActivities: number;
  publishedActivities: number;
  draftActivities: number;
  cancelledActivities: number;
  endedActivities: number;
  totalViews: number;
  totalParticipants: number;
  statusDistribution: Record<string, number>;
  approvalStatusDistribution: Record<string, number>;
  typeDistribution: Record<string, number>;
  averageRegistrations: number;
  averageViewCount: number;
}

// 用户统计
export interface UserStatistics {
  totalUsers: number;
  activeUsers: number;
  newUsersToday: number;
  totalRegistrations: number;
  roleDistribution: Record<string, number>;
  inactiveUsers: number;
  newUsers7Days: number;
  newUsers30Days: number;
  averageRegistrationsPerUser: number;
}

// 报名统计
export interface RegistrationStatistics {
  totalRegistrations: number;
  pendingRegistrations: number;
  approvedRegistrations: number;
  rejectedRegistrations: number;
  registrations7Days: number;
  registrations30Days: number;
  confirmationRate: number;
  averageRegistrationsPerActivity: number;
}

// 趋势数据
export interface TrendData {
  date: string;
  value: number;
}

// 热门活动
export interface HotActivity {
  id: number;
  title: string;
  registrationCount: number;
  viewCount: number;
  collectCount: number;
}

// 敏感词
export interface SensitiveWord {
  id: number;
  word: string;
  level: number;
  createdAt: string;
}

// 敏感词创建请求
export interface SensitiveWordCreateRequest {
  word: string;
  level?: number;
  type?: string;
}

// 敏感词更新请求
export interface SensitiveWordUpdateRequest {
  word: string;
  level?: number;
  type?: string;
}

// 敏感词批量请求
export interface SensitiveWordBatchRequest {
  words: string[];
  level?: number;
}

// 敏感词分页响应
export interface SensitiveWordPageResponse {
  list: SensitiveWord[];
  total: number;
  page: number;
  size: number;
}

// 敏感词统计
export interface SensitiveWordStatistics {
  total: number;
  level1Count: number;
  level2Count: number;
  level3Count: number;
}

// 敏感词检查结果
export interface SensitiveWordCheckResult {
  hasSensitive: boolean;
  sensitiveWords: string[];
}

// 登录锁定
export interface LoginLock {
  username: string;
  lockTime: string;
  lockReason: string;
  lockCount: number;
}

// 登录锁定分页响应
export interface LoginLockPageResponse {
  list: LoginLock[];
  total: number;
  page: number;
  size: number;
}
