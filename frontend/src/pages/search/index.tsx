import { Text, View } from '@tarojs/components'
import { useQuery } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { placeApi } from '@/api/place'
import { EmptyState } from '@/components/EmptyState'
import { ErrorState } from '@/components/ErrorState'
import { PlaceCard } from '@/components/PlaceCard'
import { SearchBar } from '@/components/SearchBar'
import { storage } from '@/storage/storage'
import { debounce } from '@/utils/debounce'
import './index.scss'

export default function SearchPage() {
  const [input, setInput] = useState('')
  const [keyword, setKeyword] = useState('')
  const [history, setHistory] = useState(storage.getSearchHistory())
  const debouncedSetKeyword = useMemo(() => debounce(setKeyword, 300), [])
  const result = useQuery({
    queryKey: ['places', 'search', keyword],
    queryFn: () => placeApi.search({ keyword, current: 1, size: 10 }),
    enabled: keyword.trim().length > 0
  })

  function commitSearch(value: string) {
    setInput(value)
    debouncedSetKeyword(value)
    if (value.trim()) {
      const next = [value.trim(), ...history.filter((item) => item !== value.trim())].slice(0, 10)
      setHistory(next)
      storage.setSearchHistory(next)
    }
  }

  return (
    <View className='cl-page search-page'>
      <SearchBar value={input} onInput={commitSearch} />
      {!keyword ? (
        <>
          <Text className='search-title'>历史搜索</Text>
          <View className='search-history'>
            {history.map((item) => (
              <Text key={item} onClick={() => commitSearch(item)}>
                {item}
              </Text>
            ))}
          </View>
          <Text className='search-title'>热门搜索</Text>
          <View className='search-history'>
            {['图书馆', '食堂', '篮球场', '技术分享'].map((item) => (
              <Text key={item} onClick={() => commitSearch(item)}>
                {item}
              </Text>
            ))}
          </View>
        </>
      ) : null}
      {result.isError ? <ErrorState onRetry={() => result.refetch()} /> : null}
      {result.data?.records.length === 0 ? <EmptyState text='没有找到相关校园地点' /> : null}
      {result.data?.records.map((place) => <PlaceCard key={place.id} place={place} />)}
    </View>
  )
}
