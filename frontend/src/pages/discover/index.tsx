import { Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { placeApi } from '@/api/place'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { PlaceCard } from '@/components/PlaceCard'
import { SearchBar } from '@/components/SearchBar'
import { SectionHeader } from '@/components/SectionHeader'
import { getCurrentLocation, DEFAULT_CAMPUS_LOCATION } from '@/platform/location'
import { queryKeys } from '@/queries/keys'
import './index.scss'

export default function DiscoverPage() {
  const [categoryId, setCategoryId] = useState(1)
  const [location, setLocation] = useState(DEFAULT_CAMPUS_LOCATION)
  const categories = useQuery({ queryKey: queryKeys.places.categories, queryFn: placeApi.categories })
  const places = useQuery({
    queryKey: queryKeys.places.nearby(categoryId, location.longitude, location.latitude),
    queryFn: () => placeApi.nearby({ categoryId, current: 1, x: location.longitude, y: location.latitude })
  })

  useDidShow(() => {
    getCurrentLocation().then(setLocation)
  })

  return (
    <View className='cl-page discover-page'>
      <SearchBar onFocus={() => Taro.navigateTo({ url: '/pages/search/index' })} />
      <SectionHeader eyebrow='MAP / 01' title='校园分类' />
      <View className='category-grid'>
        {categories.data?.map((category) => (
          <Text
            key={category.id}
            className={`category-chip ${categoryId === category.id ? 'category-chip--active' : ''}`}
            onClick={() => setCategoryId(category.id)}
          >
            {category.name}
          </Text>
        ))}
      </View>

      <SectionHeader eyebrow='MAP / 02' title={location.source === 'device' ? '附近校园地点' : '默认校园位置'} />
      {location.source === 'default' ? <Text className='discover-tip'>开启定位后可以发现附近的校园地点</Text> : null}
      {places.isError ? <ErrorState onRetry={() => places.refetch()} /> : null}
      {places.data?.length === 0 ? <EmptyState text='附近还没有收录校园地点' /> : null}
      {places.data?.map((place) => <PlaceCard key={place.id} place={place} />)}
    </View>
  )
}
