// src/api/admin/reviews.ts
import request from '@/utils/request'

export interface Review {
  reviewId: number
  userId: number
  username: string
  bookId: number
  bookTitle: string
  coverUrl: string
  rating: number
  content: string
  createdAt: string
}

export interface ReviewStats {
  total: number
  fiveStar: number
  fourStar: number
  threeStar: number
  twoStar: number
  oneStar: number
  avgRating: number
}

export interface ReviewListParams {
  userId?: number
  bookId?: number
  rating?: number
  page?: number
  size?: number
}

export const reviewAPI = {
  // 获取评价列表
  getList: (params: ReviewListParams) => {
    return request.get<{ records: Review[], total: number, pages: number }>('admin/reviews', { params })
  },

  // 删除评价
  deleteReview: (id: number) => {
    return request.delete(`admin/reviews/${id}`)
  },

  // 获取评价统计
  getStats: () => {
    return request.get<ReviewStats>('admin/reviews/stats')
  }
}
