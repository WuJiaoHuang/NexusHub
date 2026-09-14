import { Image, Text, View } from '@tarojs/components'
import { useState } from 'react'
import './components.scss'

interface UserAvatarProps {
  name?: string
  src?: string
  size?: 'sm' | 'md'
}

export function UserAvatar({ name, size = 'md' }: UserAvatarProps) {
  const [failed, setFailed] = useState(false)
  const initial = name?.trim().slice(0, 1) || 'C'
  if (src && !failed) {
    return (
      <View className={`user-avatar user-avatar--${size}`}>
        <Image className='user-avatar__image' src={src} mode='aspectFill' onError={() => setFailed(true)} />
      </View>
    )
  }
  return (
    <View className={`user-avatar user-avatar--${size}`}>
      <Text>{initial}</Text>
    </View>
  )
}
