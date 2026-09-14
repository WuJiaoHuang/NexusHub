import { Text, View } from '@tarojs/components'
import './components.scss'

interface UploadProgressProps {
  done: number
  total: number
}

export function UploadProgress({ done, total }: UploadProgressProps) {
  const width = total <= 0 ? 0 : Math.round((done / total) * 100)
  return (
    <View className='upload-progress'>
      <Text>正在上传图片 {done} / {total}</Text>
      <View className='upload-progress__bar'>
        <View className='upload-progress__fill' style={{ width: `${width}%` }} />
      </View>
    </View>
  )
}
