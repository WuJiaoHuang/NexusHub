export function parseImageUrls(value?: string): string[] {
  if (!value) {
    return []
  }
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

export function firstImage(value?: string): string | undefined {
  return parseImageUrls(value)[0]
}

export async function uploadWithConcurrency<T, R>(
  items: T[],
  limit: number,
  worker: (item: T, index: number) => Promise<R>,
  onDone?: (done: number, total: number) => void
): Promise<R[]> {
  const results: R[] = new Array(items.length)
  let nextIndex = 0
  let done = 0

  async function run() {
    while (nextIndex < items.length) {
      const current = nextIndex
      nextIndex += 1
      results[current] = await worker(items[current] as T, current)
      done += 1
      onDone?.(done, items.length)
    }
  }

  const workers = Array.from({ length: Math.min(limit, items.length) }, run)
  await Promise.all(workers)
  return results
}

export function canAddMoreImages(currentCount: number, max = 9): boolean {
  return currentCount < max
}
