import request from './request.js'

export function uploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload/image', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}

export function uploadVerifyPhoto(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload/verify', form, { headers: { 'Content-Type': 'multipart/form-data' } })
}
