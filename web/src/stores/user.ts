import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { userLogin as loginApi, getUserProfile as profileApi } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('user', JSON.stringify(info))
  }

  const login = async (username: string, password: string) => {
    const res = await loginApi({ username, password })
    setToken(res.data.token)
    setUserInfo({
      id: res.data.userId,
      username: res.data.username,
      realName: res.data.realName,
      contact: '',
      role: res.data.role,
      createdAt: ''
    })
    return res.data
  }

  const fetchUserProfile = async () => {
    if (!token.value) return null
    try {
      const res = await profileApi()
      setUserInfo(res.data)
      return res.data
    } catch {
      logout()
      return null
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  const initUser = () => {
    const storedUser = localStorage.getItem('user')
    if (storedUser) {
      userInfo.value = JSON.parse(storedUser)
    }
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    login,
    fetchUserProfile,
    logout,
    initUser
  }
})
