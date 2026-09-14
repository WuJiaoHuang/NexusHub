import { describe, expect, it, vi } from 'vitest'

vi.mock('../src/api/event', () => ({
  eventApi: {
    registrationStatus: vi.fn()
  }
}))

import { eventApi } from '../src/api/event'
import { pollRegistrationStatus } from '../src/utils/registration'

describe('pollRegistrationStatus', () => {
  it('returns registered when backend eventually confirms', async () => {
    vi.useFakeTimers()
    vi.mocked(eventApi.registrationStatus).mockResolvedValueOnce(false).mockResolvedValueOnce(true)
    const promise = pollRegistrationStatus(1)
    await vi.advanceTimersByTimeAsync(1000)
    await vi.advanceTimersByTimeAsync(2000)
    await expect(promise).resolves.toBe('registered')
    vi.useRealTimers()
  })
})
