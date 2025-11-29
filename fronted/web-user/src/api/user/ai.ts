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

export function chatWithAI(data: ChatParams) {
  // AI 对话需要较长时间（RAG 检索 + DeepSeek 生成），单独设置 90 秒超时
  return request.post<ChatResponse>('/ai/chat', data, {
    timeout: 90000 // 90 秒
  });
}

export function getChatHistory(params?: {
  sessionId?: string;
  page?: number;
  size?: number;
}) {
  return request.get<ChatHistoryResponse>('/ai/history', { params });
}

export interface QuickQuestion {
  id: number;
  question: string;
  category: string;
}

export function getQuickQuestions() {
  // 返回静态快捷问题列表，不调用后端接口
  const quickQuestions: QuickQuestion[] = [
    { id: 1, question: '图书馆开放时间是什么？', category: '图书馆业务' },
    { id: 2, question: '如何借阅图书？', category: '图书馆业务' },
    { id: 3, question: '推荐一些经典科幻小说', category: '图书推荐' },
    { id: 4, question: '如何成为签约作者？', category: '作者问答' },
    { id: 5, question: '借书证丢失怎么办？', category: '图书馆业务' },
    { id: 6, question: '有哪些适合青少年阅读的书籍？', category: '图书推荐' }
  ];
  return Promise.resolve(quickQuestions);
}

export function clearChatHistory() {
  return request.delete('/ai/history');
}
