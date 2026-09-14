import Taro from '@tarojs/taro'

const TOKEN_KEY = 'campuslink.token'
const SEARCH_HISTORY_KEY = 'campuslink.search.history'

export const storage = {
  getToken(): string | null {
    return Taro.getStorageSync<string>(TOKEN_KEY) || null
  },
  setToken(token: string): void {
    Taro.setStorageSync(TOKEN_KEY, token)
  },
  clearToken(): void {
    Taro.removeStorageSync(TOKEN_KEY)
  },
  getSearchHistory(): string[] {
    return Taro.getStorageSync<string[]>(SEARCH_HISTORY_KEY) || []
  },
  setSearchHistory(items: string[]): void {
    Taro.setStorageSync(SEARCH_HISTORY_KEY, items.slice(0, 10))
  }
}
