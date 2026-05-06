<template>
  <main class="page">
    <template v-if="mode === 'menu'">
      <h1>发布中心</h1>
      <p class="fixed-type">选择你要进行的发布动作</p>
      <div class="action-grid">
        <button type="button" class="action-card" @click="openMood">发动态</button>
        <button type="button" class="action-card" @click="gotoHelpCreate">发求助</button>
        <button type="button" class="action-card" @click="gotoGroupCreate">发团体内容</button>
        <button type="button" class="action-card" @click="openClue">提交资源线索</button>
      </div>
    </template>

    <template v-else-if="mode === 'mood'">
      <h1>发布内容</h1>
      <p class="fixed-type">仅支持发布：每日心声</p>
      <PublishEditor :title="form.title" :content="form.content" @update="onEditor" />
      <PublishImageUpload @change="onImages" />
      <PublishTagSelector v-model="form.tags" :tags="MOOD_CATEGORIES" />
      <button type="button" class="submit" @click="submitMood">立即发布</button>
      <button type="button" class="back" @click="backToPublishMenu">返回发布中心</button>
    </template>

    <template v-else>
      <h1>提交资源线索</h1>
      <p class="fixed-type">提交后将进入待处理队列，由运营审核发布</p>
      <form class="clue-form" @submit.prevent="submitClue">
        <label>
          资源标题
          <input v-model.trim="clueForm.title" type="text" maxlength="100" placeholder="请输入标题" />
        </label>
        <label>
          资源分类
          <select v-model="clueForm.type">
            <option value="people">找人</option>
            <option value="activity">找活动</option>
            <option value="group">找圈子</option>
            <option value="resource">实用资源</option>
          </select>
        </label>
        <label>
          联系方式
          <input v-model.trim="clueForm.contact" type="text" maxlength="120" placeholder="手机号/微信/链接" />
        </label>
        <label>
          地区（选填）
          <input v-model.trim="clueForm.location" type="text" maxlength="100" placeholder="例如：河北省 保定市" />
        </label>
        <label>
          简述（选填）
          <textarea v-model.trim="clueForm.summary" rows="3" maxlength="500" placeholder="简要说明资源信息"></textarea>
        </label>
        <button type="submit" class="submit">提交线索</button>
      </form>
      <button type="button" class="back" @click="backToPublishMenu">返回发布中心</button>
    </template>

    <p v-if="message" class="msg">{{ message }}</p>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PublishEditor from '@/components/platform/publish/PublishEditor.vue'
import PublishImageUpload from '@/components/platform/publish/PublishImageUpload.vue'
import PublishTagSelector from '@/components/platform/publish/PublishTagSelector.vue'
import { createPositiveShare } from '@/api/positiveShare.js'
import { submitLocalResourceClue } from '@/api/localResources.js'

const route = useRoute()
const router = useRouter()
const message = ref('')
const mode = ref('menu')
const MOOD_CATEGORIES = ['感恩', '鼓励', '成长', '理想', '生活', '学习', '工作', '人际']
const form = reactive({ type: 'mood', title: '', content: '', tags: [], images: [] })
const clueForm = reactive({ title: '', type: 'people', contact: '', location: '', summary: '' })

function applyPublishModeFromQuery() {
  if (route.query.mode === 'clue') {
    message.value = ''
    mode.value = 'clue'
  }
}

onMounted(applyPublishModeFromQuery)
watch(() => route.query.mode, applyPublishModeFromQuery)

function openMood() {
  message.value = ''
  mode.value = 'mood'
  router.replace({ path: '/platform/publish', query: {} })
}

function openClue() {
  message.value = ''
  mode.value = 'clue'
  router.replace({ path: '/platform/publish', query: { mode: 'clue' } })
}

function backToPublishMenu() {
  mode.value = 'menu'
  if (route.query.mode) {
    router.replace({ path: '/platform/publish', query: {} })
  }
}

function gotoHelpCreate() {
  router.push('/platform/help/create')
}

function gotoGroupCreate() {
  router.push('/platform/groups/create')
}

function onEditor(payload) {
  form.title = payload.title
  form.content = payload.content
}

function onImages(files) {
  form.images = files
}

async function submitMood() {
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

async function submitClue() {
  if (!clueForm.title) {
    message.value = '资源标题不能为空'
    return
  }
  if (!clueForm.contact) {
    message.value = '联系方式不能为空'
    return
  }
  try {
    await submitLocalResourceClue({
      title: clueForm.title,
      type: clueForm.type,
      contact: clueForm.contact,
      location: clueForm.location,
      summary: clueForm.summary
    })
    message.value = '线索提交成功，已进入待处理队列'
    backToPublishMenu()
    clueForm.title = ''
    clueForm.type = 'people'
    clueForm.contact = ''
    clueForm.location = ''
    clueForm.summary = ''
  } catch (error) {
    message.value = error?.response?.data?.message || '线索提交失败，请稍后重试'
  }
}
</script>

<style scoped>
.page{width:min(100% - 24px,560px);margin:16px auto;display:grid;gap:12px;padding-bottom:calc(72px + env(safe-area-inset-bottom));}
h1{margin:0;font-size:20px}
.fixed-type{margin:0;color:var(--lc-muted);font-size:14px}
.submit{height:44px;border:0;border-radius:10px;background:var(--lc-blue);color:var(--lc-surface);font-weight:700}
.msg{margin:0;color:var(--lc-muted);font-size:13px}
.action-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:10px}
.action-card{height:88px;border:1px solid var(--lc-border);border-radius:12px;background:var(--lc-surface);font-weight:700;color:var(--lc-text)}
.clue-form{display:grid;gap:10px}
.clue-form label{display:grid;gap:6px;font-size:13px;color:var(--lc-muted)}
.clue-form input,.clue-form select,.clue-form textarea{width:100%;border:1px solid var(--lc-border);border-radius:10px;padding:10px;font:inherit;background:var(--lc-surface)}
.back{height:40px;border:1px solid var(--lc-border);border-radius:10px;background:var(--lc-surface);color:var(--lc-text);font-weight:600}
</style>
