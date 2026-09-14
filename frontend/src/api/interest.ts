import { request } from './client'

export const interestApi = {
  list() {
    return request<string[]>('/users/interests')
  },
  update(tags: string[]) {
    return request<string[], string[]>('/users/interests', { method: 'PUT', data: tags })
  }
}
