import { describe, expect, it, vi } from 'vitest'
import { canAddMoreImages, parseImageUrls, uploadWithConcurrency } from '../src/utils/media'

describe('parseImageUrls', () => {
  it('handles empty string', () => {
    expect(parseImageUrls('')).toEqual([])
  })

  it('handles single url', () => {
    expect(parseImageUrls('https://a.test/1.jpg')).toEqual(['https://a.test/1.jpg'])
  })

  it('handles multiple urls', () => {
    expect(parseImageUrls(' https://a.test/1.jpg,https://a.test/2.png ,,')).toEqual([
      'https://a.test/1.jpg',
      'https://a.test/2.png'
    ])
  })
})

describe('uploadWithConcurrency', () => {
  it('limits concurrent workers', async () => {
    let active = 0
    let maxActive = 0
    const worker = vi.fn(async (item: number) => {
      active += 1
      maxActive = Math.max(maxActive, active)
      await new Promise((resolve) => setTimeout(resolve, 10))
      active -= 1
      return item * 2
    })

    const result = await uploadWithConcurrency([1, 2, 3, 4, 5], 3, worker)

    expect(result).toEqual([2, 4, 6, 8, 10])
    expect(maxActive).toBeLessThanOrEqual(3)
  })
})

describe('canAddMoreImages', () => {
  it('allows fewer than 9 images only', () => {
    expect(canAddMoreImages(8)).toBe(true)
    expect(canAddMoreImages(9)).toBe(false)
  })
})
