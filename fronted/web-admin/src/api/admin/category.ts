// src/api/admin/category.ts
import request from '@/utils/request'

interface Category {
  categoryId: number
  name: string
  parentId: number | null
  level: number
  children?: Category[]
}

interface CreateCategoryParams {
  name: string
  parentId?: number | null
}

export const categoryAPI = {
  // 获取分类列表
  getList: () => {
    return request.get<Category[]>('categories')
  },

  // 新增分类
  createCategory: (data: CreateCategoryParams) => {
    return request.post<{ categoryId: number }>('categories', data)
  },

  // 编辑分类
  updateCategory: (id: number, data: Partial<CreateCategoryParams>) => {
    return request.put(`categories/${id}`, data)
  },

  // 删除分类
  deleteCategory: (id: number) => {
    return request.delete(`categories/${id}`)
  }
}