import { request } from './client'
import type { CampusPlace, CampusPlaceSearchRequest, CampusPlaceSearchResponse, PlaceCategory } from '@/types/api'

function toQuery(params: Record<string, string | number | undefined>): string {
  const search = Object.entries(params)
    .filter((entry): entry is [string, string | number] => entry[1] !== undefined && entry[1] !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return search ? `?${search}` : ''
}

export const placeApi = {
  categories() {
    return request<PlaceCategory[]>('/place-categories/list')
  },
  detail(id: number) {
    return request<CampusPlace>(`/places/${id}`)
  },
  nearby(params: { categoryId: number; current?: number; x?: number; y?: number }) {
    return request<CampusPlace[]>(`/places/nearby${toQuery(params)}`)
  },
  search(params: CampusPlaceSearchRequest) {
    return request<CampusPlaceSearchResponse>(`/places/search${toQuery(params)}`)
  }
}
