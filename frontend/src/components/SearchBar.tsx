import { Input, View } from '@tarojs/components'
import './components.scss'

interface SearchBarProps {
  value?: string
  placeholder?: string
  onInput?: (value: string) => void
  onFocus?: () => void
}

export function SearchBar({ value, placeholder = '搜索图书馆、食堂、活动...', onInput, onFocus }: SearchBarProps) {
  return (
    <View className='search-bar'>
      <Input
        value={value}
        placeholder={placeholder}
        placeholderClass='search-bar__placeholder'
        onInput={(event) => onInput?.(event.detail.value)}
        onFocus={onFocus}
      />
    </View>
  )
}
