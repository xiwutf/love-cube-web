<template>
  <div class="publish-page">
    <div class="top-bar">
      <button class="back-btn" type="button" @click="router.back()">返回</button>
      <span class="title">发布动态</span>
      <button class="submit-btn" type="button" :disabled="submitting || !canSubmit" @click="submitDynamic">
        {{ submitting ? '发布中' : '发布' }}
      </button>
    </div>

    <div class="hero-strip" aria-hidden="true" />

    <div class="editor-stack">
      <section class="editor-card compose-card">
        <p class="compose-lead">今天想和大家分享什么？</p>
        <van-field
          v-model="content"
          class="compose-field"
          type="textarea"
          rows="8"
          maxlength="500"
          show-word-limit
          placeholder="可以写心情、见证、祷告代求或生活片段…真诚分享更容易遇见同路人。"
          :border="false"
        />

        <div class="tool-block">
          <span class="tool-label">表情</span>
          <div class="emoji-scroll" role="list">
            <button
              v-for="em in quickEmojis"
              :key="em"
              type="button"
              class="emoji-chip"
              @click="appendText(em)"
            >
              {{ em }}
            </button>
          </div>
        </div>

        <div class="tool-block">
          <span class="tool-label">常用话题</span>
          <div class="tag-row">
            <button
              v-for="tag in topicTags"
              :key="tag"
              type="button"
              class="topic-chip"
              @click="appendTopic(tag)"
            >
              {{ tag }}
            </button>
          </div>
        </div>
      </section>

      <section class="editor-card media-card">
        <div class="section-head">
          <span class="section-title">添加图片</span>
          <span class="section-hint">可选，最多 9 张</span>
        </div>
        <p class="section-desc">配图能让动态更生动；纯文字也同样欢迎。</p>
        <van-uploader
          v-model="fileList"
          class="publish-uploader"
          :after-read="onAfterRead"
          :before-delete="onBeforeDelete"
          multiple
          :max-count="9"
        />
      </section>

      <section class="tips-card">
        <div class="tips-title">
          <span class="tips-icon" aria-hidden="true">✦</span>
          发布小贴士
        </div>
        <ul class="tips-list">
          <li>尊重他人，避免人身攻击与不当言论。</li>
          <li>勿发布广告、外链引流或隐私信息（电话、住址等）。</li>
          <li>内容会展示在动态广场，请使用友善、真实的表达。</li>
        </ul>
      </section>
    </div>

    <AppTabBar />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
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

const quickEmojis = ['🙏', '✨', '💛', '🌿', '🎵', '☕', '📖', '🌅', '💭', '🤝', '🎉', '☀️']

const topicTags = ['#今日感恩', '#读经心得', '#代祷', '#联谊日常', '#周末计划', '#新朋友']

const canSubmit = computed(() => {
  const text = content.value.trim()
  if (text.length > 0) return true
  return false
})

function appendText(fragment) {
  const cur = content.value
  const needsSpace = cur.length > 0 && !/\s$/.test(cur)
  content.value = cur + (needsSpace ? ' ' : '') + fragment
}

function appendTopic(tag) {
  const cur = content.value
  const piece = cur.includes(tag) ? '' : `${tag} `
  if (!piece) {
    showToast({ message: '已包含该话题' })
    return
  }
  const needsLead = cur.length > 0 && !/\s$/.test(cur)
  content.value = cur + (needsLead ? ' ' : '') + piece
}

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
  background: var(--lc-bg);
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
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.back-btn,
.submit-btn {
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--lc-pink);
  font-weight: 600;
}

.submit-btn:disabled {
  color: var(--lc-subtle);
  font-weight: 500;
}

.title {
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: var(--lc-text-deep);
}

.hero-strip {
  height: 4px;
  margin: 0 12px;
  border-radius: 4px;
  background: linear-gradient(90deg, var(--lc-pink-light), var(--lc-indigo-light), var(--lc-pink-border));
  opacity: 0.95;
}

.editor-stack {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-3);
  margin: var(--lc-space-3) 12px 0;
}

.editor-card {
  padding: 14px 14px 16px;
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
  border: 1px solid var(--lc-border);
}

.compose-lead {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-slate);
}

.compose-field :deep(.van-field__control) {
  min-height: 168px;
  font-size: 15px;
  line-height: 1.55;
  color: var(--lc-text);
}

.compose-field :deep(.van-field__word-limit) {
  color: var(--lc-muted-light);
  font-size: 12px;
}

.tool-block {
  margin-top: 14px;
}

.tool-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: var(--lc-muted-light);
  letter-spacing: 0.02em;
  margin-bottom: 8px;
}

.emoji-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
}

.emoji-scroll::-webkit-scrollbar {
  display: none;
}

.emoji-chip {
  flex-shrink: 0;
  min-width: 40px;
  height: 40px;
  border-radius: var(--lc-radius-sm);
  border: 1px solid var(--lc-border);
  background: var(--lc-soft);
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.emoji-chip:active {
  transform: scale(0.96);
  background: var(--lc-pink-light);
  border-color: var(--lc-pink-border);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-chip {
  border: none;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 13px;
  color: var(--lc-indigo);
  background: var(--lc-indigo-light);
  font-weight: 500;
  cursor: pointer;
  transition: filter 0.15s ease, transform 0.12s ease;
}

.topic-chip:active {
  transform: scale(0.98);
  filter: brightness(0.97);
}

.section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text-deep);
}

.section-hint {
  font-size: 12px;
  color: var(--lc-muted-light);
}

.section-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--lc-muted);
  line-height: 1.45;
}

.publish-uploader :deep(.van-uploader__upload) {
  border-radius: var(--lc-radius-sm);
  border: 1px dashed var(--lc-border);
  background: var(--lc-bg);
}

.tips-card {
  padding: 14px 16px 16px;
  border-radius: var(--lc-radius);
  background: linear-gradient(145deg, var(--lc-surface) 0%, var(--lc-pink-light) 120%);
  border: 1px solid var(--lc-pink-border);
  margin-bottom: 8px;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
  color: var(--lc-text-deep);
  margin-bottom: 10px;
}

.tips-icon {
  color: var(--lc-pink);
  font-size: 12px;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--lc-muted);
  line-height: 1.55;
}

.tips-list li + li {
  margin-top: 6px;
}
</style>
