import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import type { CampusEvent } from '@/types/api'
import { formatTime, splitTags } from '@/utils/format'
import './components.scss'

interface EventCardProps {
  event: CampusEvent
}

export function EventCard({ event }: EventCardProps) {
  const tags = splitTags(event.tags).slice(0, 3)
  const month = event.startTime ? new Date(event.startTime).toLocaleString('en-US', { month: 'short' }).toUpperCase() : 'CAMPUS'
  const day = event.startTime ? new Date(event.startTime).getDate() : ''

  return (
    <View className='event-card cl-pressable' onClick={() => Taro.navigateTo({ url: `/pages/event-detail/index?id=${event.id}` })}>
      <View className='event-card__poster'>
        <Text className='event-card__month'>{month}</Text>
        <Text className='event-card__day'>{day}</Text>
      </View>
      <View className='event-card__body'>
        <Text className='event-card__title'>{event.title}</Text>
        <Text className='event-card__time'>{formatTime(event.startTime)}</Text>
        <Text className='event-card__location'>{event.location || '校园内'}</Text>
        <View className='event-card__tags'>
          {tags.map((tag) => (
            <Text key={tag} className='event-card__tag'>
              {tag}
            </Text>
          ))}
        </View>
      </View>
    </View>
  )
}
