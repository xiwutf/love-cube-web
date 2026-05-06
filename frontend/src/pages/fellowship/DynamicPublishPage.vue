<template>
  <div class="publish-page">
    <div class="top-bar">
      <button class="back-btn" type="button" @click="router.back()">返回</button>
      <span class="title">发布动态</span>
      <button class="submit-btn" type="button" :disabled="submitting || !content.trim()" @click="submitDynamic">
        {{ submitting ? '发布中' : '发布' }}
      </button>
    </div>

    <div class="editor-card">
      <van-field
        v-model="content"
        type="textarea"
        rows="7"
        maxlength="500"
        show-word-limit
        placeholder="分享此刻的心情和故事..."
      />
      <van-uploader
        v-model="fileList"
        :after-read="onAfterRead"
        :before-delete="onBeforeDelete"
        multiple
        :max-count="9"
      />
    </div>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { publishDynamic } from '@/api/dynamic.js'
import { uploadImage } from '@/api/upload.js'

const router = useRouter()
const content = ref('')
const fileList = ref([])
const imageUrls = ref([])
const submitting = ref(false)

async function onAfterRead(fileItem) {
  const items = Array.isArray(fileItem) ? fileItem : [fileItem]
  for (const item of items) {
    item.status = 'uploading'
    item.message = '上传中...'
    try {
      const res = await uploadImage(item.file)
      const url = typeof res === 'string' ? res : (res?.url || res?.data || '')
      if (!url) throw new Error('上传结果无效')
      imageUrls.value.push(url)
      item.status = 'done'
      item.message = ''
      item.url = url
    } catch (error) {
      item.status = 'failed'
      item.message = '上传失败'
      showToast({ type: 'fail', message: error?.message || '图片上传失败' })
    }
  }
}

function onBeforeDelete(file, detail) {
  const url = file?.url || ''
  if (url) {
    imageUrls.value = imageUrls.value.filter((img) => img !== url)
  } else if (typeof detail?.index === 'number') {
    imageUrls.value.splice(detail.index, 1)
  }
  return true
}

async function submitDynamic() {
  const text = content.value.trim()
  if (!text) {
    showToast({ type: 'fail', message: '请输入动态内容' })
    return
  }
  if (fileList.value.some((item) => item.status === 'uploading')) {
    showToast({ type: 'fail', message: '图片上传中，请稍后' })
    return
  }
  submitting.value = true
  try {
    await publishDynamic({ content: text, imageUrls: imageUrls.value })
    showToast({ type: 'success', message: '发布成功' })
    router.replace('/fellowship/dynamic')
  } catch (error) {
    showToast({ type: 'fail', message: error?.message || '发布失败' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 72px;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 64px 1fr 64px;
  align-items: center;
  height: 48px;
  padding: 0 10px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.back-btn,
.submit-btn {
  border: none;
  background: transparent;
  font-size: 13px;
  color: #ff5f84;
}

.submit-btn:disabled {
  color: #bfbfbf;
}

.title {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.editor-card {
  margin: 12px;
  padding: 10px;
  border-radius: 14px;
  background: #fff;
}
</style>
