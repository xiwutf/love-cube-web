<template>
  <section class="help-detail">
    <p v-if="loading" class="muted">加载中…</p>
    <p v-else-if="error" class="err">{{ error }}</p>

    <template v-else-if="detail">
      <header class="head">
        <router-link to="/platform/help" class="back">← 互助广场</router-link>
        <div class="head-row">
          <span class="pill">{{ typeLabel(detail.helpType) }}</span>
          <span :class="['badge', detail.status]">{{ statusLabel(detail.status) }}</span>
        </div>
        <h1>{{ detail.title }}</h1>
        <p class="meta">
          {{ detail.publisherName }}
          <template v-if="detail.region"> · {{ detail.region }}</template>
          · 发布于 {{ formatTime(detail.createdAt) }}
          <template v-if="detail.deadline"> · 期望 {{ detail.deadline }}</template>
        </p>
      </header>

      <article class="body">
        <h2>需求说明</h2>
        <p class="content">{{ detail.content }}</p>
        <div v-if="detail.contactType || detail.contactValue" class="contact-box">
          <h3>发布者联系方式</h3>
          <p>{{ detail.contactType || '—' }}：{{ detail.contactValue || '—' }}</p>
        </div>
        <p v-else-if="detail.status === 'active' && !detail.viewerIsAuthor" class="hint">
          发布者联系方式将在您被确认互助后展示（若对方已填写）。
        </p>

        <div v-if="detail.status === 'resolved' && detail.helperUserId" class="resolve-banner">
          <strong>已解决</strong>
          <span v-if="detail.resolvedNote">感谢：{{ detail.resolvedNote }}</span>
          <p v-if="helperStats" class="stats-line">
            帮助人互助数据：发起互助 {{ helperStats.helpCount }} 次 · 被确认 {{ helperStats.acceptedCount }} 次 · 成功帮助他人 {{ helperStats.successCount }} 次
          </p>
        </div>
      </article>

      <section class="replies">
        <h2>互助意向 <span class="count">（{{ replies.length }}）</span></h2>
        <p class="muted small">这里展示的是「互助意向」，不是讨论区留言。</p>
        <ul>
          <li v-for="r in replies" :key="r.id" class="reply">
            <div class="reply-head">
              <strong>{{ r.userName }}</strong>
              <span class="pill sm">{{ r.statusLabel || replyStatusLabel(r.status) }}</span>
              <span v-if="r.isHelper" class="pill sm success">最终帮助人</span>
            </div>
            <p class="reply-msg">{{ r.message }}</p>
            <div v-if="r.contactValue || r.contactType" class="reply-contact">
              <span>对方联系方式：{{ r.contactType || '' }} {{ r.contactValue || '' }}</span>
            </div>
            <div v-if="detail.viewerIsAuthor && r.status === 'pending' && detail.status === 'active'" class="reply-actions">
              <button
                type="button"
                class="btn sm primary"
                :disabled="actioning"
                @click="accept(r.id)"
              >接受</button>
              <button
                type="button"
                class="btn sm ghost danger"
                :disabled="actioning"
                @click="reject(r.id)"
              >拒绝</button>
            </div>
          </li>
        </ul>
        <p v-if="!replies.length" class="muted">暂时还没有互助意向</p>
      </section>

      <footer class="foot-actions" v-if="detail.status === 'active'">
        <template v-if="!detail.viewerIsAuthor && userStore.isLoggedIn && canOffer">
          <button type="button" class="btn primary" @click="openOffer">我能帮忙</button>
        </template>
        <template v-else-if="!userStore.isLoggedIn">
          <router-link to="/login" class="btn primary">登录后我能帮忙</router-link>
        </template>
        <template v-if="detail.viewerIsAuthor">
          <button type="button" class="btn success" :disabled="actioning || !accepted.length" @click="openResolve">
            标记已解决
          </button>
          <button type="button" class="btn ghost" :disabled="actioning" @click="closeReq">关闭需求</button>
        </template>
      </footer>
    </template>

    <!-- 我能帮忙 -->
    <div v-if="offerOpen" class="modal-mask" @click.self="offerOpen = false">
      <div class="modal" role="dialog" aria-modal="true">
        <h3>我能提供什么帮助</h3>
        <textarea v-model="offerForm.message" rows="5" maxlength="2000" placeholder="简要说明你能提供的帮助"></textarea>
        <label class="check">
          <input v-model="offerForm.contactWilling" type="checkbox">
          我愿意提供联系方式
        </label>
        <template v-if="offerForm.contactWilling">
          <input v-model.trim="offerForm.contactType" class="mt" placeholder="联系方式类型，如微信" maxlength="32">
          <input v-model.trim="offerForm.contactValue" class="mt" placeholder="联系方式" maxlength="200">
        </template>
        <p v-if="offerErr" class="err">{{ offerErr }}</p>
        <div class="modal-actions">
          <button type="button" class="btn ghost" @click="offerOpen = false">取消</button>
          <button type="button" class="btn primary" :disabled="offerSaving" @click="submitOffer">{{ offerSaving ? '提交中…' : '提交意向' }}</button>
        </div>
      </div>
    </div>

    <!-- 标记已解决 -->
    <div v-if="resolveOpen" class="modal-mask" @click.self="resolveOpen = false">
      <div class="modal" role="dialog" aria-modal="true">
        <h3>标记已解决</h3>
        <p class="muted small">请选择实际帮助您解决问题的用户（须为已接受的互助意向）。</p>
        <label class="field">
          <span>帮助我的人</span>
          <select v-model.number="resolveForm.helperUserId">
            <option disabled :value="0">请选择</option>
            <option v-for="u in accepted" :key="u.userId" :value="u.userId">{{ u.userName }}（用户 {{ u.userId }}）</option>
          </select>
        </label>
        <label class="field">
          <span>一句感谢（选填）</span>
          <input v-model.trim="resolveForm.thankNote" maxlength="500" placeholder="选填">
        </label>
        <p v-if="resolveErr" class="err">{{ resolveErr }}</p>
        <div class="modal-actions">
          <button type="button" class="btn ghost" @click="resolveOpen = false">取消</button>
          <button type="button" class="btn success" :disabled="resolveSaving" @click="submitResolve">{{ resolveSaving ? '提交中…' : '确认已解决' }}</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  acceptHelpReply,
  closeHelpRequest,
  createHelpReply,
  fetchHelpRequestDetail,
  fetchHelpUserStats,
  rejectHelpReply,
  resolveHelpRequest
} from '@/api/help.js'
import { useUserStore } from '@/stores/user.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const error = ref('')
const detail = ref(null)
const actioning = ref(false)

const offerOpen = ref(false)
const offerSaving = ref(false)
const offerErr = ref('')
const offerForm = ref({
  message: '',
  contactWilling: false,
  contactType: '',
  contactValue: ''
})

const resolveOpen = ref(false)
const resolveSaving = ref(false)
const resolveErr = ref('')
const resolveForm = ref({ helperUserId: 0, thankNote: '' })
const helperStats = ref(null)

const TYPE_LABELS = {
  JOB_SEEK: '找工作',
  RECRUIT: '招人',
  FIND_MATERIAL: '找资料',
  OFFER_RESOURCE: '提供资源',
  ASK_EXP: '求经验',
  OTHER: '其他'
}

const replies = computed(() => (Array.isArray(detail.value?.replies) ? detail.value.replies : []))

const accepted = computed(() => replies.value.filter((r) => r.status === 'accepted'))

const canOffer = computed(() => {
  if (!detail.value) return false
  return String(detail.value.userId) !== String(userStore.userId)
})

function typeLabel(c) {
  return TYPE_LABELS[c] || c
}

function statusLabel(s) {
  return (
    {
      pending: '审核中',
      active: '进行中',
      resolved: '已解决',
      closed: '已关闭',
      rejected: '未通过审核'
    }[s] || s
  )
}

function replyStatusLabel(s) {
  return { pending: '待确认', accepted: '已接受', rejected: '已拒绝' }[s] || s
}

function formatTime(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function load() {
  loading.value = true
  error.value = ''
  helperStats.value = null
  const id = route.params.id
  try {
    const res = await fetchHelpRequestDetail(id)
    detail.value = res
    if (res?.status === 'resolved' && res?.helperUserId) {
      try {
        helperStats.value = await fetchHelpUserStats(res.helperUserId)
      } catch {
        helperStats.value = null
      }
    }
  } catch (e) {
    error.value = e.message || '加载失败'
    detail.value = null
  } finally {
    loading.value = false
  }
}

function openOffer() {
  offerErr.value = ''
  offerOpen.value = true
}

async function submitOffer() {
  offerSaving.value = true
  offerErr.value = ''
  try {
    await createHelpReply(route.params.id, {
      message: offerForm.value.message,
      contactWilling: offerForm.value.contactWilling,
      contactType: offerForm.value.contactType,
      contactValue: offerForm.value.contactValue
    })
    offerOpen.value = false
    offerForm.value = { message: '', contactWilling: false, contactType: '', contactValue: '' }
    router.replace({ path: route.path, query: {} })
    await load()
  } catch (e) {
    offerErr.value = e.message || '提交失败'
  } finally {
    offerSaving.value = false
  }
}

async function accept(replyId) {
  actioning.value = true
  try {
    await acceptHelpReply(replyId)
    await load()
  } catch (e) {
    error.value = e.message || '操作失败'
  } finally {
    actioning.value = false
  }
}

async function reject(replyId) {
  if (!window.confirm('确定拒绝该互助意向吗？拒绝后对方不会计入「被接受」统计。')) return
  actioning.value = true
  try {
    const res = await rejectHelpReply(replyId)
    if (res && res.success === false) {
      throw new Error(res.message || '操作失败')
    }
    await load()
  } catch (e) {
    error.value = e.message || '操作失败'
  } finally {
    actioning.value = false
  }
}

function openResolve() {
  resolveErr.value = ''
  resolveForm.value = { helperUserId: accepted.value[0]?.userId || 0, thankNote: '' }
  resolveOpen.value = true
}

async function submitResolve() {
  if (!resolveForm.value.helperUserId) {
    resolveErr.value = '请选择帮助人'
    return
  }
  resolveSaving.value = true
  resolveErr.value = ''
  try {
    await resolveHelpRequest(route.params.id, {
      helperUserId: resolveForm.value.helperUserId,
      thankNote: resolveForm.value.thankNote
    })
    resolveOpen.value = false
    await load()
  } catch (e) {
    resolveErr.value = e.message || '提交失败'
  } finally {
    resolveSaving.value = false
  }
}

async function closeReq() {
  if (!window.confirm('确定要关闭该需求吗？关闭后无法再回应。')) return
  actioning.value = true
  try {
    await closeHelpRequest(route.params.id)
    await load()
  } catch (e) {
    error.value = e.message || '关闭失败'
  } finally {
    actioning.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    load()
  }
)

watch(
  () => [route.query.offer, detail.value?.id],
  () => {
    if (route.query.offer === '1' && userStore.isLoggedIn && detail.value && canOffer.value) {
      offerOpen.value = true
    }
  }
)

onMounted(async () => {
  await load()
  if (route.query.offer === '1' && userStore.isLoggedIn && canOffer.value) {
    offerOpen.value = true
  }
})
</script>

<style scoped>
.help-detail {
  max-width: 720px;
  margin: 0 auto;
  padding: 1.5rem 1rem 3rem;
  color: var(--lc-text);
}

.back {
  display: inline-block;
  margin-bottom: 0.75rem;
  color: var(--lc-blue);
  text-decoration: none;
  font-weight: 600;
}

.head-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.35rem;
}

.head h1 {
  margin: 0.25rem 0;
  font-size: 1.45rem;
  line-height: 1.3;
}

.meta {
  margin: 0;
  color: var(--lc-muted);
  font-size: 0.9rem;
}

.pill {
  display: inline-block;
  padding: 0.2rem 0.55rem;
  border-radius: 6px;
  background: var(--lc-indigo-light);
  color: var(--lc-indigo);
  font-size: 0.8rem;
  font-weight: 600;
}

.pill.sm {
  font-size: 0.72rem;
}

.pill.success {
  background: var(--lc-green-light);
  color: var(--lc-green);
}

.badge {
  font-size: 0.78rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  background: var(--lc-soft);
  color: var(--lc-muted);
}

.badge.active {
  background: var(--lc-green-light);
  color: var(--lc-emerald);
}

.badge.pending {
  background: #fff7ed;
  color: var(--lc-orange);
}

.badge.resolved {
  background: var(--lc-blue-light);
  color: var(--lc-blue);
}

.body {
  margin-top: 1.25rem;
  padding: 1rem 0;
  border-top: 1px solid var(--lc-border);
}

.body h2 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
}

.content {
  white-space: pre-wrap;
  line-height: 1.65;
  color: var(--lc-slate);
}

.contact-box {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background: var(--lc-soft);
  border: 1px solid var(--lc-border);
}

.contact-box h3 {
  margin: 0 0 0.35rem;
  font-size: 0.9rem;
}

.hint {
  margin-top: 0.75rem;
  font-size: 0.88rem;
  color: var(--lc-muted);
}

.resolve-banner {
  margin-top: 1rem;
  padding: 0.85rem 1rem;
  border-radius: 8px;
  background: var(--lc-emerald-light);
  color: var(--lc-emerald);
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.stats-line {
  margin: 0;
  font-size: 0.88rem;
  color: var(--lc-text);
  font-weight: 500;
}

.replies {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--lc-border);
}

.replies h2 {
  margin: 0 0 0.25rem;
  font-size: 1.05rem;
}

.count {
  font-weight: 400;
  color: var(--lc-muted);
}

.small {
  font-size: 0.85rem;
}

.replies ul {
  list-style: none;
  margin: 0.75rem 0 0;
  padding: 0;
}

.reply {
  padding: 0.85rem 0;
  border-bottom: 1px solid var(--lc-border);
}

.reply-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.4rem;
}

.reply-msg {
  margin: 0.5rem 0 0;
  white-space: pre-wrap;
  line-height: 1.55;
}

.reply-contact {
  margin-top: 0.35rem;
  font-size: 0.88rem;
  color: var(--lc-muted);
}

.reply-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.btn.danger {
  border-color: var(--lc-red);
  color: var(--lc-red);
}

.foot-actions {
  margin-top: 1.5rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.65rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.55rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  border: 1px solid transparent;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.95rem;
}

.btn.primary {
  background: var(--lc-blue);
  color: #fff;
}

.btn.success {
  background: var(--lc-emerald);
  color: #fff;
}

.btn.ghost {
  background: var(--lc-surface);
  border-color: var(--lc-border);
  color: var(--lc-text);
}

.btn.sm {
  margin-top: 0.5rem;
  padding: 0.35rem 0.75rem;
  font-size: 0.85rem;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 80;
}

.modal {
  width: 100%;
  max-width: 420px;
  background: var(--lc-surface);
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.modal h3 {
  margin: 0 0 0.75rem;
}

.modal textarea,
.modal input,
.modal select {
  width: 100%;
  box-sizing: border-box;
  font: inherit;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  padding: 0.5rem 0.6rem;
}

.mt {
  margin-top: 0.5rem;
}

.check {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-top: 0.65rem;
  font-size: 0.88rem;
  color: var(--lc-muted);
}

.field {
  display: block;
  margin-top: 0.75rem;
}

.field span {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.88rem;
  font-weight: 600;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}

.muted {
  color: var(--lc-muted);
}

.err {
  color: var(--lc-red);
  font-size: 0.88rem;
}
</style>
