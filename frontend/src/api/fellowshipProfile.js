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
  const compressed = await compressForUpload(file)
  return request.post('/upload/avatar', toFormData(compressed), {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export async function uploadFellowshipPhoto(file) {
  const compressed = await compressForUpload(file)
  return request.post('/upload/photos', toFormData(compressed), {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
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

async function compressForUpload(file) {
  if (!file || !file.type?.startsWith('image/')) return file
  if (file.size <= 900 * 1024) return file

  const dataUrl = await readFileAsDataUrl(file)
  const image = await loadImage(dataUrl)
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  if (!context) return file

  const maxEdge = 1280
  let { width, height } = image
  if (width > maxEdge || height > maxEdge) {
    if (width >= height) {
      height = Math.round((height * maxEdge) / width)
      width = maxEdge
    } else {
      width = Math.round((width * maxEdge) / height)
      height = maxEdge
    }
  }
  canvas.width = width
  canvas.height = height
  context.drawImage(image, 0, 0, width, height)

  const qualitySteps = [0.82, 0.76, 0.7, 0.62]
  let bestBlob = null
  for (const quality of qualitySteps) {
    const blob = await canvasToBlob(canvas, quality)
    if (!blob) continue
    bestBlob = blob
    if (blob.size <= 900 * 1024) break
  }

  if (!bestBlob) return file
  return new File([bestBlob], replaceExt(file.name, '.jpg'), { type: 'image/jpeg' })
}

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

function loadImage(src) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('图片解析失败'))
    img.src = src
  })
}

function canvasToBlob(canvas, quality) {
  return new Promise((resolve) => {
    canvas.toBlob((blob) => resolve(blob), 'image/jpeg', quality)
  })
}

function replaceExt(fileName, ext) {
  const name = String(fileName || 'upload')
  const dot = name.lastIndexOf('.')
  if (dot <= 0) return `${name}${ext}`
  return `${name.slice(0, dot)}${ext}`
}
