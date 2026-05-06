<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">平台资讯管理</h1>
      <p class="platform-subtitle">先看简介列表，点击进入详情进行完整编辑</p>

      <div class="admin-toolbar admin-desktop-only">
        <input v-model="draft.title" class="admin-input" placeholder="资讯标题" />
        <input v-model="draft.tag" class="admin-input" placeholder="标签" />
        <input v-model="draft.category" class="admin-input" placeholder="分类（选填）" />
        <CoverUploadField v-model="draft.coverUrl" :disabled="saving" />
        <label class="admin-check"><input type="checkbox" v-model="draft.pinned" /> 置顶</label>
        <label class="admin-check"><input type="checkbox" v-model="draft.recommended" /> 推荐</label>
        <button type="button" class="admin-btn primary" :disabled="saving" @click="create">新增资讯</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea admin-desktop-only" placeholder="资讯内容"></textarea>

      <p class="admin-mobile-note admin-mobile-only">移动端仅支持查看，编辑请使用 PC 端。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="article-list admin-desktop-only">
      <article v-for="item in paginatedItems" :key="item.id" class="article-item">
        <AdminBriefCardHeader
          :title="item.title"
          :summary="item.summary"
          fallback-summary="暂无摘要"
          :status="item.status"
          :updated-at-text="formatDate(item.updatedAt)"
        />
        <div class="article-brief-meta">
          <span>标签：{{ item.tag || '-' }}</span>
          <span>分类：{{ item.category || '-' }}</span>
          <span v-if="item.pinned">置顶</span>
          <span v-if="item.recommended">推荐</span>
        </div>
        <div class="admin-cell-actions card-action-row">
          <button class="admin-btn" type="button" :disabled="saving" @click="openDetail(item)">查看详情</button>
        </div>
      </article>
      <section class="platform-card" v-if="!items.length">
        <van-empty description="暂无资讯" />
      </section>
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in paginatedItems" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.summary || '暂无摘要' }}</p>
        <p class="admin-row-meta">标签：{{ item.tag || '-' }} / 分类：{{ item.category || '-' }}</p>
        <p class="admin-row-meta">更新：{{ formatDate(item.updatedAt) }}</p>
        <div class="admin-cell-actions card-action-row">
          <button class="admin-btn" type="button" :disabled="saving" @click="openDetail(item)">查看详情</button>
        </div>
      </article>
      <van-empty v-if="!items.length" description="暂无资讯" />
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
      :title="detailDialog.title || '资讯详情'"
      :loading="saving"
      @update:visible="onDetailDialogVisible"
    >
        <div class="admin-detail-grid">
          <label class="admin-detail-field">
            <span class="admin-detail-label">标题</span>
            <input v-model="detailDialog.form.title" class="admin-input" placeholder="资讯标题" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">标签</span>
            <input v-model="detailDialog.form.tag" class="admin-input" placeholder="标签" />
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
        </div>
        <ContentEditorDialog
          :visible="detailDialog.editorVisible"
          :title="`编辑正文：${detailDialog.title || ''}`"
          :content="detailDialog.form.content"
          :loading="saving"
          placeholder="请输入完整正文"
          @update:visible="onDetailEditorVisible"
          @apply="applyDetailContent"
          @save="saveDetailFromEditor"
        />
        <div class="admin-detail-field">
          <ContentCopyEditorField
            :summary="detailDialog.form.summary"
            summary-placeholder="用于列表简述"
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
import { getArticles, saveArticle, deleteArticle } from '@/api/adminContent.js'

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
  tag: '平台资讯',
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
    tag: '',
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
    items.value = await getArticles()
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!draft.title) return
  saving.value = true
  try {
    const created = await saveArticle({ ...draft, status: 'draft' })
    items.value.unshift(created)
    Object.assign(draft, {
      title: '',
      summary: '',
      content: '',
      tag: '平台资讯',
      category: '',
      coverUrl: '',
      pinned: false,
      recommended: false
    })
    showToast({ message: '资讯已创建', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '创建失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function save(item, options = {}) {
  saving.value = true
  try {
    const updated = await saveArticle({ ...item })
    Object.assign(item, updated)
    const message = options.fromEditor ? `资讯已保存（字数：${options.contentLength || 0}）` : '资讯已保存'
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
    const updated = await saveArticle({ ...item, status: newStatus })
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
    await deleteArticle(item.id)
    items.value = items.value.filter((entry) => entry.id !== item.id)
    showToast({ message: '资讯已删', type: 'success' })
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
  detailDialog.title = item.title || '资讯详情'
  detailDialog.form = {
    title: item.title || '',
    summary: item.summary || '',
    content: item.content || '',
    tag: item.tag || '',
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
    const updated = await saveArticle(payload)
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
}

.admin-check input[type='checkbox'] {
  accent-color: var(--lc-rose);
}

.article-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.article-item {
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: #fff;
  padding: 14px;
  box-shadow: var(--lc-shadow-sm);
  display: grid;
  gap: 12px;
}

.article-brief-meta {
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

</style>



