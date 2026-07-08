import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 后端统一响应格式
 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  traceId?: string
}

/**
 * Axios 实例：统一前缀、统一错误处理。
 */
const request: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：注入 Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gac_lms_token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理 Result
request.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const { code, message, data } = response.data
    if (code === 0) {
      return data as any
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg =
      status === 401
        ? '登录已过期，请重新登录'
        : status === 403
        ? '无权限访问'
        : error.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
