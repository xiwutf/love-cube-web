<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">本地资源管理</h1>
      <p class="platform-subtitle">维护找人、活动、圈子和实用资源，支持发布与下架。</p>

      <div class="create-grid">
        <label class="field-block">
          <span class="field-label">标题</span>
          <input v-model.trim="draft.title" class="admin-input" placeholder="请输入标题" />
        </label>
        <label class="field-block">
          <span class="field-label">类型</span>
          <select v-model="draft.type" class="admin-input">
            <option value="people">找人</option>
            <option value="activity">找活动</option>
            <option value="group">找圈子</option>
            <option value="resource">实用资源</option>
          </select>
        </label>
        <label class="field-block">
          <span class="field-label">地点</span>
          <input v-model.trim="draft.location" class="admin-input" placeholder="地点（可选）" />
        </label>
        <label class="field-block">
          <span class="field-label">活动时间</span>
          <input v-model.trim="draft.eventTime" class="admin-input" placeholder="2026-05-06T19:00:00" />
        </label>
      </div>
      <label class="field-block">
        <span class="field-label">简介</span>
        <textarea v-model.trim="draft.summary" class="admin-textarea" placeholder="简介（可选）" />
      </label>
      <div class="admin-cell-actions">
        <button class="admin-btn primary" :disabled="saving" type="button" @click="create">新增资源</button>
      </div>
    </section>

    <section class="admin-list">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span>
        </div>
        <p class="admin-row-meta">
          <span class="inline-tag">{{ typeLabel(item.type) }}</span>
          <span>{{ item.location || '地点待补充' }}</span>
        </p>
        <p class="admin-row-meta">热度 {{ item.heat || 0 }} · 感兴趣 {{ item.interestCount || 0 }}</p>
        <p class="admin-row-meta">{{ item.summary || '暂无简介' }}</p>
        <div class="admin-cell-actions">
          <button class="admin-btn" :disabled="saving" type="button" @click="edit(item)">编辑</button>
          <button class="admin-btn" :disabled="saving" type="button" @click="toggleStatus(item)">
            {{ item.status === 'published' ? '下架' : '发布' }}
          </button>
          <button class="admin-btn danger" :disabled="saving" type="button" @click="remove(item)">删除</button>
        </div>
      </article>
      <van-empty v-if="!items.length && !loading" description="暂无本地资源" />
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import {
  createAdminLocalResource,
  deleteAdminLocalResource,
  getAdminLocalResources,
  offlineAdminLocalResource,
  publishAdminLocalResource,
  updateAdminLocalResource
} from '@/api/adminContent.js'

const loading = ref(false)
const saving = ref(false)
const items = ref([])
const draft = reactive({
  title: '',
  type: 'people',
  location: '',
  eventTime: '',
  summary: '',
  coverUrl: ''
})

function typeLabel(type) {
  const map = { people: '找人', activity: '找活动', group: '找圈子', resource: '实用资源' }
  return map[type] || '未分类'
}

function statusLabel(status) {
  const map = { draft: '草稿', published: '已发布', offline: '已下架' }
  return map[status] || (status || '未知')
}

async function load() {
  loading.value = true
  try {
    items.value = await getAdminLocalResources()
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!draft.title) {
    showToast({ type: 'fail', message: '请先填写标题' })
    return
  }
  saving.value = true
  try {
    const created = await createAdminLocalResource({ ...draft, status: 'draft' })
    items.value.unshift(created)
    draft.title = ''
    draft.location = ''
    draft.eventTime = ''
    draft.summary = ''
    draft.coverUrl = ''
    showToast({ type: 'success', message: '资源已新增' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '新增失败' })
  } finally {
    saving.value = false
  }
}

async function edit(item) {
  const nextTitle = window.prompt('编辑标题', item.title || '')
  if (nextTitle === null) return
  const nextSummary = window.prompt('编辑简介（可为空）', item.summary || '')
  if (nextSummary === null) return
  const nextLocation = window.prompt('编辑地点（可为空）', item.location || '')
  if (nextLocation === null) return
  saving.value = true
  try {
    const payload = {
      ...item,
      title: `${nextTitle}`.trim(),
      summary: `${nextSummary}`.trim(),
      location: `${nextLocation}`.trim()
    }
    const updated = await updateAdminLocalResource(item.id, payload)
    Object.assign(item, updated)
    showToast({ type: 'success', message: '保存成功' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '保存失败' })
  } finally {
    saving.value = false
  }
}

async function toggleStatus(item) {
  saving.value = true
  try {
    const updated = item.status === 'published'
      ? await offlineAdminLocalResource(item.id)
      : await publishAdminLocalResource(item.id)
    Object.assign(item, updated)
    showToast({ type: 'success', message: '状态已更新' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '状态更新失败' })
  } finally {
    saving.value = false
  }
}

async function remove(item) {
  saving.value = true
  try {
    await deleteAdminLocalResource(item.id)
    items.value = items.value.filter(entry => entry.id !== item.id)
    showToast({ type: 'success', message: '已删除' })
  } catch (error) {
    showToast({ type: 'fail', message: error.message || '删除失败' })
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.create-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin-top: 12px; }
.field-block { display: grid; gap: 6px; }
.field-label { font-size: 12px; font-weight: 700; color: var(--lc-muted); }
.admin-list { margin-top: 12px; display: grid; gap: 10px; }
.inline-tag { display: inline-flex; align-items: center; padding: 2px 8px; border-radius: 999px; font-size: 12px; font-weight: 700; background: var(--lc-indigo-soft); color: var(--lc-indigo); margin-right: 6px; }
@media (max-width: 900px) { .create-grid { grid-template-columns: 1fr; } }
</style>
