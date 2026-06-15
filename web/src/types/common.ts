// API 响应接口
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  requestId?: string;
}

// 分页响应接口
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

// 分页请求接口
export interface PageRequest {
  page?: number;
  size?: number;
}

// 通用 ID 请求接口
export interface IdRequest {
  id: number;
}

// 通用批量 ID 请求接口
export interface BatchIdRequest {
  ids: number[];
}

// 通用删除请求接口
export interface DeleteRequest {
  ids?: number[];
}

// 通用状态更新请求接口
export interface StatusUpdateRequest<T = string> {
  id: number;
  status: T;
}

// 通用审核请求接口
export interface ApprovalRequest {
  reason?: string;
}

// 通用批量操作请求接口
export interface BatchOperationRequest {
  ids: number[];
}

// 通用批量操作响应接口
export interface BatchOperationResponse {
  successCount: number;
  failedCount: number;
  failedIds?: number[];
  failureReasons?: string[];
}

// 通用搜索请求接口
export interface SearchRequest {
  keyword?: string;
  page?: number;
  size?: number;
}

// 通用日期范围请求接口
export interface DateRangeRequest {
  startDate?: string;
  endDate?: string;
}

// 通用排序请求接口
export interface SortRequest {
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

// 通用列表响应接口
export interface ListResponse<T> {
  list: T[];
  total: number;
}

// 通用 ID 对象接口
export interface IdObject {
  id: number;
}

// 通用创建时间接口
export interface Creatable {
  createdAt: string;
}

// 通用更新时间接口
export interface Updatable {
  updatedAt: string;
}

// 通用创建和更新时间接口
export interface Timestamps extends Creatable, Updatable {}

// 通用软删除接口
export interface SoftDeletable {
  deletedAt?: string;
  deleted?: boolean;
}

// 通用状态接口
export interface Statusable {
  status: string;
}

// 通用审核状态接口
export interface Approvable {
  approvalStatus: 'pending' | 'approved' | 'rejected';
}

// 通用分页和排序请求
export interface PageSortRequest extends PageRequest, SortRequest {}

// 通用搜索和分页请求
export interface SearchPageRequest extends SearchRequest, PageSortRequest {}

// 通用日期范围和分页请求
export interface DateRangePageRequest extends DateRangeRequest, PageSortRequest {}

// 导出所有类型
export * from './common';
