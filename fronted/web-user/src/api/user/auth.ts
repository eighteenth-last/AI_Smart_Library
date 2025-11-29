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

export interface RegisterParams {
  username: string;
  password: string;
  nickname?: string;
  role: 'reader' | 'author';
  phone?: string;
  email: string;
}

export interface RegisterResponse {
  userId: number;
  username: string;
  token: string;
}

export interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  role: string;
  phone?: string;
  email: string;
  status: number;
  createdAt: string;
  profilePicture?: string;
}

export function login(data: LoginParams) {
  return request.post<LoginResponse>('/auth/login', data);
}

export function register(data: RegisterParams) {
  return request.post<RegisterResponse>('/auth/register', data);
}

export function logout() {
  return request.post('/auth/logout');
}

export function getUserProfile() {
  return request.get<UserInfo>('/users/profile');
}

export function updateUserProfile(data: Partial<UserInfo>) {
  return request.put('/users/profile', data);
}

// 导出authAPI对象，方便使用
export const authAPI = {
  login,
  register,
  logout,
  getProfile: getUserProfile,
  updateProfile: updateUserProfile
};
