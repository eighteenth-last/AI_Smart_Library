import request from '@/utils/request';

/**
 * 收藏图书
 */
export function addFavorite(bookId: number) {
  return request.post(`/favorites/${bookId}`);
}

/**
 * 取消收藏
 */
export function removeFavorite(bookId: number) {
  return request.delete(`/favorites/${bookId}`);
}

/**
 * 检查是否收藏
 */
export function checkFavorite(bookId: number) {
  return request.get<boolean>(`/favorites/${bookId}/check`);
}

/**
 * 获取我的收藏列表
 */
export interface FavoriteBook {
  favId: number;
  bookId: number;
  bookTitle: string;
  authorName: string;
  coverUrl: string;
  createdAt: string;
}

export function getMyFavorites(page: number = 1, size: number = 10) {
  return request.get<{
    records: FavoriteBook[];
    total: number;
    current: number;
    size: number;
  }>('/user/favorites', {
    params: { page, size }
  });
}
