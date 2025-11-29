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
  // 从 localStorage 初始化
  const token = ref<string>(localStorage.getItem('token') || '')
  const savedUserInfo = localStorage.getItem('userInfo')
  const userInfo = ref<UserInfo>(savedUserInfo ? JSON.parse(savedUserInfo) : {})

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

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

  const logout = () => {
    clearUser()
  }

  const isLoggedIn = () => {
    return !!token.value
  }

  return {
    userInfo,
    token,
    setToken,
    setUserInfo,
    setUser,
    clearUser,
    logout,
    isLoggedIn
  }
})