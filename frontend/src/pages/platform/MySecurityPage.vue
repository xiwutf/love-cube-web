<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">账号安全</h1>
    </header>

    <div class="sp-body">
      <!-- 安全评分 -->
      <div class="sp-card sec-score-card">
        <div class="sec-score-label">安全等级</div>
        <div class="sec-score" :class="secScoreClass">{{ secScoreLabel }}</div>
        <div class="sec-score-hint">{{ secScoreHint }}</div>
      </div>

      <!-- 账号信息 -->
      <div class="sp-card">
        <div class="sp-card-title">账号信息</div>
        <div class="sec-row">
          <div class="sec-row-left">
            <span class="sec-icon">📱</span>
            <div>
              <div class="sec-row-name">手机号</div>
              <div class="sec-row-sub">{{ user?.phone ? maskPhone(user.phone) : '未绑定' }}</div>
            </div>
          </div>
          <span class="sec-badge" :class="user?.phone ? 'ok' : 'warn'">{{ user?.phone ? '已绑定' : '未绑定' }}</span>
        </div>
        <div class="sec-row">
          <div class="sec-row-left">
            <span class="sec-icon">🔑</span>
            <div>
              <div class="sec-row-name">登录密码</div>
              <div class="sec-row-sub">定期更换密码可提升安全性</div>
            </div>
          </div>
          <button type="button" class="sec-action-btn" @click="goChangePwd">修改</button>
        </div>
        <div class="sec-row no-border">
          <div class="sec-row-left">
            <span class="sec-icon">🪪</span>
            <div>
              <div class="sec-row-name">实名认证</div>
              <div class="sec-row-sub">{{ verifyLabel }}</div>
            </div>
          </div>
          <router-link to="/me/verify" class="sec-action-btn">{{ verifyAction }}</router-link>
        </div>
      </div>

      <!-- 安全建议 -->
      <div class="sp-card">
        <div class="sp-card-title">安全建议</div>
        <div v-for="tip in secTips" :key="tip.text" class="tip-row">
          <span class="tip-icon" :class="tip.ok ? 'ok' : 'warn'">{{ tip.ok ? '✓' : '!' }}</span>
          <span class="tip-text">{{ tip.text }}</span>
        </div>
      </div>

      <!-- 注册信息 -->
      <div class="sp-card">
        <div class="sec-info-row">
          <span>账号 ID</span><strong>{{ userIdDisplay }}</strong>
        </div>
        <div class="sec-info-row">
          <span>注册时间</span><strong>{{ registerDate }}</strong>
        </div>
        <div class="sec-info-row no-border">
          <span>当前状态</span><strong style="color:#059669">正常使用中</strong>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

const userIdDisplay = computed(() => user.value?.id || user.value?.userId || '--')
const verifyLabel = computed(() => {
  const s = String(user.value?.verificationStatus || 'none')
  if (s === 'approved') return '已认证'
  if (s === 'pending') return '认证审核中'
  return '未认证'
})
const verifyAction = computed(() => {
  const s = String(user.value?.verificationStatus || 'none')
  if (s === 'approved') return '查看'
  if (s === 'pending') return '查看进度'
  return '去认证'
})
const registerDate = computed(() => {
  const raw = user.value?.createdAt
  if (!raw) return '--'
  return String(raw).replace('T', ' ').slice(0, 10)
})

const secScore = computed(() => {
  let score = 0
  if (user.value?.phone) score++
  if (user.value?.verificationStatus === 'approved') score++
  return score
})

const secScoreLabel = computed(() => ['待提升', '一般', '良好'][secScore.value] || '待提升')
const secScoreClass = computed(() => ['warn', 'medium', 'ok'][secScore.value] || 'warn')
const secScoreHint = computed(() => {
  if (secScore.value === 0) return '建议绑定手机号并完成实名认证'
  if (secScore.value === 1) return '再完成一步即可达到良好安全等级'
  return '账号安全状态良好，请继续保持'
})

const secTips = computed(() => [
  { text: '已绑定手机号', ok: Boolean(user.value?.phone) },
  { text: '已完成实名认证', ok: user.value?.verificationStatus === 'approved' },
  { text: '定期更换密码', ok: false }
])

function maskPhone(phone) {
  const s = String(phone)
  if (s.length < 7) return s
  return s.slice(0, 3) + '****' + s.slice(-4)
}

function goChangePwd() {
  router.push('/fellowship/change-password')
}

onMounted(async () => {
  if (!user.value) await userStore.refreshCurrentUser().catch(() => {})
})
</script>

<style scoped>
.sp-page { min-height: 100vh; background: #f6f7fb; color: #0f172a; }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: #fff; border-bottom: 1px solid #eef0f4;
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: #4f46e5; cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-card {
  background: #fff; border: 1px solid #eef0f4; border-radius: 18px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 14px; padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 14px; color: #0f172a; }

.sec-score-card { text-align: center; padding: 20px 16px; }
.sec-score-label { font-size: 12px; color: #94a3b8; margin-bottom: 8px; }
.sec-score {
  display: inline-block; font-size: 22px; font-weight: 900;
  padding: 6px 20px; border-radius: 999px; margin-bottom: 8px;
}
.sec-score.ok { background: #dcfce7; color: #059669; }
.sec-score.medium { background: #fef3c7; color: #d97706; }
.sec-score.warn { background: #fee2e2; color: #dc2626; }
.sec-score-hint { font-size: 12px; color: #64748b; }

.sec-row {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; padding: 13px 0; border-bottom: 1px solid #f1f5f9;
}
.sec-row.no-border { border-bottom: 0; padding-bottom: 0; }
.sec-row-left { display: flex; align-items: center; gap: 12px; flex: 1; min-width: 0; }
.sec-icon { font-size: 20px; width: 28px; text-align: center; flex: 0 0 auto; }
.sec-row-name { font-size: 14px; font-weight: 600; color: #1e293b; }
.sec-row-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.sec-badge {
  flex: 0 0 auto; border-radius: 999px; padding: 3px 10px;
  font-size: 11px; font-weight: 700;
}
.sec-badge.ok { background: #dcfce7; color: #059669; }
.sec-badge.warn { background: #fee2e2; color: #dc2626; }
.sec-action-btn {
  flex: 0 0 auto; border: 1px solid #eef0f4; border-radius: 999px;
  background: #fff; color: #4f46e5;
  font-size: 13px; font-weight: 700; padding: 5px 14px; cursor: pointer;
  text-decoration: none;
}

.tip-row {
  display: flex; align-items: center; gap: 10px; padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}
.tip-row:last-child { border-bottom: 0; padding-bottom: 0; }
.tip-icon {
  width: 22px; height: 22px; flex: 0 0 auto;
  display: grid; place-items: center; border-radius: 50%;
  font-size: 12px; font-weight: 800;
}
.tip-icon.ok { background: #dcfce7; color: #059669; }
.tip-icon.warn { background: #fef3c7; color: #d97706; }
.tip-text { font-size: 13px; color: #334155; }

.sec-info-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 11px 0; border-bottom: 1px solid #f1f5f9; font-size: 13px; color: #64748b;
}
.sec-info-row.no-border { border-bottom: 0; padding-bottom: 0; }
.sec-info-row strong { color: #1e293b; }

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
