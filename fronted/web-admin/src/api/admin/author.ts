import request from '@/utils/request'

export interface Author {
  authorId: number
  userId?: number
  username?: string
  name: string
  country?: string
  birthYear?: number
  intro?: string
  authStatus: number
  bookCount?: number
  createdAt: string
  updatedAt?: string
}

export interface PageParams {
  page: number
  size: number
  authStatus?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 获取作者列表（管理员）
export function getAuthorsForAdmin(params: PageParams) {
  return request.get<PageResult<Author>>('/admin/authors/list', { params })
}

// 审核作者
export function reviewAuthor(authorId: number, authStatus: number) {
  return request.put(`/admin/authors/review/${authorId}`, null, {
    params: { authStatus }
  })
}

export const authorAPI = {
  getList: getAuthorsForAdmin,  // 添加 getList 别名
  getAuthorsForAdmin,
  reviewAuthor
}
