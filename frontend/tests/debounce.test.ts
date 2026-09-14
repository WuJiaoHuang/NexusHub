import { describe, expect, it, vi } from 'vitest'
import { debounce } from '../src/utils/debounce'

describe('debounce', () => {
  it('uses the latest call', () => {
    vi.useFakeTimers()
    const fn = vi.fn()
    const debounced = debounce(fn, 300)
    debounced('j')
    debounced('ja')
    debounced('java')
    vi.advanceTimersByTime(300)
    expect(fn).toHaveBeenCalledTimes(1)
    expect(fn).toHaveBeenCalledWith('java')
    vi.useRealTimers()
  })
})
