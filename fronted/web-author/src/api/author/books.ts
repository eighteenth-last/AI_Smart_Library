import request from '@/utils/request';

export interface AuthorBook {
  bookId: number;
  title: string;
  isbn: string;
  publisher: string;
  publishYear: number;
  coverUrl: string;
  totalStock: number;
  availableStock: number;
  borrowCount: number;
  averageRating: number;
  status: number;
}

export async function getAuthorBooks(keyword?: string): Promise<AuthorBook[]> {
  const params: any = {};
  if (keyword) {
    params.keyword = keyword;
  }
  return request.get<AuthorBook[]>('/author/books', { params });
}
