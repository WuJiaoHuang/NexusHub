import { Button, Image, Text, View } from '@tarojs/components'
import Taro, { useRouter, useShareAppMessage } from '@tarojs/taro'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { eventApi } from '@/api/event'
import { ErrorState } from '@/components/ErrorState'
import { SectionHeader } from '@/components/SectionHeader'
import { successHaptic } from '@/platform/haptics'
import { queryKeys } from '@/queries/keys'
import { formatTime, splitTags } from '@/utils/format'
import { pollRegistrationStatus, type RegistrationUiStatus } from '@/utils/registration'
import './index.scss'

export default function EventDetailPage() {
  const router = useRouter()
  const id = Number(router.params.id)
  const [status, setStatus] = useState<RegistrationUiStatus>('idle')
  const [coverFailed, setCoverFailed] = useState(false)
  const event = useQuery({ queryKey: queryKeys.events.detail(id), queryFn: () => eventApi.detail(id), enabled: Number.isFinite(id) })
  const statusQuery = useQuery({
    queryKey: queryKeys.events.registrationStatus(id),
    queryFn: () => eventApi.registrationStatus(id),
    enabled: Number.isFinite(id),
    retry: false
  })
  const register = useMutation({
    mutationFn: () => eventApi.register(id),
    onSuccess: async () => {
      setStatus('processing')
      Taro.showToast({ title: '报名请求已提交', icon: 'none' })
      const next = await pollRegistrationStatus(id)
      setStatus(next)
      if (next === 'registered') {
        successHaptic()
        Taro.showToast({ title: '报名成功', icon: 'success' })
      } else {
        Taro.showToast({ title: '仍在处理中，可稍后查看', icon: 'none' })
      }
    },
    onError: () => {
      setStatus('failed')
      Taro.showToast({ title: '报名失败', icon: 'none' })
    }
  })

  useShareAppMessage(() => ({
    title: event.data?.title || 'CampusLink 校园活动',
    path: `/pages/event-detail/index?id=${id}`
  }))

  const registered = statusQuery.data || status === 'registered'
  const cta = registered ? '已报名' : status === 'processing' ? '处理中' : status === 'timeout' ? '稍后查看状态' : '立即报名'

  if (event.isError) {
    return <ErrorState onRetry={() => event.refetch()} />
  }

  return (
    <View className='cl-page event-detail-page'>
      <View className='event-poster'>
        {event.data?.cover && !coverFailed ? (
          <Image className='event-poster__image' src={event.data.cover} mode='aspectFill' onError={() => setCoverFailed(true)} />
        ) : null}
        <View className='event-poster__content'>
          <Text className='event-poster__date'>{formatTime(event.data?.startTime)}</Text>
          <Text className='event-poster__title'>{event.data?.title}</Text>
          <Text className='event-poster__location'>{event.data?.location}</Text>
        </View>
      </View>
      <View className='event-info'>
        <Text>时间：{formatTime(event.data?.startTime)}</Text>
        <Text>地点：{event.data?.location || '校园内'}</Text>
        <Text>名额：{event.data?.capacity ?? '--'}</Text>
      </View>
      <SectionHeader eyebrow='EVENT / INFO' title='活动介绍' />
      <Text className='event-desc'>{event.data?.description || '活动介绍暂未填写。'}</Text>
      <View className='event-tags'>
        {splitTags(event.data?.tags).map((tag) => (
          <Text key={tag}>{tag}</Text>
        ))}
      </View>
      <View className='event-cta-wrap'>
        <Button className='event-cta' disabled={registered || register.isPending || status === 'processing'} onClick={() => register.mutate()}>
          {cta}
        </Button>
      </View>
    </View>
  )
}
