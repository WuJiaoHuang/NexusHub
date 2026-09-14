import { request } from './client'
import type { LoginForm, UserDTO } from '@/types/api'

export const authApi = {
  sendCode(phone: string) {
    return request<string>(`/auth/code?phone=${encodeURIComponent(phone)}`, { method: 'POST' })
  },
  login(form: LoginForm) {
    return request<string, LoginForm>('/auth/login', { method: 'POST', data: form })
  },
  logout() {
    return request<null>('/auth/logout', { method: 'POST' })
  },
  me() {
    return request<UserDTO>('/users/me')
  },
  updateAvatar(url: string) {
    return request<UserDTO, { url: string }>('/users/avatar', { method: 'PUT', data: { url } })
  }
}
