// src/api/user/review.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface Review {
  reviewId: number
  userId: number
  username: string
  nickname: string
  rating: number
  content?: string
  createdAt: string
}

interface ReviewParams {
  rating?: number
  page?: number
  size?: number
}

interface ReviewCreateParams {
  rating: number
  content?: string
}

export const reviewAPI = {
  // 评价图书
  createReview: (bookId: number, data: ReviewCreateParams) => {
    return request.post<AxiosResponse<{ data: { reviewId: number } }>>(`/reviews/${bookId}`, data)
  },

  // 获取图书评价列表
  getReviews: (bookId: number, params?: ReviewParams) => {
    return request.get<AxiosResponse<{ 
      data: { 
        records: Review[]; 
        total: number; 
        page: number; 
        size: number 
      } 
    }>>(`/books/${bookId}/reviews`, { params })
  }
}