// src/api/user/notification.ts
import request from '@/utils/request'

interface Notification {
  notificationId: number
  type: string
  title: string
  content: string
  relatedId?: number
  isRead: number
  createdAt: string
}

interface NotificationParams {
  type?: string
  isRead?: number
  page?: number
  size?: number
}

interface NotificationListResponse {
  records: Notification[]
  total: number
  page: number
  size: number
  unreadCount: number
}

export const notificationAPI = {
  // 获取通知列表
  getList: (params?: NotificationParams) => {
    return request.get<NotificationListResponse>('/notifications', { params })
  },

  // 标记通知已读
  markAsRead: (id: number) => {
    return request.put(`/notifications/${id}/read`)
  },

  // 批量标记已读
  markAllAsRead: () => {
    return request.put(`/notifications/read-all`)
  }
}