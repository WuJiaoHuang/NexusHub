import Taro from '@tarojs/taro'
import { ApiError } from './error'
import { mockRequest } from '@/mocks/handler'
import { unwrapResult } from './result'
import { useAuthStore } from '@/store/auth'
import type { ApiResult } from '@/types/api'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface RequestOptions<TBody> {
  method?: HttpMethod
  data?: TBody
  header?: Record<string, string>
}

const API_BASE_URL = process.env.TARO_APP_API_BASE_URL || 'http://localhost:8080'
const DEMO_MODE = process.env.TARO_APP_DEMO_MODE === 'true'

export async function request<TResponse, TBody = unknown>(
  url: string,
  options: RequestOptions<TBody> = {}
): Promise<TResponse> {
  if (DEMO_MODE) {
    return mockRequest<TResponse>(url)
  }

  const token = useAuthStore.getState().token
  const header: Record<string, string> = {
    'content-type': 'application/json',
    ...options.header
  }
  if (token) {
    header.authorization = token
  }

  const response = await Taro.request<ApiResult<TResponse>>({
    url: `${API_BASE_URL}${url}`,
    method: options.method || 'GET',
    data: options.data,
    header
  })

  if (response.statusCode === 401) {
    useAuthStore.getState().logoutLocal()
    throw new ApiError('请先登录', 401)
  }
  if (response.statusCode < 200 || response.statusCode >= 300) {
    throw new ApiError(`网络请求失败 (${response.statusCode})`, response.statusCode)
  }
  return unwrapResult(response.data)
}
