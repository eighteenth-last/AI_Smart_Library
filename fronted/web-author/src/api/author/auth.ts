import request from '@/utils/request';

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  nickname: string;
  role: string;
  token: string;
  expiresIn: number;
}

export interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  role: string;
  phone?: string;
  email: string;
  profilePicture?: string;
  status: number;
  createdAt: string;
}

export interface UpdateProfileDTO {
  username: string;
  email: string;
  phone?: string;
  password?: string;
  profilePicture?: string;
}

export function login(data: LoginParams) {
  return request.post<LoginResponse>('/auth/login', data);
}

export function logout() {
  return request.post('/auth/logout');
}

export async function getUserProfile(): Promise<UserInfo> {
  return request.get<UserInfo>('/users/profile');
}

export async function updateProfile(data: UpdateProfileDTO): Promise<UserInfo> {
  return request.put<UserInfo>('/users/profile', data);
}

export interface AuthorRegisterParams {
  name: string;
  country?: string;
  birthYear?: number;
  intro?: string;
}

export interface AuthorRegisterResponse {
  authorId: number;
}

export function registerAsAuthor(data: AuthorRegisterParams) {
  return request.post<AuthorRegisterResponse>('/author/register', data);
}

export interface AuthorDashboard {
  totalBooks: number;
  totalBorrows: number;
  totalReviews: number;
  averageRating: number;
  pendingQuestions: number;
  monthlyBorrowTrend: Array<{
    month: string;
    count: number;
  }>;
}

export function getAuthorDashboard() {
  return request.get<AuthorDashboard>('/author/dashboard');
}
