import request from '@/utils/request';

export interface AuthorStats {
  totalBooks: number;
  totalBorrows: number;
  totalFavorites: number;
  averageRating: number;
  totalReviews: number;
  pendingQuestions: number;
  topBooks: BookBorrowStats[];
  monthlyTrends: MonthlyBorrowTrend[];
}

export interface BookBorrowStats {
  bookId: number;
  title: string;
  borrowCount: number;
  averageRating: number;
}

export interface MonthlyBorrowTrend {
  month: string;
  borrowCount: number;
}

export async function getAuthorStats(): Promise<AuthorStats> {
  return request.get<AuthorStats>('/author/stats');
}
