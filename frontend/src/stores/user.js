import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const refreshToken = ref('')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  function initFromStorage() {
    token.value = localStorage.getItem('token') || ''
    refreshToken.value = localStorage.getItem('refreshToken') || ''
    const savedUser = localStorage.getItem('userInfo')
    if (savedUser) {
      try {
        userInfo.value = JSON.parse(savedUser)
      } catch (e) {
        userInfo.value = null
      }
    }
  }

  async function login(username, password) {
    const res = await authApi.login({ username, password })
    if (res.code === 200) {
      token.value = res.data.token
      refreshToken.value = res.data.refreshToken
      userInfo.value = res.data
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
    return res
  }

  async function register(username, password, nickname) {
    const res = await authApi.register({ username, password, nickname })
    if (res.code === 200) {
      token.value = res.data.token
      refreshToken.value = res.data.refreshToken
      userInfo.value = res.data
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
    return res
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch (e) {
      // ignore
    }
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    const res = await userApi.getCurrentUser()
    if (res.code === 200) {
      userInfo.value = { ...userInfo.value, ...res.data }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
    return res
  }

  async function refreshUserToken() {
    try {
      const res = await authApi.refreshToken(refreshToken.value)
      if (res.code === 200) {
        token.value = res.data.token
        refreshToken.value = res.data.refreshToken
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('refreshToken', res.data.refreshToken)
        return true
      }
    } catch (e) {
      // refresh failed
    }
    return false
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    initFromStorage,
    login,
    register,
    logout,
    fetchUserInfo,
    refreshUserToken
  }
})
