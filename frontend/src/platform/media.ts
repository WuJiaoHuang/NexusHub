import Taro from '@tarojs/taro'
import type { LocalMediaFile } from '@/types/media'

interface ChooseImagesOptions {
  count: number
  sourceType?: Array<'album' | 'camera'>
}

export async function chooseImages({ count, sourceType = ['album', 'camera'] }: ChooseImagesOptions): Promise<LocalMediaFile[]> {
  if (process.env.TARO_ENV === 'h5') {
    return chooseImagesByInput(count)
  }
  const result = await Taro.chooseMedia({
    count,
    mediaType: ['image'],
    sourceType
  })
  return result.tempFiles.map((file, index) => ({
    id: `${Date.now()}-${index}`,
    path: file.tempFilePath,
    size: file.size
  }))
}

export async function compressImageForAvatar(file: LocalMediaFile): Promise<LocalMediaFile> {
  if (process.env.TARO_ENV !== 'weapp') {
    return file
  }
  try {
    const result = await Taro.compressImage({ src: file.path, quality: 80 })
    return { ...file, path: result.tempFilePath }
  } catch {
    return file
  }
}

function chooseImagesByInput(count: number): Promise<LocalMediaFile[]> {
  return new Promise((resolve, reject) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/jpeg,image/png,image/webp'
    input.multiple = count > 1
    input.onchange = () => {
      const files = Array.from(input.files || []).slice(0, count)
      resolve(
        files.map((file, index) => ({
          id: `${Date.now()}-${index}`,
          path: URL.createObjectURL(file),
          name: file.name,
          size: file.size,
          type: file.type,
          file
        }))
      )
    }
    input.onerror = () => reject(new Error('选择图片失败'))
    input.click()
  })
}
