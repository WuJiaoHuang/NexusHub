import { mockCategories, mockEvents, mockPlaces, mockPosts, mockUser } from './data'
import type { CampusPlaceSearchResponse, EventRegistration, ScrollResult } from '@/types/api'
import type { UploadResponse } from '@/types/media'

function numberAfter(path: string, prefix: string): number {
  return Number(path.replace(prefix, '').split('/')[0])
}

export async function mockRequest<TResponse>(url: string): Promise<TResponse> {
  const parsed = new URL(url, 'http://mock.local')
  const path = parsed.pathname

  if (path === '/auth/code') return '123456' as TResponse
  if (path === '/auth/login') return 'demo-token' as TResponse
  if (path === '/users/me') return mockUser as TResponse
  if (path === '/users/avatar') return mockUser as TResponse
  if (path === '/users/interests') return ['Java', 'AI', '篮球'] as TResponse
  if (path === '/uploads/image') {
    const scene = parsed.searchParams.get('scene') || 'POST'
    const objectKey = scene === 'AVATAR' ? 'avatars/1/demo.jpg' : 'posts/1/202609/demo.jpg'
    return { objectKey, url: `https://mock.campuslink.local/${objectKey}`, size: 1024, contentType: 'image/jpeg' } satisfies UploadResponse as TResponse
  }
  if (path === '/place-categories/list') return mockCategories as TResponse
  if (path === '/places/nearby') {
    const categoryId = Number(parsed.searchParams.get('categoryId'))
    return mockPlaces.filter((place) => place.categoryId === categoryId) as TResponse
  }
  if (path === '/places/search') {
    const keyword = parsed.searchParams.get('keyword') || ''
    const records = mockPlaces.filter((place) => place.name.includes(keyword) || place.address?.includes(keyword))
    return { records, total: records.length, current: 1, size: 10 } satisfies CampusPlaceSearchResponse as TResponse
  }
  if (path.startsWith('/places/')) {
    const id = numberAfter(path, '/places/')
    return mockPlaces.find((place) => place.id === id) as TResponse
  }
  if (path === '/posts/hot') return mockPosts as TResponse
  if (path === '/posts') return 99 as TResponse
  if (path.startsWith('/posts/like/')) return null as TResponse
  if (path.startsWith('/posts/')) {
    const id = numberAfter(path, '/posts/')
    return mockPosts.find((post) => post.id === id) as TResponse
  }
  if (path === '/feeds') {
    return { list: mockPosts, minTime: Date.now(), offset: 1 } satisfies ScrollResult<(typeof mockPosts)[number]> as TResponse
  }
  if (path === '/events' || path === '/events/hot' || path === '/events/recommendations') return mockEvents as TResponse
  if (path.startsWith('/events/by-place/')) {
    const placeId = numberAfter(path, '/events/by-place/')
    return mockEvents.filter((event) => event.placeId === placeId) as TResponse
  }
  if (path.endsWith('/registrations/status')) return true as TResponse
  if (path.endsWith('/registrations')) return 10001 as TResponse
  if (path.startsWith('/events/')) {
    const id = numberAfter(path, '/events/')
    return mockEvents.find((event) => event.id === id) as TResponse
  }
  if (path === '/event-registrations/me') {
    return [{ id: 10001, userId: 1, eventId: 1, status: 1, createTime: new Date().toISOString() }] satisfies EventRegistration[] as TResponse
  }
  return null as TResponse
}
