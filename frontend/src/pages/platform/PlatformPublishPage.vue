<template>
  <main class="page">
    <h1>发布内容</h1>
    <p class="fixed-type">仅支持发布：每日心声</p>
    <PublishEditor :title="form.title" :content="form.content" @update="onEditor" />
    <PublishImageUpload @change="onImages" />
    <PublishTagSelector v-model="form.tags" :tags="MOOD_CATEGORIES" />
    <button type="button" class="submit" @click="submit">立即发布</button>
    <p v-if="message" class="msg">{{ message }}</p>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PublishEditor from '@/components/platform/publish/PublishEditor.vue'
import PublishImageUpload from '@/components/platform/publish/PublishImageUpload.vue'
import PublishTagSelector from '@/components/platform/publish/PublishTagSelector.vue'
import { createPositiveShare } from '@/api/positiveShare.js'

const router = useRouter()
const message = ref('')
const MOOD_CATEGORIES = ['感恩', '鼓励', '成长', '理想', '生活', '学习', '工作', '人际']
const form = reactive({ type: 'mood', title: '', content: '', tags: [], images: [] })

function onEditor(payload) {
  form.title = payload.title
  form.content = payload.content
}

function onImages(files) {
  form.images = files
}

async function submit() {
  if (!form.content.trim()) {
    message.value = '正文不能为空'
    return
  }

  try {
    const category = form.tags[0] || '感恩'
    await createPositiveShare({
      content: form.content.trim(),
      category,
      publicVisible: true
    })
    message.value = '发布成功，正在跳转内容中心...'
    window.setTimeout(() => router.push('/platform/content?type=mood'), 500)
  } catch (error) {
    message.value = error?.response?.data?.message || '发布失败，请稍后重试'
  }
}
</script>

<style scoped>
.page{width:min(100% - 24px,560px);margin:16px auto;display:grid;gap:12px;padding-bottom:calc(72px + env(safe-area-inset-bottom));}
h1{margin:0;font-size:20px}
.fixed-type{margin:0;color:var(--lc-muted);font-size:14px}
.submit{height:44px;border:0;border-radius:10px;background:var(--lc-blue);color:var(--lc-surface);font-weight:700}
.msg{margin:0;color:var(--lc-muted);font-size:13px}
</style>
