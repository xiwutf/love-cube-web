import request from './request.js'

export function getMyFellowshipProfile() {
  return request.get('/fellowship/profile/me')
}

export function updateMyFellowshipProfile(data) {
  return request.put('/fellowship/profile/me', data)
}

export function getFellowshipProfileCompletion() {
  return request.get('/fellowship/profile/completion')
}

function toFormData(file) {
  const form = new FormData()
  form.append('file', file)
  return form
}

export async function uploadFellowshipAvatar(file) {
  try {
    return await request.post('/fellowship/profile/avatar', toFormData(file), {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  } catch {
    return request.post('/upload/avatar', toFormData(file), {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export async function uploadFellowshipPhoto(file) {
  try {
    return await request.post('/fellowship/profile/photos', toFormData(file), {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  } catch {
    return request.post('/upload/photos', toFormData(file), {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export async function deleteFellowshipPhoto(photoIdOrUrl) {
  try {
    return await request.delete(`/fellowship/profile/photos/${photoIdOrUrl}`)
  } catch {
    return request.delete('/upload/photos', { params: { photoUrl: photoIdOrUrl } })
  }
}

export function getFellowshipPhotos() {
  return request.get('/upload/photos')
}

export function saveFellowshipPhotos(photos) {
  return request.post('/upload/photos/save', { photos })
}
