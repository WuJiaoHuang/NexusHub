import { ApiError } from './error'
import type { ApiResult } from '@/types/api'

export function unwrapResult<T>(result: ApiResult<T>): T {
  if (!result.success) {
    throw new ApiError(result.errorMsg || '请求失败')
  }
  return result.data as T
}
