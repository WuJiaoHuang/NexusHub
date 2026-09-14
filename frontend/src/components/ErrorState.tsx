import { Button, Text, View } from '@tarojs/components'
import './components.scss'

interface ErrorStateProps {
  message?: string
  onRetry?: () => void
}

export function ErrorState({ message = '网络好像开了小差', onRetry }: ErrorStateProps) {
  return (
    <View className='error-state'>
      <Text>{message}</Text>
      {onRetry ? (
        <Button className='error-state__button cl-pressable' onClick={onRetry}>
          重试
        </Button>
      ) : null}
    </View>
  )
}
