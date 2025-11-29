import axios, { type AxiosResponse } from 'axios';
import { useUserStore } from '../store/user';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60000, // 增加到 60 秒，适配 AI 对话耗时
});

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, msg, data } = response.data;
    if (code === 200) {
      return data;
    } else if (code === 401) {
      const userStore = useUserStore();
      userStore.logout();
      return Promise.reject(new Error('登录已过期，请重新登录'));
    } else {
      return Promise.reject(new Error(msg || '请求失败'));
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore();
      userStore.logout();
      return Promise.reject(new Error('登录已过期，请重新登录'));
    }
    
    // 处理超时错误
    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      return Promise.reject(new Error('请求超时，请稍后再试'));
    }
    
    return Promise.reject(error);
  }
);

export default request;
