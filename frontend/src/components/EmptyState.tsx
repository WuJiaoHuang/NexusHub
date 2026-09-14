import { Button, Text, View } from '@tarojs/components'
import './components.scss'

interface EmptyStateProps {
  text: string
  actionText?: string
  onAction?: () => void
}

export function EmptyState({ text, actionText, onAction }: EmptyStateProps) {
  return (
    <View className='empty-state'>
      <Text>{text}</Text>
      {actionText ? (
        <Button className='empty-state__button cl-pressable' onClick={onAction}>
          {actionText}
        </Button>
      ) : null}
    </View>
  )
}
