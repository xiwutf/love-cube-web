<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">资讯管理</h1>
      <p class="platform-subtitle">PC 端支持完整编辑与发布；手机端提供轻量查看</p>

      <div class="admin-toolbar admin-desktop-only">
        <input v-model="draft.title" class="admin-input" placeholder="资讯标题" />
        <input v-model="draft.tag" class="admin-input" placeholder="标签" />
        <input v-model="draft.category" class="admin-input" placeholder="分类（填" />
        <CoverUploadField v-model="draft.coverUrl" :disabled="saving" />
        <label class="admin-check"><input type="checkbox" v-model="draft.pinned" /> 置顶</label>
        <label class="admin-check"><input type="checkbox" v-model="draft.recommended" /> 推荐</label>
        <button type="button" class="admin-btn primary" :disabled="saving" @click="create">新增资讯</button>
      </div>
      <textarea v-model="draft.content" class="admin-textarea admin-desktop-only" placeholder="资讯内容" ></textarea>

      <p class="admin-mobile-note admin-mobile-only">移动端仅支持查看，编辑请使用 PC 端。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>标签 / 分类</th>
            <th>封面</th>
            <th>标记</th>
            <th>状态</th>
            <th>摘要</th>
            <th>内容编辑</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.title }}</td>
            <td>
              <input v-model="item.tag" class="admin-input" placeholder="标签" style="margin-bottom: 6px" />
              <input v-model="item.category" class="admin-input" placeholder="分类" />
            </td>
            <td>
              <CoverUploadField v-model="item.coverUrl" :disabled="saving" />
            </td>
            <td>
              <label class="admin-check"><input type="checkbox" v-model="item.pinned" /> 置顶</label>
              <label class="admin-check"><input type="checkbox" v-model="item.recommended" /> 推荐</label>
            </td>
            <td><span class="admin-tag" :class="item.status">{{ item.status }}</span></td>
            <td><input v-model="item.summary" class="admin-input" /></td>
            <td><textarea v-model="item.content" class="admin-textarea" ></textarea></td>
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
      <van-empty v-if="!items.length" description="暂无资讯" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.title }}</strong>
          <span class="admin-tag" :class="item.status">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">标签：{{ item.tag }}{{ item.category ? ` / ${item.category}` : '' }}</p>
        <p class="admin-row-meta">
          <span v-if="item.pinned">置顶 · </span>
          <span v-if="item.recommended">推荐</span>
        </p>
        <p class="admin-row-meta">{{ item.summary }}</p>
        <p class="admin-row-meta">更新：{{ formatDate(item.updatedAt) }}</p>
      </article>
      <van-empty v-if="!items.length" description="暂无资讯" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import CoverUploadField from '@/components/admin/CoverUploadField.vue'
import { getArticles, saveArticle, deleteArticle } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])
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
    showToast({ message: '资讯已创', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '创建失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function save(item) {
  saving.value = true
  try {
    const updated = await saveArticle({ ...item })
    Object.assign(item, updated)
    showToast({ message: '资讯已保', type: 'success' })
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
    showToast({ message: '状已更新', type: 'success' })
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
}

.admin-check input[type='checkbox'] {
  accent-color: #e84f73;
}
</style>



