import request from '@/utils/request';

export interface ChatParams {
  question: string;
  sessionId?: string;
}

export interface ChatResponse {
  answer: string;
  source: 'knowledge_base' | 'deepseek';
  kbId?: number;
  sessionId: string;
  tokensUsed: number;
  inputTokens?: number;
  outputTokens?: number;
  totalTokens?: number;
  contextTokens?: number;
}

export interface ChatHistoryItem {
  logId: number;
  sessionId: string;
  question: string;
  answer: string;
  source: 'knowledge_base' | 'deepseek';
  createdAt: string;
}

export interface ChatHistoryResponse {
  records: ChatHistoryItem[];
  total: number;
  page: number;
  size: number;
}

export interface SessionVO {
  sessionId: string;
  title: string;
  messageCount: number;
  lastActiveAt: string;
}

/**
 * AI对话
 */
export function chatWithAI(data: ChatParams) {
  return request.post<ChatResponse>('/ai/chat', data, {
    timeout: 90000 // 90秒
  });
}

/**
 * 获取对话历史
 */
export function getChatHistory(params?: {
  sessionId?: string;
  page?: number;
  size?: number;
}) {
  return request.get<ChatHistoryResponse>('/ai/history', { params });
}

/**
 * 获取会话列表
 */
export function getSessions() {
  return request.get<SessionVO[]>('/ai/sessions');
}

/**
 * 保存会话
 */
export function saveSession(data: {
  sessionId: string;
  title: string;
}) {
  return request.post('/ai/sessions', data);
}

/**
 * 删除会话
 */
export function deleteSession(sessionId: string) {
  return request.delete(`/ai/sessions/${sessionId}`);
}

/**
 * 清空所有会话
 */
export function clearAllSessions() {
  return request.delete('/ai/sessions');
}

/**
 * 获取会话消息历史
 */
export function getSessionMessages(sessionId: string, page: number = 1, size: number = 50) {
  return request.get<ChatHistoryResponse>('/ai/history', {
    params: { sessionId, page, size }
  });
}
