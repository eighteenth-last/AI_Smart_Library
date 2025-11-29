import request from '@/utils/request'

export interface UploadResponse {
  url: string
  filename: string
}

export const uploadAPI = {
  // 上传头像
  uploadAvatar: async (file: File): Promise<UploadResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await request.post<UploadResponse>('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 拦截器已经提取了 data，直接返回 response
    return response
  },

  // 上传图书封面
  uploadBookCover: async (file: File): Promise<UploadResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await request.post<UploadResponse>('/upload/book-cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 拦截器已经提取了 data，直接返回 response
    return response
  }
}
