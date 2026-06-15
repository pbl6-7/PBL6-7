/** 活动状态 */
export type ActivityStatus = 'draft' | 'published' | 'cancelled' | 'ended';

/** 审核状态 */
export type ApprovalStatus = 'pending' | 'approved' | 'rejected';

/** 活动信息 */
export interface Activity {
  id: number;
  title: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  publisherId: number;
  publisherName: string;
  status: ActivityStatus;
  approvalStatus: ApprovalStatus;
  typeId: number;
  activityTypeName: string;
  maxParticipants: number;
  tags: TagResponse[];
  createdAt: string;
  updatedAt: string;
}

/** 活动发布请求 */
export interface ActivityPublishRequest {
  title: string;
  startTime: string;
  endTime: string;
  location: string;
  description?: string;
  typeId: number;
  maxParticipants?: number;
  tags?: string[];
  imageIds?: number[];
}

/** 活动查询请求 */
export interface ActivityQueryRequest {
  page?: number;
  size?: number;
  keyword?: string;
  typeId?: number;
  status?: string;
  approvalStatus?: string;
  location?: string;
  startTimeFrom?: string;
  startTimeTo?: string;
  minParticipants?: number;
  maxParticipants?: number;
  sortBy?: string;
  sortOrder?: string;
}

/** 活动分页响应 */
export interface ActivityPageResponse {
  list: Activity[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

/** 活动类型 */
export interface ActivityType {
  id: number;
  name: string;
}

/** 活动类型创建请求 */
export interface ActivityTypeCreateRequest {
  name: string;
}

/** 活动类型响应 */
export interface ActivityTypeResponse {
  id: number;
  name: string;
}

/** 标签 */
export interface Tag {
  id: number;
  name: string;
}

/** 标签创建请求 */
export interface TagCreateRequest {
  name: string;
  color?: string;
}

/** 标签响应 */
export interface TagResponse {
  id: number;
  activityId?: number;
  name: string;
  color?: string;
}

/** 话题 */
export interface Topic {
  id: number;
  title: string;
  activityId: number;
  createdAt: string;
}

/** 话题创建请求 */
export interface TopicCreateRequest {
  title: string;
  activityId: number;
}

/** 话题更新请求 */
export interface TopicUpdateRequest {
  title: string;
}

/** 话题响应 */
export interface TopicResponse {
  id: number;
  activityId: number;
  title: string;
  creatorId: number;
  creatorName: string;
  createdAt: string;
  updatedAt: string;
}

/** 活动标签设置请求 */
export interface ActivityTagRequest {
  activityId: number;
  tagIds: number[];
}

/** 活动图片 */
export interface ActivityImage {
  id: number;
  activityId: number;
  fileUrl: string;
  fileName: string;
  createdAt: string;
}

/** 审核统计 */
export interface ActivityApprovalStatistics {
  pending: number;
  approved: number;
  rejected: number;
  total: number;
  pendingActivities: Activity[];
}

/** 审核请求 */
export interface ActivityApprovalRequest {
  reason?: string;
}

/** 报名记录 */
export interface Registration {
  id: number;
  activityId: number;
  activityTitle?: string;
  location?: string;
  status: 'pending' | 'confirmed' | 'cancelled';
  createdAt: string;
  updatedAt?: string;
}
