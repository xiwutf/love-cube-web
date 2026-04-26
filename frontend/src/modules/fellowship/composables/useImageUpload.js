import { ref } from 'vue'
import { uploadImage } from '@f/api/upload.js'

export function useImageUpload() {
  const uploading = ref(false)

  function pickAndUpload(opts = {}) {
    return new Promise((resolve, reject) => {
      const input = document.createElement('input')
      input.type   = 'file'
      input.accept = 'image/*'
      input.onchange = async () => {
        const file = input.files?.[0]
        if (!file) { reject(new Error('未选择文件')); return }
        uploading.value = true
        try {
          const compressed = await compressImage(file, opts.quality ?? 0.8)
          const res = await uploadImage(compressed)
          resolve(typeof res === 'string' ? res : (res?.url ?? res?.data ?? ''))
        } catch (e) {
          reject(e)
        } finally {
          uploading.value = false
        }
      }
      input.click()
    })
  }

  function pickMultipleAndUpload(max) {
    return new Promise((resolve, reject) => {
      const input = document.createElement('input')
      input.type     = 'file'
      input.accept   = 'image/*'
      input.multiple = true
      input.onchange = async () => {
        const selected = Array.from(input.files || [])
        const files = Number.isFinite(max) && max > 0 ? selected.slice(0, max) : selected
        if (!files.length) { reject(new Error('未选择文件')); return }
        uploading.value = true
        try {
          const urls = await Promise.all(files.map(async f => {
            const compressed = await compressImage(f, 0.8)
            const res = await uploadImage(compressed)
            return typeof res === 'string' ? res : (res?.url ?? res?.data ?? '')
          }))
          resolve(urls)
        } catch (e) {
          reject(e)
        } finally {
          uploading.value = false
        }
      }
      input.click()
    })
  }

  return { pickAndUpload, pickMultipleAndUpload, uploading }
}

function compressImage(file, quality = 0.8) {
  return new Promise((resolve) => {
    if (file.size < 500 * 1024) { resolve(file); return }
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const MAX = 1080
        let { width, height } = img
        if (width > MAX || height > MAX) {
          if (width > height) { height = Math.round(height * MAX / width); width = MAX }
          else                { width  = Math.round(width  * MAX / height); height = MAX }
        }
        canvas.width  = width
        canvas.height = height
        canvas.getContext('2d').drawImage(img, 0, 0, width, height)
        canvas.toBlob(blob => resolve(new File([blob], file.name, { type: 'image/jpeg' })), 'image/jpeg', quality)
      }
      img.src = e.target.result
    }
    reader.readAsDataURL(file)
  })
}
