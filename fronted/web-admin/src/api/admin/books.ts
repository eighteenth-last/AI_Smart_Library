import request from '@/utils/request';

// 图书信息接口
export interface BookInfo {
  bookId: number;
  isbn: string;
  title: string;
  authorId: number;
  authorName?: string;
  categoryId: number;
  categoryName?: string;
  publisher?: string;
  publishYear?: number;
  coverUrl?: string;
  description?: string;
  totalStock: number;
  availableStock: number;
  status: number;
  tagIds?: number[];
  tags?: string[];
  createdAt?: string;
  updatedAt?: string;
}

// 图书列表查询参数
export interface BookListParams {
  keyword?: string;
  categoryId?: number;
  authorId?: number;
  status?: number;
  page?: number;
  size?: number;
}

// 图书列表响应
export interface BookListResponse {
  records: BookInfo[];
  total: number;
  page: number;
  size: number;
}

// 创建图书参数
export interface CreateBookParams {
  isbn: string;
  title: string;
  authorId: number;
  categoryId: number;
  publisher?: string;
  publishYear?: number;
  coverUrl?: string;
  description?: string;
  totalStock: number;
  tagIds?: number[];
}

// 更新图书参数
export interface UpdateBookParams extends Partial<CreateBookParams> {}

// 创建图书响应
export interface CreateBookResponse {
  bookId: number;
}

// 图书API对象
export const bookAPI = {
  // 获取图书列表
  getList: (params?: BookListParams) => {
    return request.get<BookListResponse>('/admin/books', { params });
  },

  // 获取图书详情
  getDetail: (bookId: number) => {
    return request.get<BookInfo>(`/admin/books/${bookId}`);
  },

  // 创建图书
  createBook: (data: CreateBookParams) => {
    return request.post<CreateBookResponse>('/admin/books', data);
  },

  // 更新图书
  updateBook: (bookId: number, data: UpdateBookParams) => {
    return request.put(`/admin/books/${bookId}`, data);
  },

  // 删除图书
  deleteBook: (bookId: number) => {
    return request.delete(`/admin/books/${bookId}`);
  }
};
