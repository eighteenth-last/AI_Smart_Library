// src/api/admin/dashboard.ts
import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

interface OverviewData {
  totalUsers: number
  totalBooks: number
  totalBorrows: number
  activeBorrows: number
  overdueCount: number
  todayNewUsers: number
  todayNewBorrows: number
}

interface HotBook {
  bookId: number
  title: string
  authorName: string
  borrowCount: number
  rank: number
}

interface CategoryStat {
  categoryId: number
  categoryName: string
  borrowCount: number
  percentage: string
}

interface BorrowTrend {
  period: string
  borrowCount: number
  returnCount: number
}

export const dashboardAPI = {
  // 获取系统总览数据
  getOverview: () => {
    return request.get<AxiosResponse<{ data: OverviewData }>>('/dashboard/overview')
  },

  // 获取热门借阅图书
  getHotBooks: (limit?: number) => {
    const params: any = {}
    if (limit) params.limit = limit
    return request.get<AxiosResponse<{ data: HotBook[] }>>('/dashboard/hot-books', { params })
  },

  // 获取分类统计
  getCategoryStats: () => {
    return request.get<AxiosResponse<{ data: CategoryStat[] }>>('/dashboard/category-stats')
  },

  // 获取借阅趋势
  getBorrowTrends: (period?: string, limit?: number) => {
    const params: any = {}
    if (period) params.period = period
    if (limit) params.limit = limit
    return request.get<AxiosResponse<{ data: BorrowTrend[] }>>('/dashboard/borrow-trends', { params })
  },

  // 获取最近操作
  getRecentActivities: (limit?: number) => {
    const params: any = {}
    if (limit) params.limit = limit
    return request.get<AxiosResponse<{ data: any[] }>>('/dashboard/recent-activities', { params })
  }
}