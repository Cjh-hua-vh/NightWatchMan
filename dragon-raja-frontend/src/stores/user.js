import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getInfo, logout as logoutApi } from '../api/auth'

// 用户状态管理
export const useUserStore = defineStore('user', () => {
  // Token
  const token = ref(localStorage.getItem('token') || '')

  // 用户信息
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 是否已登录
  const isLoggedIn = computed(() => !!token.value)

  // 是否是管理员
  const isAdmin = computed(() => userInfo.value.role === 'ADMIN')

  // 是否有审核权限（ADMIN 或 AUDITOR）
  const canAudit = computed(() => userInfo.value.role === 'ADMIN' || userInfo.value.role === 'AUDITOR')

  /**
   * 设置 Token 和用户信息（登录成功后调用）
   * @param {string} tokenValue - JWT Token
   * @param {Object} info - 用户信息
   */
  function setToken(tokenValue, info) {
    token.value = tokenValue
    userInfo.value = info
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  /**
   * 获取当前用户信息（从后端拉取最新）
   */
  async function getUserInfo() {
    try {
      const res = await getInfo()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      return res.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  /**
   * 更新本地用户信息
   * @param {Object} info - 新的用户信息
   */
  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  /**
   * 登出（调用后端API清除在线状态，再清除本地数据）
   */
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    canAudit,
    setToken,
    getUserInfo,
    updateUserInfo,
    logout
  }
})
