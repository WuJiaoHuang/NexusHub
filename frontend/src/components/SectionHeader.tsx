import { Text, View } from '@tarojs/components'
import './components.scss'

interface SectionHeaderProps {
  eyebrow?: string
  title: string
  action?: string
}

export function SectionHeader({ eyebrow, title, action }: SectionHeaderProps) {
  return (
    <View className='section-header'>
      <View>
        {eyebrow ? <Text className='section-header__eyebrow'>{eyebrow}</Text> : null}
        <Text className='section-header__title'>{title}</Text>
      </View>
      {action ? <Text className='section-header__action'>{action}</Text> : null}
    </View>
  )
}
