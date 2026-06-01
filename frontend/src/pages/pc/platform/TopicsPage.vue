<template>
  <main class="topics-pc">
    <header class="topics-pc-head">
      <router-link to="/pc/platform/play" class="back">← 成长玩法</router-link>
      <div>
        <h1>话题广场</h1>
        <p>按兴趣交流，发现同好</p>
      </div>
    </header>

    <p v-if="loading" class="loading">加载中…</p>

    <section v-else class="topics-grid">
      <article
        v-for="topic in topics"
        :key="topic.id"
        class="topic-card"
        tabindex="0"
        @click="openTopic(topic.id)"
        @keyup.enter="openTopic(topic.id)"
      >
        <h3>{{ topic.title }}</h3>
        <p>{{ topic.description }}</p>
        <div class="topic-meta">
          <span>{{ topic.postCount || 0 }} 条讨论</span>
          <span>热度 {{ topic.heat || 0 }}</span>
        </div>
      </article>
    </section>

    <div v-if="showDetail" class="modal-mask" @click.self="closeDetail">
      <section class="modal-panel" role="dialog" aria-modal="true">
        <header class="modal-head">
          <h2>{{ detail.title }}</h2>
          <button type="button" class="close" aria-label="关闭" @click="closeDetail">×</button>
        </header>
        <p class="modal-desc">{{ detail.description }}</p>
        <div class="post-list">
          <article v-for="post in detail.posts || []" :key="post.id" class="post-item">
            <p class="post-author">{{ post.authorName }}</p>
            <p class="post-content">{{ post.content }}</p>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </article>
          <p v-if="!(detail.posts || []).length" class="empty-posts">还没有讨论，来发第一条吧</p>
        </div>
        <form class="compose" @submit.prevent="submitPost">
          <textarea
            v-model.trim="draft"
            rows="3"
            maxlength="500"
            placeholder="分享你的想法…"
          />
          <button type="submit" class="btn primary" :disabled="submitting">
            {{ submitting ? '发布中…' : '发布' }}
          </button>
        </form>
        <p v-if="formError" class="form-err">{{ formError }}</p>
      </section>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { createInterestTopicPost, fetchInterestTopicDetail, fetchInterestTopics } from '@/api/interestTopics.js'

const loading = ref(false)
const topics = ref([])
const showDetail = ref(false)
const detail = ref({})
const activeTopicId = ref(null)
const draft = ref('')
const submitting = ref(false)
const formError = ref('')

async function loadTopics() {
  loading.value = true
  try {
    const res = await fetchInterestTopics()
    topics.value = Array.isArray(res) ? res : []
  } catch (e) {
    formError.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function openTopic(id) {
  activeTopicId.value = id
  draft.value = ''
  formError.value = ''
  try {
    detail.value = await fetchInterestTopicDetail(id, { page: 1, size: 20 })
    showDetail.value = true
  } catch (e) {
    formError.value = e.message || '加载话题失败'
  }
}

function closeDetail() {
  showDetail.value = false
}

async function submitPost() {
  if (!activeTopicId.value || !draft.value.trim()) {
    formError.value = '请输入内容'
    return
  }
  submitting.value = true
  formError.value = ''
  try {
    await createInterestTopicPost(activeTopicId.value, draft.value.trim())
    draft.value = ''
    detail.value = await fetchInterestTopicDetail(activeTopicId.value, { page: 1, size: 20 })
    await loadTopics()
  } catch (e) {
    formError.value = e.message || '发布失败'
  } finally {
    submitting.value = false
  }
}

function formatTime(value) {
  if (!value) return '刚刚'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadTopics)
</script>

<style scoped>
.topics-pc {
  max-width: 1100px;
  margin: 0 auto;
  padding: 28px 24px 48px;
}

.topics-pc-head {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 24px;
}

.back {
  font-size: 14px;
  color: var(--lc-blue, #2563eb);
  text-decoration: none;
  padding-top: 4px;
}

.topics-pc-head h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
}

.topics-pc-head p {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--lc-subtle, #64748b);
}

.topics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.topic-card {
  background: var(--lc-surface, #fff);
  border: 1px solid var(--lc-soft, #e8ecf4);
  border-radius: 14px;
  padding: 18px;
  cursor: pointer;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
}

.topic-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.topic-card h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}

.topic-card p {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--lc-subtle, #64748b);
  line-height: 1.5;
}

.topic-meta {
  display: flex;
  gap: 14px;
  margin-top: 12px;
  font-size: 12px;
  color: #94a3b8;
}

.loading {
  text-align: center;
  padding: 48px;
  color: var(--lc-subtle, #94a3b8);
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.modal-panel {
  width: min(640px, 100%);
  max-height: 88vh;
  display: flex;
  flex-direction: column;
  background: var(--lc-surface, #fff);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.12);
}

.modal-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.modal-head h2 {
  margin: 0;
  font-size: 20px;
}

.close {
  border: none;
  background: transparent;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  color: var(--lc-subtle, #64748b);
}

.modal-desc {
  margin: 8px 0 12px;
  font-size: 14px;
  color: var(--lc-subtle, #64748b);
}

.post-list {
  flex: 1;
  overflow-y: auto;
  min-height: 120px;
  max-height: 40vh;
  margin-bottom: 12px;
}

.post-item {
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}

.post-author {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  color: #4338ca;
}

.post-content {
  margin: 4px 0 0;
  font-size: 14px;
  line-height: 1.5;
}

.post-time {
  font-size: 11px;
  color: #94a3b8;
}

.empty-posts {
  text-align: center;
  color: var(--lc-subtle, #94a3b8);
  font-size: 13px;
  padding: 24px 0;
}

.compose textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--lc-soft, #e2e8f0);
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
  resize: vertical;
  margin-bottom: 10px;
}

.btn.primary {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-err {
  margin: 8px 0 0;
  font-size: 13px;
  color: #dc2626;
}

@media (max-width: 980px) {
  .topics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
