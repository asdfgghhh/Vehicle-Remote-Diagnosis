import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    const showError = response.config.showError !== false
    if (res.code != null && Number(res.code) !== 200) {
      if (showError) {
        ElMessage.error(res.message || '请求失败')
      }
      if (Number(res.code) === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.replace('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    const data = error.response?.data
    const message = data?.message || data?.error || error.message || '网络错误，请确认网关和认证服务已启动'
    if (error.config?.showError !== false) {
      ElMessage.error(message)
    }
    return Promise.reject(new Error(message))
  }
)

export default service
