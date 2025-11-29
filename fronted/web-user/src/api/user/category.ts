// src/api/user/category.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface Category {
  categoryId: number
  name: string
  parentId: number | null
  level: number
  children?: Category[]
}

export const categoryAPI = {
  // 获取分类列表
  getList: () => {
    return request.get<AxiosResponse<{ data: Category[] }>>('/categories')
  }
}