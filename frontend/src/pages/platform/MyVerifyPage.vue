<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">实名认证</h1>
    </header>

    <div class="sp-body">
      <p v-if="loading" class="sp-status">加载中...</p>
      <template v-else>
        <!-- 认证状态卡 -->
        <div class="sp-card vf-status-card">
          <div class="vf-icon">{{ statusIcon }}</div>
          <div class="vf-status-label" :class="statusClass">{{ statusLabel }}</div>
          <div class="vf-status-hint">{{ statusHint }}</div>
        </div>

        <!-- 认证说明 -->
        <div class="sp-card">
          <div class="sp-card-title">为什么要实名认证？</div>
          <div v-for="item in benefits" :key="item.title" class="vf-benefit-row">
            <span class="vf-benefit-icon">{{ item.icon }}</span>
            <div>
              <div class="vf-benefit-title">{{ item.title }}</div>
              <div class="vf-benefit-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 申请认证（未认证时显示） -->
        <div v-if="canApply" class="sp-card">
          <div class="sp-card-title">提交认证信息</div>
          <div class="vf-form">
            <label class="vf-label">
              <span>真实姓名</span>
              <input v-model.trim="form.realName" class="vf-input" placeholder="请输入真实姓名" maxlength="20" />
            </label>
            <label class="vf-label">
              <span>身份证号</span>
              <input v-model.trim="form.idCard" class="vf-input" placeholder="请输入身份证号" maxlength="18" />
            </label>
          </div>
          <p v-if="formErr" class="vf-err">{{ formErr }}</p>
          <p class="vf-tip">您的信息将被加密存储，仅用于身份核验，不会对外展示。</p>
          <button type="button" class="sp-primary-btn" :disabled="submitting" @click="handleSubmit">
            {{ submitting ? '提交中...' : '提交认证' }}
          </button>
        </div>

        <!-- 历史记录 -->
        <div v-if="records.length" class="sp-card">
          <div class="sp-card-title">认证记录</div>
          <div v-for="r in records" :key="r.id" class="vf-record">
            <span class="vf-record-time">{{ formatDate(r.createdAt) }}</span>
            <span class="vf-record-status" :class="r.status">{{ recordStatusLabel(r.status) }}</span>
          </div>
        </div>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { getMyVerifications, submitVerification } from '@/api/verification.js'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const loading = ref(false)
const submitting = ref(false)
const formErr = ref('')
const records = ref([])
const form = reactive({ realName: '', idCard: '' })

const verifyStatus = computed(() => String(user.value?.verificationStatus || 'none'))
const canApply = computed(() => verifyStatus.value === 'none' || verifyStatus.value === 'rejected')

const statusIcon = computed(() => ({ approved: '✅', pending: '⏳', rejected: '❌', none: '🪪' }[verifyStatus.value] || '🪪'))
const statusLabel = computed(() => ({ approved: '已完成实名认证', pending: '认证申请审核中', rejected: '认证未通过', none: '尚未进行实名认证' }[verifyStatus.value] || '尚未进行实名认证'))
const statusHint = computed(() => ({ approved: '您的账号已通过实名认证，可享受全部平台功能。', pending: '您的认证申请正在审核中，预计 1-3 个工作日内完成。', rejected: '您上次的认证申请未通过，请重新提交正确信息。', none: '完成实名认证后，可解锁更多平台功能。' }[verifyStatus.value] || ''))
const statusClass = computed(() => ({ approved: 'ok', pending: 'pending', rejected: 'fail', none: '' }[verifyStatus.value] || ''))

const benefits = [
  { icon: '🔓', title: '解锁全功能', desc: '创建团体、发布内容等高级功能需要实名认证' },
  { icon: '🛡️', title: '安全保障', desc: '保护您的账号安全，防止被盗用' },
  { icon: '🤝', title: '建立信任', desc: '实名用户更受社区信赖' }
]

function formatDate(raw) {
  if (!raw) return ''
  return String(raw).replace('T', ' ').slice(0, 10)
}

function recordStatusLabel(s) {
  return { approved: '审核通过', pending: '审核中', rejected: '未通过' }[s] || s
}

async function handleSubmit() {
  formErr.value = ''
  if (!form.realName) { formErr.value = '请输入真实姓名'; return }
  if (!form.idCard || form.idCard.length < 15) { formErr.value = '请输入有效身份证号'; return }
  submitting.value = true
  try {
    await submitVerification('REAL_NAME', { realName: form.realName, idCard: form.idCard })
    await userStore.refreshCurrentUser().catch(() => {})
    form.realName = ''
    form.idCard = ''
  } catch (e) {
    formErr.value = e?.message || '提交失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  if (!user.value) await userStore.refreshCurrentUser().catch(() => {})
  const res = await getMyVerifications().catch(() => null)
  records.value = Array.isArray(res) ? res : (Array.isArray(res?.list) ? res.list : [])
  loading.value = false
})
</script>

<style scoped>
.sp-page { min-height: 100vh; background: var(--lc-bg); color: var(--lc-text); }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: var(--lc-surface); border-bottom: 1px solid var(--lc-soft-alt);
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: var(--lc-indigo); cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-status { text-align: center; padding: 40px 0; color: var(--lc-subtle); font-size: 14px; }
.sp-card {
  background: var(--lc-surface); border: 1px solid var(--lc-soft-alt); border-radius: 18px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 14px; padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 14px; color: var(--lc-text); }

.vf-status-card { text-align: center; padding: 24px 16px; }
.vf-icon { font-size: 44px; margin-bottom: 12px; }
.vf-status-label { font-size: 16px; font-weight: 800; margin-bottom: 8px; }
.vf-status-label.ok { color: var(--lc-emerald); }
.vf-status-label.pending { color: var(--lc-amber); }
.vf-status-label.fail { color: var(--lc-red); }
.vf-status-hint { font-size: 13px; color: var(--lc-muted-light); line-height: 1.6; }

.vf-benefit-row { display: flex; align-items: flex-start; gap: 12px; padding: 10px 0; border-bottom: 1px solid var(--lc-soft); }
.vf-benefit-row:last-child { border-bottom: 0; padding-bottom: 0; }
.vf-benefit-icon { font-size: 20px; width: 28px; text-align: center; flex: 0 0 auto; }
.vf-benefit-title { font-size: 14px; font-weight: 600; color: var(--lc-text-deep); }
.vf-benefit-desc { font-size: 12px; color: var(--lc-subtle); margin-top: 3px; }

.vf-form { display: flex; flex-direction: column; gap: 12px; margin-bottom: 12px; }
.vf-label { display: flex; flex-direction: column; gap: 6px; font-size: 13px; font-weight: 700; color: var(--lc-muted); }
.vf-input {
  border: 1px solid var(--lc-soft-alt); border-radius: 12px; padding: 12px 14px;
  font: inherit; font-size: 14px; color: var(--lc-text); background: var(--lc-bg); outline: none;
}
.vf-input:focus { border-color: var(--lc-violet); background: var(--lc-surface); }
.vf-err { color: var(--lc-red); font-size: 13px; margin-bottom: 8px; }
.vf-tip { font-size: 11px; color: var(--lc-subtle); margin-bottom: 14px; line-height: 1.5; }
.sp-primary-btn {
  display: block; width: 100%; padding: 14px; border: 0; border-radius: 14px;
  background: linear-gradient(135deg, var(--lc-violet), var(--lc-indigo)); color: var(--lc-surface);
  font-size: 15px; font-weight: 800; cursor: pointer;
}
.sp-primary-btn:disabled { opacity: 0.6; cursor: default; }

.vf-record { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--lc-soft); }
.vf-record:last-child { border-bottom: 0; }
.vf-record-time { font-size: 12px; color: var(--lc-subtle); }
.vf-record-status { font-size: 12px; font-weight: 700; }
.vf-record-status.approved { color: var(--lc-emerald); }
.vf-record-status.pending { color: var(--lc-amber); }
.vf-record-status.rejected { color: var(--lc-red); }

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
