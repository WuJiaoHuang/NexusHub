import { Text, View } from '@tarojs/components'
import { useQuery } from '@tanstack/react-query'
import { eventApi } from '@/api/event'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { queryKeys } from '@/queries/keys'
import { formatTime } from '@/utils/format'
import './index.scss'

export default function MyEventsPage() {
  const registrations = useQuery({ queryKey: queryKeys.events.mine, queryFn: eventApi.mine })

  return (
    <View className='cl-page my-events-page'>
      <Text className='my-events-title'>我的活动</Text>
      {registrations.isError ? <ErrorState onRetry={() => registrations.refetch()} /> : null}
      {registrations.data?.length === 0 ? <EmptyState text='你还没有报名活动' /> : null}
      {registrations.data?.map((item) => (
        <View key={item.id} className='registration-card'>
          <Text className='registration-card__title'>报名记录 #{item.id}</Text>
          <Text>活动 ID：{item.eventId}</Text>
          <Text>状态：{item.status === 1 ? '已报名' : '处理中'}</Text>
          <Text>{formatTime(item.createTime)}</Text>
        </View>
      ))}
    </View>
  )
}
