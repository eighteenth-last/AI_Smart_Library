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
    
    return request.post<UploadResponse>('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传图书封面
  uploadBookCover: async (file: File): Promise<UploadResponse> => {
    const formData = new FormData()
    formData.append('file', file)
    
    return request.post<UploadResponse>('/upload/book-cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
