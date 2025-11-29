// src/utils/request.ts
import axios, { type AxiosInstance, type InternalAxiosRequestConfig, type AxiosRequestConfig, type AxiosResponse } from 'axios'

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
        // 直接从localStorage读取token，避免store初始化问题
        const token = localStorage.getItem('token')
        console.log('[Request Interceptor] Token from localStorage:', token ? `${token.substring(0, 20)}...` : 'null')
        if (token) {
          if (config.headers) {
            config.headers.Authorization = `Bearer ${token}`
            console.log('[Request Interceptor] Authorization header set')
          }
        } else {
          console.warn('[Request Interceptor] No token found in localStorage')
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
        // 检查响应数据是否存在
        if (!response.data) {
          return Promise.reject(new Error('响应数据为空'))
        }
        
        const { code, msg, data } = response.data
        
        // 如果没有 code 字段，说明不是标准的 API 响应格式
        if (code === undefined) {
          console.error('响应数据格式不正确:', response.data)
          return Promise.reject(new Error('响应数据格式错误'))
        }
        
        if (code === 200) {
          return data
        } else if (code === 401) {
          // token过期或未授权，清除localStorage并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/author/login'
          return Promise.reject(new Error(msg || '登录已过期，请重新登录'))
        } else {
          return Promise.reject(new Error(msg || '请求失败'))
        }
      },
      (error) => {
        if (error.response?.status === 401) {
          // token过期或未授权，清除localStorage并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          window.location.href = '/author/login'
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