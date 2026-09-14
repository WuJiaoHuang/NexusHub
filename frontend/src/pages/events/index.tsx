import { Text, View } from '@tarojs/components'
import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { eventApi } from '@/api/event'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { EventCard } from '@/components/EventCard'
import { SectionHeader } from '@/components/SectionHeader'
import { queryKeys } from '@/queries/keys'
import './index.scss'

type EventTab = 'recommended' | 'hot' | 'all'

export default function EventsPage() {
  const [tab, setTab] = useState<EventTab>('recommended')
  const recommended = useQuery({ queryKey: queryKeys.events.recommended, queryFn: () => eventApi.recommended(10) })
  const hot = useQuery({ queryKey: queryKeys.events.hot, queryFn: () => eventApi.hot(1) })
  const all = useQuery({ queryKey: queryKeys.events.list, queryFn: () => eventApi.list(1) })
  const active = tab === 'recommended' ? recommended : tab === 'hot' ? hot : all

  return (
    <View className='cl-page events-page'>
      <SectionHeader eyebrow='EVENTS' title='校园正在发生' />
      <View className='event-tabs'>
        {[
          ['recommended', '推荐'],
          ['hot', '热门'],
          ['all', '全部']
        ].map(([key, label]) => (
          <Text key={key} className={`event-tab ${tab === key ? 'event-tab--active' : ''}`} onClick={() => setTab(key as EventTab)}>
            {label}
          </Text>
        ))}
      </View>
      {active.isError ? <ErrorState onRetry={() => active.refetch()} /> : null}
      {active.data?.length === 0 ? <EmptyState text='这几天暂时没有活动' /> : null}
      {active.data?.map((event) => <EventCard key={event.id} event={event} />)}
    </View>
  )
}
