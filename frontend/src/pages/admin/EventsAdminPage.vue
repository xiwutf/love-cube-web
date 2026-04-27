<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">活动管理</h1>
      <p class="platform-subtitle">PC 端支持完整编辑；手机端仅做活动查看与状态感知。</p>

      <div class="admin-toolbar admin-desktop-only">
        <input v-model="draft.title" class="admin-input" placeholder="活动名称" />
        <input v-model="draft.time" class="admin-input" placeholder="时间" />
        <input v-model="draft.location" class="admin-input" placeholder="地点" />
        <input v-model="draft.category" class="admin-input" placeholder="分类（选填）" />
        <CoverUploadField v-model="draft.coverUrl" :disabled="saving" />
        <label class="admin-check"><input type="checkbox" v-model="draft.pinned" /> 置顶</label>
        <label class="admin-check"><input type="checkbox" v-model="draft.recommended" /> 推荐</label>
        <button type="button" class="admin-btn primary" :disabled="saving" @click="create">新增活动</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea admin-desktop-only" placeholder="活动详情" />

      <p class="admin-mobile-note admin-mobile-only">手机端仅支持查看活动状态和报名人数，编辑发布请在 PC 端完成。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>活动</th>
            <th>时间地点</th>
            <th>分类 / 封面</th>
            <th>标记</th>
            <th>报名</th>
            <th>状态</th>
            <th>摘要</th>
            <th>内容编辑</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.title }}</td>
            <td>{{ item.eventTime || item.time }}<br />{{ item.location }}</td>
            <td>
              <input v-model="item.category" class="admin-input" placeholder="分类" style="margin-bottom: 6px" />
              <CoverUploadField v-model="item.coverUrl" :disabled="saving" />
            </td>
            <td>
              <label class="admin-check"><input type="checkbox" v-model="item.pinned" /> 置顶</label>
              <label class="admin-check"><input type="checkbox" v-model="item.recommended" /> 推荐</label>
            </td>
            <td>{{ item.signupCount || 0 }}</td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td><input v-model="item.summary" class="admin-input" /></td>
            <td><textarea v-model="item.content" class="admin-textarea" /></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn" type="button" :disabled="saving" @click="save(item)">保存</button>
                <button class="admin-btn" type="button" :disabled="saving" @click="toggle(item)">
                  {{ item.status === 'published' ? '下架' : '发布' }}
                </button>
                <button class="admin-btn danger" type="button" :disabled="saving" @click="remove(item)">删除</button>
                <span class="admin-row-meta">{{ formatDate(item.updatedAt) }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!items.length" description="暂无活动" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.eventTime || item.time }} · {{ item.location }}</p>
        <p class="admin-row-meta">
          <span v-if="item.category">{{ item.category }} · </span>
          <span v-if="item.pinned">置顶 · </span>
          <span v-if="item.recommended">推荐 · </span>
          报名 {{ item.signupCount || 0 }} 人
        </p>
        <p class="admin-row-meta">更新于 {{ formatDate(item.updatedAt) }}</p>
      </article>
      <van-empty v-if="!items.length" description="暂无活动" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import CoverUploadField from '@/components/admin/CoverUploadField.vue'
import { getEvents, saveEvent, deleteEvent } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])
const draft = reactive({
  title: '',
  summary: '',
  content: '',
  time: '',
  location: '',
  signupCount: 0,
  category: '',
  coverUrl: '',
  pinned: false,
  recommended: false
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await getEvents()
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!draft.title || !draft.time) return
  saving.value = true
  try {
    const created = await saveEvent({ ...draft, status: 'draft' })
    items.value.unshift(created)
    Object.assign(draft, {
      title: '',
      summary: '',
      content: '',
      time: '',
      location: '',
      signupCount: 0,
      category: '',
      coverUrl: '',
      pinned: false,
      recommended: false
    })
    showToast({ message: '活动已创建', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '创建失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function save(item) {
  saving.value = true
  try {
    const updated = await saveEvent({ ...item })
    Object.assign(item, updated)
    showToast({ message: '活动已保存', type: 'success' })
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
    const updated = await saveEvent({ ...item, status: newStatus })
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
    await deleteEvent(item.id)
    items.value = items.value.filter((entry) => entry.id !== item.id)
    showToast({ message: '活动已删除', type: 'success' })
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

<style scoped>
.admin-check {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  white-space: nowrap;
  margin-bottom: 4px;
}

.admin-check input[type='checkbox'] {
  accent-color: #e84f73;
}
</style>
