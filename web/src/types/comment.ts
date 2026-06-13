/** 评论信息 */
export interface Comment {
  id: number;
  activityId: number;
  userId: number;
  username: string;
  content: string;
  replyToId: number | null;
  createdAt: string;
}

/** 评论创建请求 */
export interface CommentRequest {
  content: string;
  replyToId?: number;
}

/** 评论响应 */
export interface CommentResponse {
  id: number;
  activityId: number;
  userId: number;
  username: string;
  content: string;
  replyToId: number | null;
  replyToUsername: string | null;
  createdAt: string;
  replyCount: number;
  replies: CommentResponse[];
}
