export const queryKeys = {
  user: {
    me: ['user', 'me'] as const,
    interests: ['user', 'interests'] as const
  },
  places: {
    categories: ['places', 'categories'] as const,
    detail: (id: number) => ['places', 'detail', id] as const,
    nearby: (categoryId: number, x?: number, y?: number) => ['places', 'nearby', categoryId, x, y] as const,
    search: (keyword: string) => ['places', 'search', keyword] as const
  },
  posts: {
    hot: ['posts', 'hot'] as const,
    detail: (id: number) => ['posts', 'detail', id] as const,
    feeds: ['feeds'] as const
  },
  events: {
    list: ['events', 'list'] as const,
    hot: ['events', 'hot'] as const,
    recommended: ['events', 'recommended'] as const,
    detail: (id: number) => ['events', 'detail', id] as const,
    registrationStatus: (id: number) => ['events', 'registration-status', id] as const,
    mine: ['event-registrations', 'me'] as const
  }
}
