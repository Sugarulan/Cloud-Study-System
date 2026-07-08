import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户状态（W1 占位，W2 接入真实登录）
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('gac_lms_token') || '')
  const userId = ref<number>(0)
  const userName = ref<string>('')

  const isLogin = computed(() => !!token.value)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('gac_lms_token', t)
  }

  function logout() {
    token.value = ''
    userId.value = 0
    userName.value = ''
    localStorage.removeItem('gac_lms_token')
  }

  return { token, userId, userName, isLogin, setToken, logout }
})
