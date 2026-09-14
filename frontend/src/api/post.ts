import { request } from './client'
import type { CampusPost, ScrollResult, UserDTO } from '@/types/api'

export interface CreatePostPayload {
  title: string
  content?: string
  placeId?: number | null
  images?: string
}

export const postApi = {
  hot(current = 1) {
    return request<CampusPost[]>(`/posts/hot?current=${current}`)
  },
  detail(id: number) {
    return request<CampusPost>(`/posts/${id}`)
  },
  create(payload: CreatePostPayload) {
    return request<number, CreatePostPayload>('/posts', { method: 'POST', data: payload })
  },
  like(id: number) {
    return request<null>(`/posts/like/${id}`, { method: 'PUT' })
  },
  likes(id: number) {
    return request<UserDTO[]>(`/posts/likes/${id}`)
  },
  feed(lastId: number, offset = 0) {
    return request<ScrollResult<CampusPost> | []>(`/feeds?lastId=${lastId}&offset=${offset}`)
  }
}
