import request from '@/utils/request';

export interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  role: string;
  phone?: string;
  email: string;
  status: number;
  createdAt: string;
}

export interface UserListParams {
  keyword?: string;
  role?: string;
  status?: number;
  page?: number;
  size?: number;
}

export interface UserListResponse {
  records: UserInfo[];
  total: number;
  page: number;
  size: number;
}

export function getUserList(params?: UserListParams) {
  return request.get<UserListResponse>('/admin/users', { params });
}

export interface CreateUserParams {
  username: string;
  password: string;
  nickname?: string;
  role: string;
  phone?: string;
  email: string;
  status?: number;
}

export interface CreateUserResponse {
  userId: number;
}

export function createUser(data: CreateUserParams) {
  return request.post<CreateUserResponse>('/admin/users', data);
}

export function updateUser(id: number, data: Partial<CreateUserParams>) {
  return request.put(`/admin/users/${id}`, data);
}

export function deleteUser(id: number) {
  return request.delete(`/admin/users/${id}`);
}
