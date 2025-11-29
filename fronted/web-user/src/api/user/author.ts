// src/api/user/author.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface Author {
  authorId: number
  name: string
  country?: string
  birthYear?: number
  intro?: string
  bookCount: number
}

interface AuthorDetail {
  authorId: number
  name: string
  country?: string
  birthYear?: number
  intro?: string
  books: {
    bookId: number
    title: string
    coverUrl?: string
    publishYear?: number
  }[]
  totalBorrows: number
  averageRating: number
}

interface AuthorSearchParams {
  keyword?: string
  page?: number
  size?: number
}

export const authorAPI = {
  // 获取作者列表
  getList: (params?: AuthorSearchParams) => {
    return request.get<AxiosResponse<{ 
      data: { 
        records: Author[]; 
        total: number; 
        page: number; 
        size: number 
      } 
    }>>('/authors', { params })
  },

  // 获取作者详情
  getDetail: (id: number) => {
    return request.get<AxiosResponse<{ data: AuthorDetail }>>(`/authors/${id}`)
  }
}