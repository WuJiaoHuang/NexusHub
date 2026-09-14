import { Button, Text, Textarea, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { postApi } from '@/api/post'
import { uploadImage } from '@/api/upload'
import { PostImageGrid } from '@/components/PostImageGrid'
import { UploadProgress } from '@/components/UploadProgress'
import { chooseImages } from '@/platform/media'
import { queryKeys } from '@/queries/keys'
import type { LocalMediaFile } from '@/types/media'
import { canAddMoreImages, uploadWithConcurrency } from '@/utils/media'
import './index.scss'

export default function PublishPage() {
  const queryClient = useQueryClient()
  const [content, setContent] = useState('')
  const [files, setFiles] = useState<LocalMediaFile[]>([])
  const [progress, setProgress] = useState({ done: 0, total: 0 })
  const mutation = useMutation({
    mutationFn: async () => {
      setProgress({ done: 0, total: files.length })
      const uploaded = await uploadWithConcurrency(
        files,
        3,
        async (file, index) => {
          try {
            return await uploadImage({ filePath: file.path, file: file.file, scene: 'POST' })
          } catch {
            throw new Error(`第 ${index + 1} 张图片上传失败，请重试`)
          }
        },
        (done, total) => setProgress({ done, total })
      )
      return postApi.create({
        title: content.trim().slice(0, 30) || '校园随记',
        content: content.trim(),
        images: uploaded.map((item) => item.url).join(',')
      })
    },
    onSuccess: () => {
      Taro.showToast({ title: '已发布', icon: 'success' })
      setContent('')
      setFiles([])
      setProgress({ done: 0, total: 0 })
      queryClient.invalidateQueries({ queryKey: queryKeys.posts.hot })
      queryClient.invalidateQueries({ queryKey: queryKeys.posts.feeds })
      Taro.switchTab({ url: '/pages/home/index' })
    },
    onError: (error) => Taro.showToast({ title: error instanceof Error ? error.message : '发布失败，请重试', icon: 'none' })
  })

  async function addImages() {
    if (!canAddMoreImages(files.length) || mutation.isPending) {
      return
    }
    const selected = await chooseImages({ count: 9 - files.length })
    setFiles((current) => [...current, ...selected].slice(0, 9))
  }

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
      <PostImageGrid files={files} disabled={mutation.isPending} onAdd={addImages} onRemove={(id) => setFiles((current) => current.filter((file) => file.id !== id))} />
      {mutation.isPending && files.length > 0 ? <UploadProgress done={progress.done} total={progress.total} /> : null}
      <Text className='publish-note'>单条动态最多上传 9 张图片。</Text>
      <Button className='publish-button' disabled={!content.trim() || mutation.isPending} onClick={() => mutation.mutate()}>
        {mutation.isPending ? '正在发布' : '发布'}
      </Button>
    </View>
  )
}
