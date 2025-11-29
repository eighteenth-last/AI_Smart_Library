// src/api/user/borrow.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface BorrowRecord {
  recordId: number
  bookId: number
  bookTitle: string
  coverUrl?: string
  borrowTime: string
  dueTime: string
  returnTime?: string
  status: string
  isOverdue: boolean
  daysRemaining?: number
}

interface BorrowHistoryParams {
  status?: string
  page?: number
  size?: number
}

export const borrowAPI = {
  // 借阅图书
  borrowBook: (bookId: number) => {
    return request.post<AxiosResponse<{ data: BorrowRecord }>>(`/borrow/${bookId}`)
  },

  // 归还图书
  returnBook: (recordId: number) => {
    return request.post<AxiosResponse<{ 
      data: { 
        recordId: number; 
        returnTime: string; 
        overdueFee: number; 
        isOverdue: boolean 
      } 
    }>>(`/return/${recordId}`)
  },

  // 续借图书
  renewBook: (recordId: number) => {
    return request.post<AxiosResponse<{ data: { recordId: number; newDueTime: string } }>>(`/borrow/renew/${recordId}`)
  },

  // 获取个人借阅记录
  getBorrowHistory: (params?: BorrowHistoryParams) => {
    return request.get<AxiosResponse<{ 
      data: { 
        records: BorrowRecord[]; 
        total: number; 
        page: number; 
        size: number 
      } 
    }>>('/user/borrows', { params })
  }
}