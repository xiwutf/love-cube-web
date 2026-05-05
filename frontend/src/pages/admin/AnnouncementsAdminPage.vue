<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">公告管理</h1>
      <p class="platform-subtitle">PC 端支持完整编辑与发布流程；手机端提供查看能力</p>

      <div class="announcement-create admin-desktop-only">
        <div class="announcement-create-grid">
          <label class="field-block">
            <span class="field-label">公告标题</span>
            <input v-model="draft.title" class="admin-input" placeholder="请输入公告标题" />
          </label>
          <label class="field-block">
            <span class="field-label">摘要</span>
            <input v-model="draft.summary" class="admin-input" placeholder="用于卡片简述，建议 20-40 字" />
          </label>
          <label class="field-block">
            <span class="field-label">分类</span>
            <input v-model="draft.category" class="admin-input" placeholder="例如：通知 / 活动 / 公告" />
          </label>
          <label class="field-block field-block-wide">
            <span class="field-label">封面图</span>
            <CoverUploadField v-model="draft.coverUrl" :disabled="saving" />
          </label>
          <label class="field-block field-block-wide">
            <span class="field-label">附件图（如微信群二维码）</span>
            <CoverUploadField v-model="draft.attachmentUrl" :disabled="saving" />
          </label>
        </div>

        <div class="announcement-create-flags">
          <label class="admin-check"><input type="checkbox" v-model="draft.pinned" /> 置顶</label>
          <label class="admin-check"><input type="checkbox" v-model="draft.recommended" /> 推荐</label>
          <label class="admin-check"><input type="checkbox" v-model="draft.popupEnabled" /> 弹窗</label>
          <button type="button" class="admin-btn primary" :disabled="saving" @click="create">新增公告</button>
        </div>

        <label class="field-block">
          <span class="field-label">公告内容</span>
          <textarea v-model="draft.content" class="admin-textarea" placeholder="请输入完整公告内容"></textarea>
        </label>
      </div>

      <p class="admin-mobile-note admin-mobile-only">移动端仅支持查看，编辑请使用 PC 端。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="announcement-list admin-desktop-only">
      <article v-for="item in paginatedItems" :key="item.id" class="announcement-item">
        <AdminBriefCardHeader
          :title="item.title"
          :summary="item.summary"
          :status="item.status"
          :updated-at-text="formatDate(item.updatedAt)"
        />

        <div class="announcement-brief-meta">
          <span>分类：{{ item.category || '未分类' }}</span>
          <span v-if="item.pinned">置顶</span>
          <span v-if="item.recommended">推荐</span>
          <span v-if="item.popupEnabled">弹窗</span>
        </div>

        <div class="admin-cell-actions card-action-row">
          <button class="admin-btn" type="button" :disabled="saving" @click="openDetail(item)">查看详情</button>
        </div>
      </article>
      <section class="platform-card" v-if="!items.length">
        <van-empty description="暂无公告" />
      </section>
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in paginatedItems" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.summary }}</p>
        <p class="admin-row-meta">
          <span v-if="item.category">分类：{{ item.category }} · </span>
          <span v-if="item.pinned">置顶 · </span>
          <span v-if="item.recommended">推荐</span>
        </p>
        <p class="admin-row-meta">更新：{{ formatDate(item.updatedAt) }}</p>
        <div class="admin-cell-actions card-action-row">
          <button class="admin-btn" type="button" :disabled="saving" @click="openDetail(item)">查看详情</button>
        </div>
      </article>
      <van-empty v-if="!items.length" description="暂无公告" />
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
      :title="detailDialog.title || '公告详情'"
      :loading="saving"
      @update:visible="onDetailDialogVisible"
    >
        <div class="admin-detail-grid">
          <label class="admin-detail-field">
            <span class="admin-detail-label">标题</span>
            <input v-model="detailDialog.form.title" class="admin-input" placeholder="公告标题" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">摘要</span>
            <input v-model="detailDialog.form.summary" class="admin-input" placeholder="用于卡片简述" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">分类</span>
            <input v-model="detailDialog.form.category" class="admin-input" placeholder="分类" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">封面图</span>
            <CoverUploadField v-model="detailDialog.form.coverUrl" :disabled="saving" />
          </label>
          <label class="admin-detail-field">
            <span class="admin-detail-label">附件图</span>
            <CoverUploadField v-model="detailDialog.form.attachmentUrl" :disabled="saving" />
          </label>
        </div>
        <div class="admin-detail-flags">
          <label class="admin-check"><input type="checkbox" v-model="detailDialog.form.pinned" /> 置顶</label>
          <label class="admin-check"><input type="checkbox" v-model="detailDialog.form.recommended" /> 推荐</label>
          <label class="admin-check"><input type="checkbox" v-model="detailDialog.form.popupEnabled" /> 弹窗</label>
        </div>
        <ContentEditorDialog
          :visible="detailDialog.editorVisible"
          :title="`编辑正文：${detailDialog.title || ''}`"
          :content="detailDialog.form.content"
          :loading="saving"
          placeholder="请输入完整公告内容"
          @update:visible="onDetailEditorVisible"
          @apply="applyDetailContent"
          @save="saveDetailFromEditor"
        />
        <div class="admin-detail-field">
          <ContentCopyEditorField
            :summary="detailDialog.form.summary"
            summary-placeholder="用于卡片简述，建议 20-40 字"
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
import { getAnnouncements, saveAnnouncement, deleteAnnouncement } from '@/api/adminContent.js'

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
  category: '',
  coverUrl: '',
  attachmentUrl: '',
  pinned: false,
  recommended: false,
  popupEnabled: false
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
    category: '',
    coverUrl: '',
    attachmentUrl: '',
    pinned: false,
    recommended: false,
    popupEnabled: false,
    status: 'draft'
  }
})
const totalPages = computed(() => Math.max(1, Math.ceil(items.value.length / pageSize.value)))
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return items.value.slice(start, end)
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const rows = await getAnnouncements()
    items.value = Array.isArray(rows)
      ? rows.map(item => ({ ...item, popupEnabled: Boolean(item?.popupEnabled) }))
      : []
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
    Object.assign(draft, {
      title: '',
      summary: '',
      content: '',
      category: '',
      coverUrl: '',
      attachmentUrl: '',
      pinned: false,
      recommended: false,
      popupEnabled: false
    })
    showToast({ message: '公告已创建', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '创建失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function save(item, options = {}) {
  saving.value = true
  try {
    const updated = await saveAnnouncement({ ...item })
    Object.assign(item, updated)
    const message = options.fromEditor ? `公告已保存（字数：${options.contentLength || 0}）` : '公告已保存'
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
    items.value = items.value.filter((entry) => entry.id !== item.id)
    showToast({ message: '公告已删', type: 'success' })
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
  detailDialog.title = item.title || '公告详情'
  detailDialog.form = {
    title: item.title || '',
    summary: item.summary || '',
    content: item.content || '',
    category: item.category || '',
    coverUrl: item.coverUrl || '',
    attachmentUrl: item.attachmentUrl || '',
    pinned: Boolean(item.pinned),
    recommended: Boolean(item.recommended),
    popupEnabled: Boolean(item.popupEnabled),
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
    const updated = await saveAnnouncement(payload)
    Object.assign(item, updated)
    Object.assign(detailDialog.form, updated, { popupEnabled: Boolean(updated?.popupEnabled) })
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

.announcement-create {
  margin-top: 14px;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: #fff;
  padding: 16px;
  display: grid;
  gap: 14px;
}

.announcement-create-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.field-block {
  display: grid;
  gap: 6px;
}

.field-block-wide {
  grid-column: span 1;
}

.field-label {
  color: var(--lc-muted);
  font-size: 12px;
  font-weight: 700;
}

.announcement-create-flags {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 4px 0;
}

.announcement-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.announcement-item {
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: #fff;
  padding: 14px;
  box-shadow: var(--lc-shadow-sm);
  display: grid;
  gap: 12px;
}

.announcement-item-grid {
  display: grid;
  grid-template-columns: 220px repeat(2, minmax(0, 1fr));
  gap: 12px;
  align-items: start;
}

.announcement-brief-meta {
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
  .announcement-item-grid {
    grid-template-columns: 1fr 1fr;
  }

  .announcement-create-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

}

@media (max-width: 1120px) {
  .announcement-item-grid {
    grid-template-columns: 1fr;
  }

  .announcement-create-grid {
    grid-template-columns: 1fr;
  }

}
</style>



