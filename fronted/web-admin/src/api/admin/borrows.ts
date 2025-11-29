// src/api/admin/borrows.ts
import request from '@/utils/request'

export interface BorrowRecord {
  recordId: number
  userId: number
  username: string
  bookId: number
  bookTitle: string
  coverUrl: string
  borrowTime: string | null
  dueTime: string | null
  returnTime: string | null
  status: string // pending, borrowed, returned, overdue, rejected
  isRenewed: number
  overdueFee: number
  isOverdue: boolean
  daysRemaining?: number
  approveTime?: string | null
  approverId?: number | null
  rejectReason?: string | null
}

export interface BorrowListParams {
  userId?: number
  bookId?: number
  status?: string
  page?: number
  size?: number
}

export interface BorrowApproveDTO {
  recordId: number
  approved: boolean
  rejectReason?: string
}

export const borrowAPI = {
  // 获取借阅记录列表
  getList: (params: BorrowListParams) => {
    return request.get<{ records: BorrowRecord[], total: number, pages: number }>('admin/borrows', { params })
  },

  // 获取逾期记录
  getOverdueList: (params: { page?: number, size?: number }) => {
    return request.get<{ records: BorrowRecord[], total: number, pages: number }>('admin/borrows/overdue', { params })
  },

  // 审批借阅申请
  approveBorrow: (data: BorrowApproveDTO) => {
    return request.post<boolean>('/borrow/approve', data)
  }
}
