import { Button, Text, Textarea, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { postApi } from '@/api/post'
import './index.scss'

export default function PublishPage() {
  const [content, setContent] = useState('')
  const mutation = useMutation({
    mutationFn: () =>
      postApi.create({
        title: content.trim().slice(0, 30) || '校园随记',
        content: content.trim()
      }),
    onSuccess: () => {
      Taro.showToast({ title: '已发布', icon: 'success' })
      setContent('')
      Taro.switchTab({ url: '/pages/home/index' })
    },
    onError: () => Taro.showToast({ title: '发布失败，请先登录', icon: 'none' })
  })

  return (
    <View className='cl-page publish-page'>
      <Text className='publish-title'>分享今天校园里发生的事</Text>
      <Textarea
        className='publish-input'
        value={content}
        maxlength={500}
        placeholder='写下一个地点、一场活动，或一个刚好想告诉同学的发现...'
        onInput={(event) => setContent(event.detail.value)}
      />
      <Text className='publish-note'>图片发布会在后续接入静态资源访问后开放。</Text>
      <Button className='publish-button' disabled={!content.trim() || mutation.isPending} onClick={() => mutation.mutate()}>
        {mutation.isPending ? '发布中' : '发布'}
      </Button>
    </View>
  )
}
