/**
 * 图片上传 composable
 * 替换小程序 wx.chooseMedia + wx.compressImage + wx.uploadFile
 *
 * 用法：
 *   const { pickAndUpload, uploading } = useImageUpload()
 *   const url = await pickAndUpload('/api/upload/image')
 */
import { ref } from 'vue'
import { uploadImage } from '@/api/upload.js'

export function useImageUpload() {
  const uploading = ref(false)

  /**
   * 弹出文件选择框，压缩后上传，返回服务器 URL
   * @param {object} opts
   * @param {number} opts.maxSizeMB  最大压缩后大小（MB），默认 1
   * @param {number} opts.quality    JPEG 质量 0~1，默认 0.8
   */
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
          // 后端返回 { url: '...' } 或直接字符串
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

  /**
   * 多图选择上传，返回 URL 数组
   * @param {number} max 最多几张
   */
  function pickMultipleAndUpload(max = 9) {
    return new Promise((resolve, reject) => {
      const input = document.createElement('input')
      input.type     = 'file'
      input.accept   = 'image/*'
      input.multiple = true
      input.onchange = async () => {
        const files = Array.from(input.files || []).slice(0, max)
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

/** Canvas 压缩图片 */
function compressImage(file, quality = 0.8) {
  return new Promise((resolve) => {
    // 小于 500KB 不压缩
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
