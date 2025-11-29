import request from '@/utils/request';

export interface KnowledgeBase {
  kbId: number;
  name: string;
  description?: string;
  vectorDimension: number;
  indexType: string;
  documentCount?: number;
  status: number;
  createdAt: string;
  updatedAt?: string;
}

export interface KnowledgeBaseCreateDTO {
  name: string;
  description?: string;
  vectorDimension: number;
  indexType: string;
  status: number;
}

export interface KnowledgeBaseUpdateDTO {
  name?: string;
  description?: string;
  vectorDimension?: number;
  indexType?: string;
  status?: number;
}

export interface PageParams {
  page: number;
  size: number;
  keyword?: string;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 获取知识库列表
export function getKnowledgeBaseList(params: PageParams) {
  return request.get<PageResult<KnowledgeBase>>('/admin/knowledge-base/list', { params });
}

// 获取知识库详情
export function getKnowledgeBase(kbId: number) {
  return request.get<KnowledgeBase>(`/admin/knowledge-base/${kbId}`);
}

// 创建知识库
export function createKnowledgeBase(data: KnowledgeBaseCreateDTO) {
  return request.post<KnowledgeBase>('/admin/knowledge-base', data);
}

// 更新知识库
export function updateKnowledgeBase(kbId: number, data: KnowledgeBaseUpdateDTO) {
  return request.put<KnowledgeBase>(`/admin/knowledge-base/${kbId}`, data);
}

// 删除知识库
export function deleteKnowledgeBase(kbId: number) {
  return request.delete(`/admin/knowledge-base/${kbId}`);
}

// 更新知识库状态
export function updateKnowledgeBaseStatus(kbId: number, status: number) {
  return request.put(`/admin/knowledge-base/${kbId}/status`, { status });
}

// ==================== 导入导出相关 ====================

/**
 * 导入结果
 */
export interface ImportResultVO {
  totalCount: number;
  successCount: number;
  failedCount: number;
  errorDetails?: Array<{
    rowNumber: number;
    question: string;
    errorMessage: string;
  }>;
}

/**
 * 批量导入知识库（Excel/CSV）
 */
export function importKnowledge(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  
  return request.post<ImportResultVO>('/ai/admin/knowledge/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 导出知识库为 Excel
 */
export async function exportToExcel(category?: string) {
  const params: Record<string, string> = {};
  if (category) {
    params.category = category;
  }
  
  try {
    const response = await request.get('/ai/admin/knowledge/export/excel', {
      params,
      responseType: 'blob',
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      }
    });
    
    // 创建下载链接
    const blob = new Blob([response as any], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    // 生成文件名
    const filename = `知识库导出_${new Date().getTime()}.xlsx`;
    link.download = filename;
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('导出 Excel 失败:', error);
    throw error;
  }
}

/**
 * 导出知识库为 CSV
 */
export async function exportToCSV(category?: string) {
  const params: Record<string, string> = {};
  if (category) {
    params.category = category;
  }
  
  try {
    const response = await request.get('/ai/admin/knowledge/export/csv', {
      params,
      responseType: 'blob',
      headers: {
        'Accept': 'text/csv'
      }
    });
    
    // 创建下载链接
    const blob = new Blob([response as any], { type: 'text/csv;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    // 生成文件名
    const filename = `知识库导出_${new Date().getTime()}.csv`;
    link.download = filename;
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('导出 CSV 失败:', error);
    throw error;
  }
}
