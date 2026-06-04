export interface Tag {
  id: number
  activityId: number
  name: string
  color: string
}

export interface Activity {
  id: number
  title: string
  publisherId: number
  publisherName: string
  startTime: string
  endTime: string
  location: string
  description: string
  status: string
  approvalStatus: string
  maxParticipants: number
  currentParticipants?: number
  tags?: Tag[]
  createdAt: string
  updatedAt: string
}

export interface ActivityPublishRequest {
  title: string
  startTime: string
  endTime: string
  location: string
  description?: string
  maxParticipants?: number
  tags?: string[]
}

export interface ActivityQueryRequest {
  keyword?: string
  status?: string
  startDate?: string
  endDate?: string
  location?: string
  page?: number
  size?: number
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  size: number
}
