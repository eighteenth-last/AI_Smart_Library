// src/utils/request.ts
import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { useUserStore } from '../store/user'

interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

class HttpRequest {
  private instance: AxiosInstance

  constructor(baseURL: string) {
    this.instance = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    // 请求拦截器
    this.instance.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const userStore = useUserStore()
        const token = userStore.token
        if (token) {
          if (config.headers) {
            config.headers.Authorization = `Bearer ${token}`
          }
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // 响应拦截器
    this.instance.interceptors.response.use(
      (response) => {
        // 如果是 blob类型，直接返回
        if (response.config.responseType === 'blob') {
          return response.data;
        }
        
        const res = response.data
        // 如果返回的code不是200，则认为是错误
        if (res.code !== 200) {
          throw new Error(res.msg || '请求失败')
        }
        // 返回data字段
        return res.data
      },
      (error) => {
        if (error.response?.status === 401) {
          // token过期或未授权，跳转到登录页
          const userStore = useUserStore()
          userStore.clearUser()
          window.location.href = '/admin/login'
        }
        return Promise.reject(error)
      }
    )
  }

  // 通用请求方法
  public request<T = any>(config: AxiosRequestConfig): Promise<T> {
    return this.instance.request(config)
  }

  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, config)
  }

  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, config)
  }

  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, config)
  }

  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config)
  }
}

// 创建API实例
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
const request = new HttpRequest(baseURL)

export default request