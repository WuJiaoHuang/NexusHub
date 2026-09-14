import { Button, Text, View } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import { useQuery } from '@tanstack/react-query'
import { eventApi } from '@/api/event'
import { placeApi } from '@/api/place'
import { ErrorState } from '@/components/ErrorState'
import { EventCard } from '@/components/EventCard'
import { SectionHeader } from '@/components/SectionHeader'
import { queryKeys } from '@/queries/keys'
import { formatDistance } from '@/utils/format'
import './index.scss'

export default function PlaceDetailPage() {
  const router = useRouter()
  const id = Number(router.params.id)
  const place = useQuery({ queryKey: queryKeys.places.detail(id), queryFn: () => placeApi.detail(id), enabled: Number.isFinite(id) })
  const events = useQuery({ queryKey: ['events', 'by-place', id], queryFn: () => eventApi.byPlace(id), enabled: Number.isFinite(id) })

  if (place.isError) {
    return <ErrorState onRetry={() => place.refetch()} />
  }

  return (
    <View className='cl-page place-detail-page'>
      <View className='place-hero'>
        <Text>{place.data?.name?.slice(0, 2) || 'CL'}</Text>
      </View>
      <Text className='place-title'>{place.data?.name}</Text>
      <Text className='place-meta'>{formatDistance(place.data?.distance)} / {place.data?.area || '校园内'}</Text>
      <Text className='place-address'>{place.data?.address}</Text>
      <Text className='place-desc'>这里是校园生活中常被提起的一处地点，开放时间和现场安排以学校通知为准。</Text>
      <Button
        className='place-action'
        onClick={() => {
          if (process.env.TARO_ENV === 'weapp' && place.data?.x && place.data.y) {
            Taro.openLocation({ longitude: place.data.x, latitude: place.data.y, name: place.data.name, address: place.data.address })
          } else if (place.data?.address) {
            Taro.setClipboardData({ data: place.data.address })
          }
        }}
      >
        {process.env.TARO_ENV === 'weapp' ? '导航到这里' : '复制地址'}
      </Button>
      <SectionHeader eyebrow='PLACE / EVENTS' title='相关活动' />
      {events.data?.map((event) => <EventCard key={event.id} event={event} />)}
    </View>
  )
}
