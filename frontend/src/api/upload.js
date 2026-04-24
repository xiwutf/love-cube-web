import request from './request.js'

/** 上传图片，file 为 File 对象 */
export function uploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/upload/image', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
