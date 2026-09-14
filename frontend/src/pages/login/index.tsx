import { Button, Input, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'
import './index.scss'

export default function LoginPage() {
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const setToken = useAuthStore((state) => state.setToken)
  const sendCode = useMutation({
    mutationFn: () => authApi.sendCode(phone),
    onSuccess: (devCode) => {
      setCode(devCode)
      Taro.showToast({ title: '验证码已填入', icon: 'none' })
    },
    onError: () => Taro.showToast({ title: '验证码发送失败', icon: 'none' })
  })
  const login = useMutation({
    mutationFn: () => authApi.login({ phone, code }),
    onSuccess: (token) => {
      setToken(token)
      Taro.switchTab({ url: '/pages/home/index' })
    },
    onError: () => Taro.showToast({ title: '登录失败，请检查验证码', icon: 'none' })
  })

  return (
    <View className='cl-page login-page'>
      <Text className='login-brand'>CampusLink</Text>
      <Text className='login-title'>回到校园生活</Text>
      <View className='login-form'>
        <Input className='login-input' type='number' value={phone} placeholder='手机号' onInput={(event) => setPhone(event.detail.value)} />
        <View className='login-code-row'>
          <Input className='login-input login-input--code' value={code} placeholder='验证码' onInput={(event) => setCode(event.detail.value)} />
          <Button className='login-code-button' disabled={phone.length !== 11 || sendCode.isPending} onClick={() => sendCode.mutate()}>
            获取
          </Button>
        </View>
        <Button className='login-button' disabled={phone.length !== 11 || !code || login.isPending} onClick={() => login.mutate()}>
          {login.isPending ? '登录中' : '登录'}
        </Button>
      </View>
      <Text className='login-policy'>登录即表示同意用户协议与隐私政策</Text>
    </View>
  )
}
