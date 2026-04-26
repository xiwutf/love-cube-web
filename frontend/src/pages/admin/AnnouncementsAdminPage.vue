<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">公告管理</h1>
      <p class="platform-subtitle">PC 端支持完整编辑与发布流程；手机端提供查看能力。</p>

      <div class="admin-toolbar admin-desktop-only">
        <input v-model="draft.title" class="admin-input" placeholder="公告标题" />
        <input v-model="draft.summary" class="admin-input" placeholder="摘要" />
        <button type="button" class="admin-btn primary" :disabled="saving" @click="create">新增公告</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea admin-desktop-only" placeholder="公告内容" />

      <p class="admin-mobile-note admin-mobile-only">手机端仅支持查看公告状态与更新时间，编辑发布请在 PC 端完成。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中…</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>摘要</th>
            <th>状态</th>
            <th>内容编辑</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ item.summary }}</td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td><textarea v-model="item.content" class="admin-textarea" /></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn" type="button" :disabled="saving" @click="save(item)">保存</button>
                <button class="admin-btn" type="button" :disabled="saving" @click="toggle(item)">{{ item.status === 'published' ? '下架' : '发布' }}</button>
                <button class="admin-btn danger" type="button" :disabled="saving" @click="remove(item)">删除</button>
                <span class="admin-row-meta">{{ formatDate(item.updatedAt) }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!items.length" description="暂无公告" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.summary }}</p>
        <p class="admin-row-meta">更新于 {{ formatDate(item.updatedAt) }}</p>
      </article>
      <van-empty v-if="!items.length" description="暂无公告" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import { getAnnouncements, saveAnnouncement, deleteAnnouncement } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])
const draft = reactive({ title: '', summary: '', content: '' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await getAnnouncements()
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!draft.title || !draft.summary) return
  saving.value = true
  try {
    const created = await saveAnnouncement({ ...draft, status: 'draft' })
    items.value.unshift(created)
    draft.title = ''
    draft.summary = ''
    draft.content = ''
    showToast({ message: '公告已创建', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '创建失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function save(item) {
  saving.value = true
  try {
    const updated = await saveAnnouncement({ ...item })
    Object.assign(item, updated)
    showToast({ message: '公告已保存', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '保存失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function toggle(item) {
  saving.value = true
  try {
    const newStatus = item.status === 'published' ? 'draft' : 'published'
    const updated = await saveAnnouncement({ ...item, status: newStatus })
    Object.assign(item, updated)
    showToast({ message: '状态已更新', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '更新失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function remove(item) {
  saving.value = true
  try {
    await deleteAnnouncement(item.id)
    items.value = items.value.filter(i => i.id !== item.id)
    showToast({ message: '公告已删除', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '删除失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>
