// src/api/user/browse.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface BrowseHistory {
  historyId: number
  bookId: number
  bookTitle: string
  coverUrl?: string
  viewCount: number
  lastView: string
}

interface BrowseHistoryParams {
  page?: number
  size?: number
}

export const browseAPI = {
  // 记录浏览历史
  recordBrowse: (bookId: number) => {
    return request.post<AxiosResponse<null>>(`/browse/${bookId}`)
  },

  // 获取浏览历史列表
  getBrowseHistory: (params?: BrowseHistoryParams) => {
    return request.get<AxiosResponse<{ 
      data: { 
        records: BrowseHistory[]; 
        total: number; 
        page: number; 
        size: number 
      } 
    }>>('/user/browse-history', { params })
  }
}