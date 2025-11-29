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
  status: number;
  createdAt: string;
  profilePicture?: string;
}

export interface UpdateProfileDTO {
  username?: string;
  password?: string;
  email?: string;
  phone?: string;
  profilePicture?: string;
}

export function login(data: LoginParams) {
  return request.post<LoginResponse>('/auth/login', data);
}

export function logout() {
  return request.post('/auth/logout');
}

export async function getUserProfile(): Promise<UserInfo> {
  const response = await request.get<UserInfo>('/users/profile');
  return response;
}

export async function updateProfile(data: UpdateProfileDTO): Promise<UserInfo> {
  const response = await request.put<UserInfo>('/users/profile', data);
  return response;
}
