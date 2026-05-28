export interface Comment {
  id: number
  activityId: number
  userId: number
  username: string
  content: string
  createdAt: string
}

export interface CommentRequest {
  content: string
}
