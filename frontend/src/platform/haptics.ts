import Taro from '@tarojs/taro'

export function successHaptic(): void {
  if (process.env.TARO_ENV === 'weapp') {
    Taro.vibrateShort({ type: 'light' }).catch(() => undefined)
  }
}
