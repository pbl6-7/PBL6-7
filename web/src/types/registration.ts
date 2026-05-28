export interface Registration {
  id: number
  activityId: number
  userId: number
  username: string
  activityTitle?: string
  status: string
  registeredAt: string
}

export interface RegistrationRequest {
  activityId: number
}

export interface RegistrationStatusUpdateRequest {
  registrationId: number
  status: string
}

export interface RegistrationPageResponse {
  records: Registration[]
  total: number
  page: number
  size: number
}
