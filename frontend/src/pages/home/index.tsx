import { Text, View } from '@tarojs/components'
import Taro, { usePullDownRefresh } from '@tarojs/taro'
import { useQuery } from '@tanstack/react-query'
import { eventApi } from '@/api/event'
import { postApi } from '@/api/post'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { EventCard } from '@/components/EventCard'
import { FeedSkeleton } from '@/components/Skeleton'
import { PostCard } from '@/components/PostCard'
import { SectionHeader } from '@/components/SectionHeader'
import { queryKeys } from '@/queries/keys'
import './index.scss'

export default function HomePage() {
  const posts = useQuery({ queryKey: queryKeys.posts.hot, queryFn: () => postApi.hot(1) })
  const events = useQuery({ queryKey: queryKeys.events.hot, queryFn: () => eventApi.hot(1) })

  usePullDownRefresh(() => {
    Promise.all([posts.refetch(), events.refetch()]).finally(() => Taro.stopPullDownRefresh())
  })

  return (
    <View className='cl-page home-page'>
      <View className='home-top'>
        <View>
          <Text className='home-top__brand'>CampusLink</Text>
          <Text className='home-top__campus'>北邮沙河校区</Text>
        </View>
        <Text className='home-top__action' onClick={() => Taro.navigateTo({ url: '/pages/search/index' })}>
          搜索
        </Text>
      </View>

      <View className='today-board'>
        <Text className='today-board__eyebrow'>TODAY / CAMPUS</Text>
        <Text className='today-board__title'>下午好，林同学</Text>
        <Text className='today-board__text'>今天校园里也有不少新鲜事。</Text>
        <View className='today-board__stats'>
          <Text>活动 {events.data?.length ?? 0}</Text>
          <Text>动态 {posts.data?.length ?? 0}</Text>
          <Text>地点更新</Text>
        </View>
      </View>

      <SectionHeader eyebrow='CAMPUS / 01' title='今日活动' action='活动页' />
      {events.isLoading ? <FeedSkeleton /> : null}
      {events.isError ? <ErrorState onRetry={() => events.refetch()} /> : null}
      {events.data?.slice(0, 2).map((event) => <EventCard key={event.id} event={event} />)}

      <SectionHeader eyebrow='CAMPUS / 02' title='校园动态' />
      {posts.isLoading ? (
        <>
          <FeedSkeleton />
          <FeedSkeleton />
        </>
      ) : null}
      {posts.isError ? <ErrorState onRetry={() => posts.refetch()} /> : null}
      {posts.data?.length === 0 ? <EmptyState text='校园里暂时还没有新动态' /> : null}
      {posts.data?.map((post) => <PostCard key={post.id} post={post} />)}
    </View>
  )
}
