import { Text, View } from '@tarojs/components'
import { useRouter } from '@tarojs/taro'
import { useQuery } from '@tanstack/react-query'
import { postApi } from '@/api/post'
import { ErrorState } from '@/components/ErrorState'
import { PostCard } from '@/components/PostCard'
import { queryKeys } from '@/queries/keys'

export default function PostDetailPage() {
  const router = useRouter()
  const id = Number(router.params.id)
  const post = useQuery({ queryKey: queryKeys.posts.detail(id), queryFn: () => postApi.detail(id), enabled: Number.isFinite(id) })

  if (post.isError) {
    return <ErrorState onRetry={() => post.refetch()} />
  }

  return (
    <View className='cl-page'>
      {post.data ? <PostCard post={post.data} /> : null}
      <Text style='color: var(--cl-text-secondary); font-size: 13px;'>评论功能后端暂未开放。</Text>
    </View>
  )
}
