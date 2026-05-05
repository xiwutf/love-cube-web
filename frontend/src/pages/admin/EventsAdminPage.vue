<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">活动管理</h1>
      <p class="platform-subtitle">先看活动简介，点击详情进行完整编辑与发布</p>

      <div class="event-create admin-desktop-only">
        <div class="event-create-grid">
          <label class="field-block">
            <span class="field-label">活动名称</span>
            <input v-model="draft.title" class="admin-input" placeholder="请输入活动名称" />
          </label>
          <label class="field-block">
            <span class="field-label">活动时间</span>
            <input v-model="draft.time" class="admin-input" placeholder="例如：2026-05-08 19:00" />
          </label>
          <label class="field-block">
            <span class="field-label">地点</span>
            <input v-model="draft.location" class="admin-input" placeholder="请输入活动地点" />
          </label>
          <label class="field-block">
            <span class="field-label">分类（选填）</span>
            <input v-model="draft.category" class="admin-input" placeholder="例如：线上 / 线下 / 交友" />
          </label>
          <label class="field-block field-cover">
            <span class="field-label">封面图</span>
            <CoverUploadField v-model="draft.coverUrl" :disabled="saving" />
          </label>
        </div>

        <div class="event-create-actions">
          <label class="admin-check"><input type="checkbox" v-model="draft.pinned" /> 置顶</label>
          <label class="admin-check"><input type="checkbox" v-model="draft.recommended" /> 推荐</label>
          <button type="button" class="admin-btn primary" :disabled="saving" @click="create">新增活动</button>
        </div>

        <label class="field-block">
          <span class="field-label">活动详情</span>
          <textarea v-model="draft.content" class="admin-textarea" placeholder="请输入活动详情"></textarea>
        </label>
      </div>

      <p class="admin-mobile-note admin-mobile-only">手机端仅支持查看活动状态和报名人数，编辑发布请在 PC 端完成</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="event-list admin-desktop-only">
      <article v-for="item in paginatedItems" :key="item.id" class="event-item">
        <AdminBriefCardHeader
          :title="item.title"
          :summary="item.summary"
          fallback-summary="暂无摘要"
          :status="item.status"
          :updated-at-text="formatDate(item.updatedAt)"
        />
        <div class="event-brief-meta">
          <span>时间：{{ item.eventTime || item.time || '-' }}</span>
          <span>地点：{{ item.location || '-' }}</span>
          <span>分类：{{ item.category || '-' }}</span>
          <span>报名：{{ item.signupCount || 0 }}</span>
          <span v-if="item.pinned">置顶</span>
          <span v-if="item.recommended">推荐</span>
        </div>
        <div class="admin-cell-actions card-action-row">
          <button class="admin-btn" type="button" :disabled="saving" @click="openDetail(item)">查看详情</button>
        </div>
      </article>
      <section class="platform-card" v-if="!items.length">
        <van-empty description="暂无活动" />
      </section>
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in paginatedItems" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.eventTime || item.time || '-' }} / {{ item.location || '-' }}</p>
        <p class="admin-row-meta">分类：{{ item.category || '-' }} · 报名 {{ item.signupCount || 0 }} 人</p>
        <p class="admin-row-meta">更新：{{ formatDate(item.updatedAt) }}</p>
        <div class="admin-cell-actions card-action-row">
          <button class="admin-btn" type="button" :disabled="saving" @click="openDetail(item)">查看详情</button>
        </div>
      </article>
      <van-empty v-if="!items.length" description="暂无活动" />
    </div>

    <section v-if="items.length" class="platform-card pagination-card">
      <AdminPaginationBar
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total-pages="totalPages"
      />
    </section>

    <AdminDetailDialogShell
      :visible="detailDialog.visible"
      :title="detailDialog.title || '活动详情'"
      :loading="saving"
      @update:visible="onDetailDialogVisible"
    >
        <div class="admin-detail-grid">
          <label class="admin-detail-field">
            <span class="admin-detail-label">活动名称</span>
            <input v-model="detailDialog.form.title" class="admin-input" placeholder="活动名称" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">活动时间</span>
            <input v-model="detailDialog.form.time" class="admin-input" placeholder="活动时间" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">地点</span>
            <input v-model="detailDialog.form.location" class="admin-input" placeholder="地点" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">分类</span>
            <input v-model="detailDialog.form.category" class="admin-input" placeholder="分类" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">封面图</span>
            <CoverUploadField v-model="detailDialog.form.coverUrl" :disabled="saving" />
          </label>
        </div>
        <div class="admin-detail-flags">
          <label class="admin-check"><input type="checkbox" v-model="detailDialog.form.pinned" /> 置顶</label>
          <label class="admin-check"><input type="checkbox" v-model="detailDialog.form.recommended" /> 推荐</label>
          <span class="admin-row-meta">报名人数：{{ detailDialog.form.signupCount || 0 }}</span>
        </div>
        <ContentEditorDialog
          :visible="detailDialog.editorVisible"
          :title="`编辑正文：${detailDialog.title || ''}`"
          :content="detailDialog.form.content"
          :loading="saving"
          placeholder="请输入活动详情"
          @update:visible="onDetailEditorVisible"
          @apply="applyDetailContent"
          @save="saveDetailFromEditor"
        />
        <div class="admin-detail-field">
          <ContentCopyEditorField
            :summary="detailDialog.form.summary"
            summary-placeholder="用于活动卡片简述"
            :preview="previewText(detailDialog.form.content)"
            :disabled="saving"
            @update:summary="detailDialog.form.summary = $event"
            @edit="detailDialog.editorVisible = true"
          />
        </div>
        <div class="admin-cell-actions">
          <button class="admin-btn" type="button" :disabled="saving" @click="saveDetail">保存</button>
          <button class="admin-btn" type="button" :disabled="saving" @click="toggleDetail">
            {{ detailDialog.form.status === 'published' ? '下架' : '发布' }}
          </button>
          <button class="admin-btn danger" type="button" :disabled="saving" @click="removeDetail">删除</button>
        </div>
    </AdminDetailDialogShell>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showToast } from 'vant'
import CoverUploadField from '@/components/admin/CoverUploadField.vue'
import ContentEditorDialog from '@/components/admin/ContentEditorDialog.vue'
import ContentCopyEditorField from '@/components/admin/ContentCopyEditorField.vue'
import AdminPaginationBar from '@/components/admin/AdminPaginationBar.vue'
import AdminDetailDialogShell from '@/components/admin/AdminDetailDialogShell.vue'
import AdminBriefCardHeader from '@/components/admin/AdminBriefCardHeader.vue'
import { getEvents, saveEvent, deleteEvent } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
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
const detailDialog = reactive({
  visible: false,
  editorVisible: false,
  itemId: null,
  title: '',
  form: {
    title: '',
    summary: '',
    content: '',
    time: '',
    location: '',
    signupCount: 0,
    category: '',
    coverUrl: '',
    pinned: false,
    recommended: false,
    status: 'draft'
  }
})

const totalPages = computed(() => Math.max(1, Math.ceil(items.value.length / pageSize.value)))
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return items.value.slice(start, start + pageSize.value)
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

async function save(item, options = {}) {
  saving.value = true
  try {
    const updated = await saveEvent({ ...item })
    Object.assign(item, updated)
    const message = options.fromEditor ? `活动已保存（字数：${options.contentLength || 0}）` : '活动已保存'
    showToast({ message, type: 'success' })
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

function previewText(value) {
  const text = String(value || '').trim()
  if (!text) return '暂无正文内容'
  return text.length > 80 ? `${text.slice(0, 80)}...` : text
}

function openDetail(item) {
  detailDialog.visible = true
  detailDialog.editorVisible = false
  detailDialog.itemId = item.id
  detailDialog.title = item.title || '活动详情'
  detailDialog.form = {
    title: item.title || '',
    summary: item.summary || '',
    content: item.content || '',
    time: item.eventTime || item.time || '',
    location: item.location || '',
    signupCount: Number(item.signupCount || 0),
    category: item.category || '',
    coverUrl: item.coverUrl || '',
    pinned: Boolean(item.pinned),
    recommended: Boolean(item.recommended),
    status: item.status || 'draft'
  }
}

function closeDetail() {
  detailDialog.visible = false
  detailDialog.editorVisible = false
  detailDialog.itemId = null
}

function onDetailDialogVisible(visible) {
  if (!visible) {
    closeDetail()
    return
  }
  detailDialog.visible = true
}

function findCurrentItem() {
  return items.value.find((entry) => entry.id === detailDialog.itemId) || null
}

async function saveDetail(options = {}) {
  const item = findCurrentItem()
  if (!item) return
  Object.assign(item, detailDialog.form)
  await save(item, options)
  detailDialog.title = item.title || detailDialog.title
}

async function toggleDetail() {
  const item = findCurrentItem()
  if (!item) return
  saving.value = true
  try {
    const newStatus = detailDialog.form.status === 'published' ? 'draft' : 'published'
    const payload = { ...item, ...detailDialog.form, status: newStatus }
    const updated = await saveEvent(payload)
    Object.assign(item, updated)
    Object.assign(detailDialog.form, updated)
    detailDialog.title = updated.title || detailDialog.title
    showToast({ message: '状态已更新', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '更新失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function removeDetail() {
  const item = findCurrentItem()
  if (!item) return
  await remove(item)
  closeDetail()
}

function onDetailEditorVisible(visible) {
  detailDialog.editorVisible = visible
}

function applyDetailContent(content) {
  detailDialog.form.content = content
  detailDialog.editorVisible = false
}

async function saveDetailFromEditor(content) {
  detailDialog.form.content = content
  await saveDetail({ fromEditor: true, contentLength: String(content || '').length })
  if (!saving.value) detailDialog.editorVisible = false
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

watch(pageSize, () => {
  currentPage.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages
})

onMounted(load)
</script>

<style scoped>
.admin-check {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--lc-muted);
  cursor: pointer;
  white-space: nowrap;
  margin-bottom: 4px;
}

.admin-check input[type='checkbox'] {
  accent-color: var(--lc-rose);
}

.event-create {
  margin-top: 14px;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: #fff;
  padding: 16px;
  display: grid;
  gap: 14px;
}

.event-create-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.field-block {
  display: grid;
  gap: 6px;
}

.field-cover {
  grid-column: span 2;
}

.field-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-muted);
}

.event-create-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.event-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.event-item {
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: #fff;
  padding: 14px;
  box-shadow: var(--lc-shadow-sm);
  display: grid;
  gap: 12px;
}

.event-brief-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--lc-muted);
  font-size: 13px;
}

.card-action-row {
  display: flex;
  justify-content: flex-end;
}

.pagination-card {
  margin-top: 12px;
}

@media (max-width: 1360px) {
  .event-create-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1120px) {
  .event-create-grid {
    grid-template-columns: 1fr;
  }

  .field-cover {
    grid-column: span 1;
  }
}
</style>



