import { create } from 'zustand'
import { storage } from '@/storage/storage'
import type { UserDTO } from '@/types/api'

interface AuthState {
  token: string | null
  user: UserDTO | null
  setToken: (token: string) => void
  setUser: (user: UserDTO | null) => void
  logoutLocal: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: storage.getToken(),
  user: null,
  setToken: (token) => {
    storage.setToken(token)
    set({ token })
  },
  setUser: (user) => set({ user }),
  logoutLocal: () => {
    storage.clearToken()
    set({ token: null, user: null })
  }
}))
