import Taro from '@tarojs/taro'
import { ApiError } from './error'
import { mockRequest } from '@/mocks/handler'
import { unwrapResult } from './result'
import { useAuthStore } from '@/store/auth'
import type { ApiResult } from '@/types/api'
import type { UploadResponse, UploadScene } from '@/types/media'

const API_BASE_URL = process.env.TARO_APP_API_BASE_URL || 'http://localhost:8080'
const DEMO_MODE = process.env.TARO_APP_DEMO_MODE === 'true'

interface UploadImageOptions {
  filePath: string
  file?: File
  scene: UploadScene
}

export async function uploadImage({ filePath, file, scene }: UploadImageOptions): Promise<UploadResponse> {
  if (DEMO_MODE) {
    return mockRequest<UploadResponse>(`/uploads/image?scene=${scene}`)
  }
  const token = useAuthStore.getState().token
  if (process.env.TARO_ENV === 'h5' && file) {
    return uploadImageByFormData(file, scene, token)
  }
  const response = await Taro.uploadFile({
    url: `${API_BASE_URL}/uploads/image`,
    filePath,
    name: 'file',
    formData: { scene },
    header: token ? { authorization: token } : undefined
  })
  if (response.statusCode === 401) {
    useAuthStore.getState().logoutLocal()
    throw new ApiError('请先登录', 401)
  }
  const data = typeof response.data === 'string' ? JSON.parse(response.data) : response.data
  return unwrapResult(data as ApiResult<UploadResponse>)
}

function uploadImageByFormData(file: File, scene: UploadScene, token: string | null): Promise<UploadResponse> {
  const form = new FormData()
  form.append('file', file)
  form.append('scene', scene)
  return fetch(`${API_BASE_URL}/uploads/image`, {
    method: 'POST',
    headers: token ? { authorization: token } : undefined,
    body: form
  }).then(async (response) => {
    if (response.status === 401) {
      useAuthStore.getState().logoutLocal()
      throw new ApiError('请先登录', 401)
    }
    return unwrapResult((await response.json()) as ApiResult<UploadResponse>)
  })
}
