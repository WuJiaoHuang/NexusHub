import { View } from '@tarojs/components'
import './components.scss'

export function FeedSkeleton() {
  return (
    <View className='skeleton-card'>
      <View className='skeleton-line skeleton-line--short' />
      <View className='skeleton-line' />
      <View className='skeleton-line skeleton-line--mid' />
    </View>
  )
}
