import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',  // 使用相对路径，让 Vite 代理处理
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        console.log('Request:', config.method, config.url, config.data)
        return config
    },
    error => {
        console.error('Request error:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        console.log('Response:', res)

        if (res.code !== 200) {
            ElMessage.error(res.msg || '请求失败')
            if (res.code === 401) {
                // 未登录，跳转到登录页
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                window.location.href = '/login'
            }
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        console.error('Response error:', error)
        let message = '网络错误'

        if (error.code === 'ECONNABORTED') {
            message = '请求超时'
        } else if (error.response) {
            message = error.response.data?.msg || `服务器错误 ${error.response.status}`
        } else if (error.request) {
            message = '无法连接到服务器，请确保后端服务已启动'
        }

        ElMessage.error(message)
        return Promise.reject(error)
    }
)

export default request