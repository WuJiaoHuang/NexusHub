import { Button, Image, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import type { LocalMediaFile } from '@/types/media'
import './components.scss'

interface PostImageGridProps {
  files: LocalMediaFile[]
  disabled?: boolean
  onAdd: () => void
  onRemove: (id: string) => void
}

export function PostImageGrid({ files, disabled, onAdd, onRemove }: PostImageGridProps) {
  return (
    <View className='media-grid'>
      {files.map((file) => (
        <View key={file.id} className='media-grid__item'>
          <Image
            className='media-grid__image'
            src={file.path}
            mode='aspectFill'
            onClick={() => Taro.previewImage({ current: file.path, urls: files.map((item) => item.path) })}
          />
          {!disabled ? (
            <Button className='media-grid__remove' onClick={() => onRemove(file.id)}>
              ×
            </Button>
          ) : null}
        </View>
      ))}
      {files.length < 9 ? (
        <Button className='media-grid__add' disabled={disabled} onClick={onAdd}>
          <Text className='media-grid__plus'>+</Text>
          <Text>添加图片</Text>
        </Button>
      ) : null}
    </View>
  )
}
