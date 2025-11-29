import request from '@/utils/request';

/**
 * 会话信息接口
 */
export interface SessionInfo {
  sessionId: string;
  sessionTitle: string;
  preview: string;
  totalMessages: number;
  lastActiveAt: string;
  createdAt: string;
}

/**
 * 保存会话请求
 */
export interface SaveSessionRequest {
  sessionId: string;
  sessionTitle: string;
  totalMessages: number;
  contextMessages?: string;
}

/**
 * 获取用户会话列表
 */
export const getSessions = () => {
  return request.get<SessionInfo[]>('/ai/sessions');
};

/**
 * 保存或更新会话
 */
export const saveSession = (data: SaveSessionRequest) => {
  return request.post<boolean>('/ai/sessions', data);
};

/**
 * 删除会话
 */
export const deleteSession = (sessionId: string) => {
  return request.delete<boolean>(`/ai/sessions/${sessionId}`);
};

/**
 * 清空所有会话
 */
export const clearAllSessions = () => {
  return request.delete<boolean>('/ai/sessions');
};
