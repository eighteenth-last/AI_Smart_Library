// src/api/admin/notifications.ts
import request from '@/utils/request'

export interface Notification {
  notificationId: number
  userId: number
  username: string
  type: string
  typeName: string
  title: string
  content: string
  relatedId: number | null
  isRead: number
  createdAt: string
}

export interface NotificationStats {
  total: number
  unread: number
  systemAnnouncement: number
  borrowDue: number
  overdue: number
}

export interface NotificationListParams {
  userId?: number
  type?: string
  isRead?: number
  page?: number
  size?: number
}

export interface CreateNotificationDTO {
  userId?: number | null
  type: string
  title: string
  content: string
  relatedId?: number | null
}

export const notificationAPI = {
  // 获取通知列表
  getList: (params: NotificationListParams) => {
    return request.get<{ records: Notification[], total: number, pages: number }>('admin/notifications', { params })
  },

  // 发送通知
  sendNotification: (data: CreateNotificationDTO) => {
    return request.post('admin/notifications', data)
  },

  // 删除通知
  deleteNotification: (id: number) => {
    return request.delete(`admin/notifications/${id}`)
  },

  // 获取通知统计
  getStats: () => {
    return request.get<NotificationStats>('admin/notifications/stats')
  }
}
