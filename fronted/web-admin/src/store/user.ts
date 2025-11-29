// src/store/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  userId?: number
  username?: string
  nickname?: string
  role?: string
  phone?: string
  email?: string
  profilePicture?: string
  status?: number
  createdAt?: string
  token?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo>({})
  const token = ref<string>('')

  // 初始化时从 localStorage 读取
  const initStore = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')
    
    if (savedToken) {
      token.value = savedToken
    }
    
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo)
      } catch (e) {
        console.error('Failed to parse userInfo from localStorage', e)
      }
    }
  }

  // 立即初始化
  initStore()

  const setUser = (user: UserInfo) => {
    userInfo.value = user
    // 总是更新localStorage，不管是否有token
    localStorage.setItem('userInfo', JSON.stringify(user))
    
    // 如果有token，也更新token
    if (user.token) {
      token.value = user.token
      localStorage.setItem('token', user.token)
    }
  }

  const clearUser = () => {
    userInfo.value = {}
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const isLoggedIn = () => {
    return !!token.value || !!localStorage.getItem('token')
  }

  const logout = () => {
    clearUser()
  }

  return {
    userInfo,
    token,
    setUser,
    clearUser,
    isLoggedIn,
    logout
  }
})