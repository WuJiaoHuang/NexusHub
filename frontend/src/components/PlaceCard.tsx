import { Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import type { CampusPlace } from '@/types/api'
import { formatDistance } from '@/utils/format'
import './components.scss'

interface PlaceCardProps {
  place: CampusPlace
}

export function PlaceCard({ place }: PlaceCardProps) {
  return (
    <View className='place-card cl-pressable' onClick={() => Taro.navigateTo({ url: `/pages/place-detail/index?id=${place.id}` })}>
      <View className='place-card__poster'>
        <Text>{place.name.slice(0, 2)}</Text>
      </View>
      <View className='place-card__body'>
        <View className='place-card__row'>
          <Text className='place-card__title'>{place.name}</Text>
          <Text className='place-card__distance'>{formatDistance(place.distance)}</Text>
        </View>
        <Text className='place-card__address'>{place.area || place.address || '校园地点'}</Text>
        <Text className='place-card__meta'>{place.openHours || '开放时间以现场为准'}</Text>
      </View>
    </View>
  )
}
