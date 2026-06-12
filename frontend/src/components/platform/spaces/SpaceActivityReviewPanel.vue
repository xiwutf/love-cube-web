<template>
  <section class="activity-review operation-shell">
    <div class="section-head operation-section-head">
      <div>
        <p class="section-kicker">Activity Review</p>
        <h2>活动审核</h2>
        <p class="hint">审核成员投稿的活动，通过后自动发布到团体活动列表。</p>
      </div>
      <span class="status-badge" :class="pendingCount > 0 ? 'warning' : 'neutral'">
        {{ pendingCount > 0 ? `${pendingCount} 待审核` : '暂无待审' }}
      </span>
    </div>

    <div v-if="loading" class="inline-state">加载中…</div>
    <div v-else-if="error" class="inline-state error-state">{{ error }}</div>

    <div v-else-if="items.length" class="proposal-list">
      <article v-for="item in items" :key="item.id" class="proposal-card platform-card">
        <header>
          <div>
            <strong>{{ item.title }}</strong>
            <p class="meta-line">
              投稿人：{{ item.creatorName || '成员' }}
              · {{ formatDate(item.createdAt) }}
            </p>
          </div>
          <span class="status-badge warning">待审核</span>
        </header>

        <p class="proposal-desc">{{ item.description || '暂无简介' }}</p>
        <p class="meta-line">
          {{ formatDate(item.startTime) }} — {{ formatDate(item.endTime) }}
          <span v-if="item.location"> · {{ item.location }}</span>
        </p>

        <div v-if="reviewingId === item.id" class="review-form">
          <label class="field">
            <span>审核意见</span>
            <textarea v-model.trim="reviewComment" rows="2" maxlength="500" placeholder="选填，驳回时建议填写原因" />
          </label>
          <div class="form-actions">
            <button type="button" class="btn ghost" @click="cancelReview">取消</button>
            <button type="button" class="btn danger" :disabled="saving" @click="submitReview(item, false)">
              {{ saving ? '处理中…' : '驳回' }}
            </button>
            <button type="button" class="btn primary" :disabled="saving" @click="submitReview(item, true)">
              {{ saving ? '处理中…' : '通过并发布' }}
            </button>
          </div>
        </div>
        <div v-else class="card-actions">
          <button type="button" class="btn primary sm" @click="startReview(item)">审核</button>
        </div>
      </article>
    </div>

    <p v-else class="inline-state">暂无待审核的活动投稿</p>
  </section>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { fetchPendingActivityProposals, reviewActivityProposal } from '@/api/groupActivityProposals.js'

const props = defineProps({
  groupId: { type: [String, Number], required: true }
})

const emit = defineEmits(['updated', 'flash'])

const loading = ref(false)
const error = ref('')
const items = ref([])
const pendingCount = ref(0)
const reviewingId = ref(null)
const reviewComment = ref('')
const saving = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchPendingActivityProposals(props.groupId)
    const data = res?.data ?? res
    items.value = data?.items ?? []
    pendingCount.value = Number(data?.pendingCount ?? items.value.length)
  } catch (err) {
    error.value = err?.message || '加载失败'
    items.value = []
    pendingCount.value = 0
  } finally {
    loading.value = false
  }
}

function formatDate(v) {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function startReview(item) {
  reviewingId.value = item.id
  reviewComment.value = ''
}

function cancelReview() {
  reviewingId.value = null
  reviewComment.value = ''
}

async function submitReview(item, approved) {
  saving.value = true
  try {
    const res = await reviewActivityProposal(props.groupId, item.id, {
      approved,
      comment: reviewComment.value || (approved ? '审核通过' : '未通过审核')
    })
    const msg = res?.message || (approved ? '审核通过，活动已发布' : '已驳回活动投稿')
    emit('flash', msg, 'success')
    reviewingId.value = null
    reviewComment.value = ''
    await load()
    emit('updated', pendingCount.value)
  } catch (err) {
    emit('flash', err?.message || '审核失败', 'error')
  } finally {
    saving.value = false
  }
}

onMounted(load)

watch(() => props.groupId, load)

defineExpose({ load, pendingCount })
</script>

<style scoped>
.activity-review {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-4);
}

.hint {
  margin: var(--lc-space-1) 0 0;
  color: var(--lc-text-muted);
  font-size: var(--lc-text-sm);
}

.proposal-list {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-3);
}

.proposal-card {
  padding: var(--lc-space-4);
}

.proposal-card header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-2);
}

.proposal-desc {
  margin: 0 0 var(--lc-space-2);
  color: var(--lc-text-secondary);
}

.meta-line {
  margin: 0;
  color: var(--lc-text-muted);
  font-size: var(--lc-text-sm);
}

.review-form {
  margin-top: var(--lc-space-3);
  padding-top: var(--lc-space-3);
  border-top: 1px solid var(--lc-border);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-1);
  margin-bottom: var(--lc-space-3);
}

.field textarea {
  width: 100%;
  padding: var(--lc-space-2);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-md);
  font: inherit;
}

.form-actions,
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--lc-space-2);
}

.error-state {
  color: var(--lc-danger);
}
</style>
