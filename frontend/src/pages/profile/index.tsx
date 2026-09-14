import { Button, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation, useQuery } from '@tanstack/react-query'
import { authApi } from '@/api/auth'
import { UserAvatar } from '@/components/UserAvatar'
import { queryKeys } from '@/queries/keys'
import { useAuthStore } from '@/store/auth'
import './index.scss'

export default function ProfilePage() {
  const logoutLocal = useAuthStore((state) => state.logoutLocal)
  const me = useQuery({ queryKey: queryKeys.user.me, queryFn: authApi.me, retry: false })
  const logout = useMutation({
    mutationFn: authApi.logout,
    onSettled: () => {
      logoutLocal()
      Taro.redirectTo({ url: '/pages/login/index' })
    }
  })

  if (me.isError) {
    return (
      <View className='cl-page profile-page'>
        <View className='login-card'>
          <Text className='login-card__title'>回到校园生活</Text>
          <Button className='profile-button' onClick={() => Taro.navigateTo({ url: '/pages/login/index' })}>
            登录 CampusLink
          </Button>
        </View>
      </View>
    )
  }

  return (
    <View className='cl-page profile-page'>
      <View className='profile-head'>
        <UserAvatar name={me.data?.nickName} />
        <View>
          <Text className='profile-name'>{me.data?.nickName || '校园同学'}</Text>
          <Text className='profile-id'>STUDENT ID / {me.data?.id || '--'}</Text>
        </View>
      </View>
      <View className='profile-menu'>
        <Text onClick={() => Taro.navigateTo({ url: '/pages/my-events/index' })}>我的活动</Text>
        <Text onClick={() => Taro.navigateTo({ url: '/pages/interests/index' })}>我的兴趣</Text>
        <Text>我的动态</Text>
        <Text>我的关注</Text>
        <Text>关于 CampusLink</Text>
      </View>
      <Button className='profile-button profile-button--ghost' onClick={() => logout.mutate()}>
        退出登录
      </Button>
    </View>
  )
}
