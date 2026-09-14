import { Button, Image, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { postApi } from '@/api/post'
import { queryKeys } from '@/queries/keys'
import type { CampusPost } from '@/types/api'
import { formatTime } from '@/utils/format'
import { parseImageUrls } from '@/utils/media'
import { UserAvatar } from './UserAvatar'
import './components.scss'

interface PostCardProps {
  post: CampusPost
}

export function PostCard({ post }: PostCardProps) {
  const queryClient = useQueryClient()
  const [failedImages, setFailedImages] = useState<string[]>([])
  const images = parseImageUrls(post.images).filter((url) => !failedImages.includes(url)).slice(0, 9)
  const mutation = useMutation({
    mutationFn: () => postApi.like(post.id),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: queryKeys.posts.hot })
      queryClient.setQueryData<CampusPost[]>(queryKeys.posts.hot, (old) =>
        old?.map((item) =>
          item.id === post.id
            ? {
                ...item,
                isLike: !item.isLike,
                liked: item.isLike ? Math.max(0, item.liked - 1) : item.liked + 1
              }
            : item
        )
      )
    },
    onError: () => {
      Taro.showToast({ title: '点赞失败，稍后再试', icon: 'none' })
      queryClient.invalidateQueries({ queryKey: queryKeys.posts.hot })
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.posts.hot })
    }
  })

  return (
    <View className='post-card cl-pressable' onClick={() => Taro.navigateTo({ url: `/pages/post-detail/index?id=${post.id}` })}>
      <View className='post-card__meta'>
        <UserAvatar name={post.name} src={post.icon} size='sm' />
        <View className='post-card__author'>
          <Text className='post-card__name'>{post.name || '校园同学'}</Text>
          <Text className='post-card__time'>{formatTime(post.createTime)}</Text>
        </View>
      </View>
      <Text className='post-card__title'>{post.title}</Text>
      {post.content ? <Text className='post-card__content'>{post.content}</Text> : null}
      {images.length > 0 ? (
        <View className={`post-card__images post-card__images--${Math.min(images.length, 4)}`}>
          {images.slice(0, 4).map((url, index) => (
            <View key={url} className='post-card__image-wrap' onClick={(event) => {
              event.stopPropagation()
              Taro.previewImage({ current: url, urls: images })
            }}>
              <Image
                className='post-card__image'
                src={url}
                mode='aspectFill'
                onError={() => setFailedImages((old) => (old.includes(url) ? old : [...old, url]))}
              />
              {index === 3 && images.length > 4 ? <Text className='post-card__image-more'>+{images.length - 4}</Text> : null}
            </View>
          ))}
        </View>
      ) : null}
      <View className='post-card__actions'>
        <Button
          className={`post-card__like ${post.isLike ? 'post-card__like--active' : ''}`}
          onClick={(event) => {
            event.stopPropagation()
            mutation.mutate()
          }}
        >
          <Text>赞</Text>
          <Text>{post.liked}</Text>
        </Button>
        <Text>{post.comments} 评论</Text>
      </View>
    </View>
  )
}
