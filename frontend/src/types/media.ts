export interface LocalMediaFile {
  id: string
  path: string
  name?: string
  size?: number
  type?: string
  file?: File
}

export interface UploadResponse {
  objectKey: string
  url: string
  size: number
  contentType: string
}

export type UploadScene = 'AVATAR' | 'POST'
