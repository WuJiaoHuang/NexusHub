export function debounce<TArgs extends unknown[]>(fn: (...args: TArgs) => void, wait: number) {
  let timer: ReturnType<typeof setTimeout> | undefined
  return (...args: TArgs) => {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => fn(...args), wait)
  }
}
