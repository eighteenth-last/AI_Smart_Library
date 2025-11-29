// src/api/admin/tokenStats.ts
import request from '@/utils/request'

/**
 * Token 统计 DTO
 */
export interface TokenStatsDTO {
  totalStats: TotalTokenStats
  dailyTrend: DailyTokenStats[]
  userRanking: UserTokenStats[]
  roleStats: RoleTokenStats[]
}

export interface TotalTokenStats {
  totalInputTokens: number
  totalOutputTokens: number
  totalTokens: number
  totalChats: number
  avgTokensPerChat: number
  todayTokens: number
  yesterdayTokens: number
  growthRate: number
}

export interface DailyTokenStats {
  date: string
  tokens: number
  chats: number
  avgTokens: number
}

export interface UserTokenStats {
  userId: number
  username: string
  nickname: string
  tokens: number
  chats: number
  rank: number
}

export interface RoleTokenStats {
  role: string
  roleName: string
  tokens: number
  chats: number
  avgTokens: number
  userCount: number
}

/**
 * Token 统计 API
 */
export const tokenStatsAPI = {
  /**
   * 获取完整 Token 统计数据
   */
  getStats: (days: number = 30) => {
    return request.get<TokenStatsDTO>('/admin/token-stats', {
      params: { days }
    })
  },

  /**
   * 获取总 Token 统计
   */
  getTotalStats: () => {
    return request.get<TotalTokenStats>('/admin/token-stats/total')
  },

  /**
   * 获取每日趋势
   */
  getDailyTrend: (days: number = 30) => {
    return request.get<DailyTokenStats[]>('/admin/token-stats/daily-trend', {
      params: { days }
    })
  },

  /**
   * 获取用户排行
   */
  getUserRanking: () => {
    return request.get<UserTokenStats[]>('/admin/token-stats/user-ranking')
  }
}
