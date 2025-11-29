import request from '@/utils/request';

export interface Book {
  bookId: number;
  isbn: string;
  title: string;
  authorId: number;
  authorName: string;
  categoryId: number;
  categoryName: string;
  publisher: string;
  publishYear: number;
  coverUrl: string;
  description: string;
  totalStock: number;
  availableStock: number;
  tags: string[];
  averageRating: number;
  reviewCount: number;
}

export interface BookDetail {
  bookId: number;
  isbn: string;
  title: string;
  author: {
    authorId: number;
    name: string;
    country: string;
    intro: string;
  };
  category: {
    categoryId: number;
    name: string;
  };
  publisher: string;
  publishYear: number;
  coverUrl: string;
  description: string;
  totalStock: number;
  availableStock: number;
  tags: string[];
  averageRating: number;
  reviewCount: number;
}

export interface BookListParams {
  keyword?: string;
  authorId?: number;
  categoryId?: number;
  page?: number;
  size?: number;
}

export interface BookListResponse {
  records: Book[];
  total: number;
  page: number;
  size: number;
}

export interface SearchParams {
  keyword: string;
  type?: 'title' | 'author' | 'isbn';
  page?: number;
  size?: number;
}

export interface Category {
  categoryId: number;
  name: string;
  parentId?: number;
  level: number;
  sortOrder?: number;
  children?: Category[];
  bookCount?: number;
}

export interface RecommendBook {
  bookId: number;
  title: string;
  coverUrl: string;
  similarity: number;
  similarityPercent: string;
  reason: string;
  sharedTags: string[];
}

export function getBookList(params?: BookListParams) {
  return request.get<BookListResponse>('/books', { params });
}

export function getBookDetail(id: number) {
  return request.get<BookDetail>(`/books/${id}`);
}

export function searchBooks(params: SearchParams) {
  return request.get<BookListResponse>('/books/search', { params });
}

export function getRecommendBooks(id: number, limit: number = 5) {
  return request.get<RecommendBook[]>(`/books/${id}/recommend`, { params: { limit } });
}

export function getCategories() {
  return request.get<Category[]>('/categories');
}

export function getAuthors(params?: { keyword?: string; page?: number; size?: number }) {
  return request.get('/authors', { params });
}

export function getAuthorDetail(id: number) {
  return request.get(`/authors/${id}`);
}
