// src/api/admin/tags.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface Tag {
  tagId: number
  name: string
  category?: string
}

export const tagAPI = {
  // 获取标签列表
  getList: () => {
    return request.get<Tag[]>('tags')
  }
}
