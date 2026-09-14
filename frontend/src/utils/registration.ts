import { eventApi } from '@/api/event'

export type RegistrationUiStatus = 'idle' | 'processing' | 'registered' | 'timeout' | 'failed'

const POLL_DELAYS = [1000, 2000, 3000, 5000]

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms)
  })
}

export async function pollRegistrationStatus(eventId: number): Promise<RegistrationUiStatus> {
  for (const delay of POLL_DELAYS) {
    await sleep(delay)
    const registered = await eventApi.registrationStatus(eventId)
    if (registered) {
      return 'registered'
    }
  }
  return 'timeout'
}
