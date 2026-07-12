import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { getImageUrl } from '../utils/format'

// 公网访问时使用环境变量指定的后端地址（内网穿透请配 .env.local）
const isPublic = typeof window !== 'undefined' && !window.location.hostname.includes('localhost') && !window.location.hostname.includes('127.0.0.1')
const BASE_URL = isPublic
  ? (import.meta.env.VITE_PUBLIC_API_URL || '/api')
  : '/api'

const service = axios.create({
  baseURL: BASE_URL,
  timeout: 10000
})

const IMAGE_FIELDS = new Set(['avatar', 'cover', 'url'])

function normalizeImageUrls(value, key = '') {
  if (Array.isArray(value)) {
    return value.map((item) => normalizeImageUrls(item, key))
  }

  if (value && typeof value === 'object') {
    Object.keys(value).forEach((childKey) => {
      value[childKey] = normalizeImageUrls(value[childKey], childKey)
    })
    return value
  }

  if (typeof value === 'string') {
    if (value.startsWith('/uploads/') || IMAGE_FIELDS.has(key) || key.toLowerCase().includes('image')) {
      return getImageUrl(value)
    }
  }

  return value
}

// 请求拦截器：自动添加 Token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理返回
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    res.data = normalizeImageUrls(res.data)
    return res
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
