import { Button, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { interestApi } from '@/api/interest'
import { queryKeys } from '@/queries/keys'
import './index.scss'

const OPTIONS = ['Java', 'AI', '篮球', '摄影', '音乐', '跑步', '羽毛球', '游戏', '阅读', '志愿活动']

export default function InterestsPage() {
  const interests = useQuery({ queryKey: queryKeys.user.interests, queryFn: interestApi.list })
  const [selected, setSelected] = useState<string[]>([])
  const update = useMutation({
    mutationFn: () => interestApi.update(selected),
    onSuccess: () => Taro.showToast({ title: '已保存', icon: 'success' }),
    onError: () => Taro.showToast({ title: '保存失败', icon: 'none' })
  })

  useEffect(() => {
    if (interests.data) {
      setSelected(interests.data)
    }
  }, [interests.data])

  function toggle(tag: string) {
    setSelected((current) => (current.includes(tag) ? current.filter((item) => item !== tag) : [...current, tag]))
  }

  return (
    <View className='cl-page interests-page'>
      <Text className='interests-title'>我的兴趣</Text>
      <Text className='interests-subtitle'>用于活动页的规则推荐，不会写成 AI 推荐。</Text>
      <View className='interest-grid'>
        {OPTIONS.map((tag) => (
          <Text key={tag} className={`interest-chip ${selected.includes(tag) ? 'interest-chip--active' : ''}`} onClick={() => toggle(tag)}>
            {tag}
          </Text>
        ))}
      </View>
      <Button className='interest-save' onClick={() => update.mutate()}>
        保存兴趣
      </Button>
    </View>
  )
}
