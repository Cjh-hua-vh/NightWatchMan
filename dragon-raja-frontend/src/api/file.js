import request from './request'

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/file/upload/avatar', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}

export function uploadPostImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/file/upload/post-image', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
}
