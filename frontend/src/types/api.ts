export interface ApiResult<T> {
  success: boolean
  errorMsg: string | null
  data: T | null
  total: number | null
}

export interface UserDTO {
  id: number
  nickName: string
  icon: string
}

export interface UserInfo {
  userId: number
  city?: string
  introduce?: string
  fans?: number
  followee?: number
  gender?: boolean
  birthday?: string
  credits?: number
  level?: boolean
}

export interface PlaceCategory {
  id: number
  name: string
  icon: string
  sort: number
}

export interface CampusPlace {
  id: number
  name: string
  categoryId: number
  images?: string
  area?: string
  address?: string
  x?: number
  y?: number
  avgPrice?: number
  sold?: number
  comments?: number
  score?: number
  openHours?: string
  createTime?: string
  updateTime?: string
  distance?: number
}

export interface CampusPost {
  id: number
  placeId?: number | null
  userId: number
  icon?: string
  name?: string
  isLike?: boolean
  title: string
  images?: string
  content?: string
  liked: number
  comments: number
  createTime?: string
  updateTime?: string
}

export interface ScrollResult<T> {
  list: T[]
  minTime: number
  offset: number
}

export interface CampusEvent {
  id: number
  placeId?: number | null
  title: string
  description?: string
  category?: string
  location?: string
  cover?: string
  tags?: string
  startTime?: string
  endTime?: string
  registrationStartTime?: string
  registrationEndTime?: string
  capacity?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface EventRegistration {
  id: number
  userId: number
  eventId: number
  status: number
  createTime?: string
  updateTime?: string
}

export interface LoginForm {
  phone: string
  code: string
  password?: string
}

export interface CampusPlaceSearchRequest {
  keyword?: string
  categoryId?: number
  area?: string
  minPrice?: number
  maxPrice?: number
  minScore?: number
  current?: number
  size?: number
  sort?: 'score_desc' | 'sold_desc' | 'avgPrice_asc' | 'avgPrice_desc'
}

export interface CampusPlaceSearchResponse {
  records: CampusPlace[]
  total: number
  current: number
  size: number
}
