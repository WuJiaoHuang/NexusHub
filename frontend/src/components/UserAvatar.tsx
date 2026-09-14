import { Text, View } from '@tarojs/components'
import './components.scss'

interface UserAvatarProps {
  name?: string
  src?: string
  size?: 'sm' | 'md'
}

export function UserAvatar({ name, size = 'md' }: UserAvatarProps) {
  const initial = name?.trim().slice(0, 1) || 'C'
  return (
    <View className={`user-avatar user-avatar--${size}`}>
      <Text>{initial}</Text>
    </View>
  )
}
