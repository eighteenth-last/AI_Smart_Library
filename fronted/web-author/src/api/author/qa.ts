// src/api/author/qa.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface AuthorQA {
  qaId: number
  userId: number
  username: string
  question: string
  answer?: string
  status: string
  isPublic: number
  createdAt: string
  answeredAt?: string
}

interface QAListParams {
  status?: string
  page?: number
  size?: number
}

interface AnswerQAParams {
  qaId: number
  answer: string
  isPublic?: number
}

interface AuthorDashboardData {
  totalBooks: number
  totalBorrows: number
  totalReviews: number
  averageRating: number
  pendingQuestions: number
  monthlyBorrowTrend: {
    month: string
    count: number
  }[]
}

export const qaAPI = {
  // 获取读者提问列表
  getQuestions: (params?: QAListParams) => {
    return request.get<{ 
      records: AuthorQA[]; 
      total: number; 
      page: number; 
      size: number 
    }>('/author/questions', { params })
  },

  // 回复读者提问
  answerQuestion: (data: AnswerQAParams) => {
    return request.post<{ qaId: number; kbId: number }>('/author/answer', data)
  },

  // 获取作者数据看板
  getDashboard: () => {
    return request.get<AuthorDashboardData>('/author/dashboard')
  }
}