import { Button, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { authApi } from '@/api/auth'
import { uploadImage } from '@/api/upload'
import { UserAvatar } from '@/components/UserAvatar'
import { chooseImages, compressImageForAvatar } from '@/platform/media'
import { queryKeys } from '@/queries/keys'
import { useAuthStore } from '@/store/auth'
import './index.scss'

export default function ProfilePage() {
  const queryClient = useQueryClient()
  const logoutLocal = useAuthStore((state) => state.logoutLocal)
  const setUser = useAuthStore((state) => state.setUser)
  const me = useQuery({ queryKey: queryKeys.user.me, queryFn: authApi.me, retry: false })
  const avatarUpload = useMutation({
    mutationFn: async (sourceType: Array<'album' | 'camera'>) => {
      const selected = await chooseImages({ count: 1, sourceType })
      const first = selected[0]
      if (!first) {
        throw new Error('请选择图片')
      }
      const compressed = await compressImageForAvatar(first)
      const uploaded = await uploadImage({ filePath: compressed.path, file: compressed.file, scene: 'AVATAR' })
      return authApi.updateAvatar(uploaded.url)
    },
    onSuccess: (user) => {
      setUser(user)
      queryClient.setQueryData(queryKeys.user.me, user)
      Taro.showToast({ title: '头像更新成功', icon: 'success' })
    },
    onError: () => Taro.showToast({ title: '上传失败，请稍后重试', icon: 'none' })
  })
  const logout = useMutation({
    mutationFn: authApi.logout,
    onSettled: () => {
      logoutLocal()
      Taro.redirectTo({ url: '/pages/login/index' })
    }
  })

  async function handleAvatarTap() {
    if (avatarUpload.isPending) {
      return
    }
    try {
      const action = await Taro.showActionSheet({ itemList: ['拍照', '从相册选择'] })
      avatarUpload.mutate(action.tapIndex === 0 ? ['camera'] : ['album'])
    } catch {
      // User cancelled the action sheet.
    }
  }

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
        <View className='profile-avatar-wrap' onClick={handleAvatarTap}>
          <UserAvatar name={me.data?.nickName} src={me.data?.icon} />
          <View className='profile-avatar-camera'>+</View>
          {avatarUpload.isPending ? (
            <View className='profile-avatar-overlay'>
              <Text>上传中</Text>
            </View>
          ) : null}
        </View>
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
