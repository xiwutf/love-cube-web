<template>
  <div class="topics-m">
    <header class="topics-head">
      <button type="button" class="back" aria-label="返回" @click="goBack()">‹</button>
      <div>
        <h1>话题广场</h1>
        <p>按兴趣交流，发现同好</p>
      </div>
    </header>

    <div v-if="loading" class="loading">加载中…</div>

    <template v-else>
      <article
        v-for="topic in topics"
        :key="topic.id"
        class="topic-card"
        @click="openTopic(topic.id)"
      >
        <h3>{{ topic.title }}</h3>
        <p>{{ topic.description }}</p>
        <div class="topic-meta">
          <span>{{ topic.postCount || 0 }} 条讨论</span>
          <span>热度 {{ topic.heat || 0 }}</span>
        </div>
      </article>
    </template>

    <van-popup v-model:show="showDetail" position="bottom" round :style="{ height: '88vh' }">
      <div class="detail">
        <h2>{{ detail.title }}</h2>
        <p class="detail-desc">{{ detail.description }}</p>
        <div class="post-list">
          <div v-for="post in detail.posts || []" :key="post.id" class="post-item">
            <p class="post-author">{{ post.authorName }}</p>
            <p class="post-content">{{ post.content }}</p>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
          <van-empty v-if="!(detail.posts || []).length" description="还没有讨论，来发第一条吧" image-size="60" />
        </div>
        <van-field
          v-model="draft"
          rows="2"
          type="textarea"
          maxlength="500"
          placeholder="分享你的想法…"
        />
        <van-button round block type="primary" color="#6366f1" :loading="submitting" @click="submitPost">
          发布
        </van-button>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { createInterestTopicPost, fetchInterestTopicDetail, fetchInterestTopics } from '@/api/interestTopics.js'
import { useBackNavigation } from '@/composables/useBackNavigation.js'

const { goBack } = useBackNavigation('/m/platform')
const loading = ref(false)
const topics = ref([])
const showDetail = ref(false)
const detail = ref({})
const activeTopicId = ref(null)
const draft = ref('')
const submitting = ref(false)

async function loadTopics() {
  loading.value = true
  try {
    const res = await fetchInterestTopics()
    topics.value = Array.isArray(res) ? res : []
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

async function openTopic(id) {
  activeTopicId.value = id
  draft.value = ''
  try {
    detail.value = await fetchInterestTopicDetail(id, { page: 1, size: 20 })
    showDetail.value = true
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载话题失败' })
  }
}

async function submitPost() {
  if (!activeTopicId.value || !draft.value.trim()) {
    showToast('请输入内容')
    return
  }
  submitting.value = true
  try {
    await createInterestTopicPost(activeTopicId.value, draft.value.trim())
    showToast({ type: 'success', message: '发布成功' })
    draft.value = ''
    detail.value = await fetchInterestTopicDetail(activeTopicId.value, { page: 1, size: 20 })
    await loadTopics()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '发布失败' })
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
.topics-m {
  min-height: 100vh;
  padding: 12px 14px 24px;
  background: #f4f5fb;
}

.topics-head {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.back {
  display: grid;
  place-items: center;
  background: #fff;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 22px;
  text-decoration: none;
  color: var(--lc-indigo, #4f46e5);
  border: 1px solid var(--lc-soft, #e8ecf4);
  padding: 0;
  cursor: pointer;
}

.topics-head h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
}

.topics-head p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}

.topic-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 10px;
  border: 1px solid #e8ecf4;
}

.topic-card h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}

.topic-card p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.topic-meta {
  display: flex;
  gap: 12px;
  margin-top: 10px;
  font-size: 11px;
  color: #94a3b8;
}

.detail {
  padding: 16px 14px calc(16px + env(safe-area-inset-bottom));
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail h2 {
  margin: 0;
  font-size: 18px;
}

.detail-desc {
  margin: 6px 0 12px;
  font-size: 13px;
  color: #64748b;
}

.post-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 10px;
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
  color: #334155;
  line-height: 1.5;
}

.post-time {
  font-size: 10px;
  color: #94a3b8;
}

.loading {
  text-align: center;
  padding: 40px 0;
  color: #94a3b8;
}
</style>
