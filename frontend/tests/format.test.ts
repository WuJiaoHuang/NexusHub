import { describe, expect, it } from 'vitest'
import { formatDistance, splitTags } from '../src/utils/format'

describe('formatDistance', () => {
  it('formats meters under one kilometer', () => {
    expect(formatDistance(0.12)).toBe('120m')
  })

  it('formats kilometers', () => {
    expect(formatDistance(1.23)).toBe('1.2km')
  })
})

describe('splitTags', () => {
  it('splits comma separated tags', () => {
    expect(splitTags('Java, AI,篮球')).toEqual(['Java', 'AI', '篮球'])
  })
})
