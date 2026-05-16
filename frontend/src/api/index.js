import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      ElMessage.error(data?.message || `请求失败 (${status})`)
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

// 认证API
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  logout: () => request.post('/auth/logout'),
  refreshToken: (token) => request.post('/auth/refresh', token),
}

// 用户API
export const userApi = {
  getCurrentUser: () => request.get('/user/me'),
  updateUser: (data) => request.put('/user/me', data),
  changePassword: (data) => request.put('/user/password', data),
}

// 训练API
export const trainingApi = {
  create: (data) => request.post('/training', data),
  update: (id, data) => request.put(`/training/${id}`, data),
  delete: (id) => request.delete(`/training/${id}`),
  getDetail: (id) => request.get(`/training/${id}`),
  getList: (params) => request.get('/training', { params }),
  getStats: () => request.get('/training/stats'),
  getCalendar: (year, month) => request.get('/training/calendar', { params: { year, month } }),
  getStreak: () => request.get('/training/streak'),
  getExercises: (params) => request.get('/training/exercises', { params }),
  getExerciseCategories: () => request.get('/training/exercises/categories'),
  addExercise: (data) => request.post('/training/exercises', data),
  getTemplates: () => request.get('/training/templates'),
  getTemplateDetail: (id) => request.get(`/training/templates/${id}`),
  createTemplate: (data) => request.post('/training/templates', data),
  updateTemplate: (id, data) => request.put(`/training/templates/${id}`, data),
  deleteTemplate: (id) => request.delete(`/training/templates/${id}`),
}

// 饮食API
export const dietApi = {
  add: (data) => request.post('/diet', data),
  delete: (id) => request.delete(`/diet/${id}`),
  getDaily: (date) => request.get('/diet/daily', { params: { date } }),
  getDailySummary: (date) => request.get('/diet/daily/summary', { params: { date } }),
  getStats: () => request.get('/diet/stats'),
  searchFood: (params) => request.get('/diet/food/search', { params }),
  addCustomFood: (data) => request.post('/diet/food', data),
  searchOnline: (keyword) => request.get('/diet/food/online', { params: { keyword } }),
}

// 身体数据API
export const bodyApi = {
  add: (data) => request.post('/body', data),
  update: (id, data) => request.put(`/body/${id}`, data),
  delete: (id) => request.delete(`/body/${id}`),
  getList: (params) => request.get('/body', { params }),
  getLatest: () => request.get('/body/latest'),
}

// 仪表盘API
export const dashboardApi = {
  getDashboard: () => request.get('/dashboard'),
}

// 成就API
export const achievementApi = {
  getList: () => request.get('/achievement'),
  check: () => request.post('/achievement/check'),
  getPoints: () => request.get('/achievement/points'),
}

// 饮水API
export const waterApi = {
  add: (amount) => request.post('/water', null, { params: { amount } }),
  getToday: () => request.get('/water/today'),
  getDaily: (date) => request.get('/water/daily', { params: { date } }),
}

// 睡眠API
export const sleepApi = {
  add: (data) => request.post('/sleep', data),
  getList: (params) => request.get('/sleep', { params }),
  getLatest: () => request.get('/sleep/latest'),
}

export default request
