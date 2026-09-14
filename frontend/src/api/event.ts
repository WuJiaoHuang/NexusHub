import { request } from './client'
import type { CampusEvent, EventRegistration } from '@/types/api'

export const eventApi = {
  list(current = 1) {
    return request<CampusEvent[]>(`/events?current=${current}`)
  },
  hot(current = 1) {
    return request<CampusEvent[]>(`/events/hot?current=${current}`)
  },
  recommended(limit = 10) {
    return request<CampusEvent[]>(`/events/recommendations?limit=${limit}`)
  },
  detail(id: number) {
    return request<CampusEvent>(`/events/${id}`)
  },
  byPlace(placeId: number) {
    return request<CampusEvent[]>(`/events/by-place/${placeId}`)
  },
  register(eventId: number) {
    return request<number>(`/events/${eventId}/registrations`, { method: 'POST' })
  },
  registrationStatus(eventId: number) {
    return request<boolean>(`/events/${eventId}/registrations/status`)
  },
  mine() {
    return request<EventRegistration[]>('/event-registrations/me')
  }
}
