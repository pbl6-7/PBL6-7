/** 通知信息 */
export interface Notification {
  id: number;
  activityId: number;
  /** 通知标题 */
  title: string;
  type: string;
  content: string;
  isRead: boolean;
  createTime: string;
}

/** 通知分页响应 */
export interface NotificationPageResponse {
  records: Notification[];
  total: number;
  pages: number;
  current: number;
}
