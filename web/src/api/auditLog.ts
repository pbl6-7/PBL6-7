import apiClient from './client';
import type { ApiResponse } from '@/types/common';

/** 审计日志接口 */
export interface AuditLog {
  /** 日志ID */
  id: number;
  /** 操作用户ID */
  userId: number;
  /** 目标用户ID */
  targetUserId?: number;
  /** 操作用户名 */
  username: string;
  /** 操作类型 */
  operation: string;
  /** 资源类型 */
  resourceType: string;
  /** 资源ID */
  resourceId: string;
  /** 请求方法 */
  requestMethod?: string;
  /** 请求路径 */
  requestPath?: string;
  /** 请求参数 */
  requestParams?: string;
  /** 响应状态码 */
  responseStatus?: number;
  /** 操作详情（响应消息） */
  responseMessage: string;
  /** 客户端IP地址 */
  clientIp: string;
  /** User-Agent */
  userAgent?: string;
  /** 执行时间(ms) */
  executionTime?: number;
  /** 创建时间 */
  createdAt: string;
}

/** 获取审计日志列表 */
export const getAuditLogs = (params: {
  /** 用户ID */
  userId?: number;
  /** 操作类型 */
  operation?: string;
  /** 资源类型 */
  resourceType?: string;
  /** 开始时间 */
  startTime?: string;
  /** 结束时间 */
  endTime?: string;
  /** 页码 */
  page?: number;
  /** 每页数量 */
  size?: number;
}) =>
  apiClient.get<ApiResponse<any>>('/audit-logs', { params });

/** 获取最近审计日志 */
export const getRecentAuditLogs = (limit: number = 20) =>
  apiClient.get<ApiResponse<AuditLog[]>>('/audit-logs/recent', { params: { limit } });

/** 获取我的审计日志 */
export const getMyAuditLogs = () =>
  apiClient.get<ApiResponse<AuditLog[]>>('/audit-logs/my');

/** 获取审计日志统计 */
export const getAuditLogStats = () =>
  apiClient.get<ApiResponse<any>>('/audit-logs/stats');
