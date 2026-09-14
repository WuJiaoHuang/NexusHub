import dayjs from 'dayjs'

export function formatDistance(distance?: number): string {
  if (distance === undefined || Number.isNaN(distance)) {
    return '校内'
  }
  const meters = distance * 1000
  if (meters < 1000) {
    return `${Math.round(meters)}m`
  }
  return `${(meters / 1000).toFixed(1)}km`
}

export function formatTime(value?: string): string {
  if (!value) {
    return ''
  }
  return dayjs(value).format('MM月DD日 HH:mm')
}

export function splitTags(tags?: string): string[] {
  if (!tags) {
    return []
  }
  return tags
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
}
