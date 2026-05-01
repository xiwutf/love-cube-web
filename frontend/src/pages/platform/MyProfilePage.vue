<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">我的资料</h1>
      <button type="button" class="sp-action-btn" @click="openEdit">编辑</button>
    </header>

    <div class="sp-body">
      <div class="sp-card sp-profile-card">
        <div class="pf-avatar-wrap">
          <img v-if="user?.avatar" :src="user.avatar" class="pf-avatar" alt="头像" />
          <div v-else class="pf-avatar pf-avatar-fb">{{ avatarFallback }}</div>
          <button type="button" class="pf-avatar-edit" @click="openEdit">更换</button>
        </div>
        <div class="pf-info">
          <div class="pf-name">{{ displayName }}</div>
          <div class="pf-meta">UID {{ userIdDisplay }}</div>
        </div>
      </div>

      <div class="sp-card">
        <div class="pf-row">
          <span class="pf-label">昵称</span>
          <span class="pf-value">{{ displayName }}</span>
        </div>
        <div class="pf-row">
          <span class="pf-label">手机号</span>
          <span class="pf-value">{{ user?.phone || '未绑定' }}</span>
        </div>
        <div class="pf-row">
          <span class="pf-label">所在地</span>
          <span class="pf-value">{{ user?.location || '未填写' }}</span>
        </div>
        <div class="pf-row">
          <span class="pf-label">个人简介</span>
          <span class="pf-value pf-bio">{{ user?.bio || '暂无简介' }}</span>
        </div>
        <div class="pf-row">
          <span class="pf-label">认证状态</span>
          <span class="pf-value" :class="{ 'pf-verified': user?.verificationStatus === 'approved' }">{{ verifyLabel }}</span>
        </div>
        <div class="pf-row">
          <span class="pf-label">注册时间</span>
          <span class="pf-value">{{ registerDate }}</span>
        </div>
        <div class="pf-row no-border">
          <span class="pf-label">邀请码</span>
          <span class="pf-value pf-invite">
            {{ inviteCode || '加载中...' }}
            <button v-if="inviteCode" type="button" class="pf-copy-btn" @click="copyInvite">复制</button>
            <em v-if="copyTip" :class="{ err: copyErr }">{{ copyTip }}</em>
          </span>
        </div>
      </div>

      <button type="button" class="sp-primary-btn" @click="openEdit">编辑个人资料</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getMyInviteCode } from '@/api/invite.js'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const inviteCode = ref('')
const copyTip = ref('')
const copyErr = ref(false)

const displayName = computed(() => user.value?.username || user.value?.nickname || '用户')
const userIdDisplay = computed(() => user.value?.id || user.value?.userId || '--')
const avatarFallback = computed(() => String(displayName.value || 'U').slice(0, 1).toUpperCase())

const verifyLabel = computed(() => {
  const s = String(user.value?.verificationStatus || 'none')
  if (s === 'approved') return '已认证'
  if (s === 'pending') return '认证审核中'
  if (s === 'rejected') return '认证未通过'
  return '未认证'
})

const registerDate = computed(() => {
  const raw = user.value?.createdAt
  if (!raw) return '--'
  return String(raw).replace('T', ' ').slice(0, 10)
})

function openEdit() {
  router.push({ path: '/me', query: { panel: 'edit' } })
}

async function copyInvite() {
  copyTip.value = ''
  copyErr.value = false
  try {
    await navigator.clipboard.writeText(inviteCode.value)
    copyTip.value = '已复制'
  } catch {
    copyErr.value = true
    copyTip.value = '复制失败'
  }
  setTimeout(() => { copyTip.value = '' }, 2000)
}

onMounted(async () => {
  if (!user.value) await userStore.refreshCurrentUser().catch(() => {})
  const res = await getMyInviteCode().catch(() => null)
  inviteCode.value = String(res?.inviteCode || res?.code || '').trim()
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
.sp-action-btn {
  padding: 0 16px; height: 52px;
  border: 0; background: none; font-size: 14px; color: var(--lc-indigo); font-weight: 700; cursor: pointer;
}

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}

.sp-card {
  background: var(--lc-surface); border: 1px solid var(--lc-soft-alt); border-radius: 18px;
  box-shadow: 0 4px 16px rgba(15,23,42,0.05); margin-bottom: 14px; overflow: hidden;
}

.sp-profile-card {
  display: flex; align-items: center; gap: 16px; padding: 20px 16px;
}
.pf-avatar-wrap { position: relative; flex: 0 0 auto; }
.pf-avatar {
  width: 80px; height: 80px; border-radius: 50%; object-fit: cover;
  border: 3px solid var(--lc-indigo-soft);
}
.pf-avatar-fb {
  display: grid; place-items: center;
  width: 80px; height: 80px; border-radius: 50%;
  background: linear-gradient(135deg, var(--lc-violet), var(--lc-indigo));
  color: var(--lc-surface); font-size: 28px; font-weight: 800;
}
.pf-avatar-edit {
  position: absolute; bottom: -2px; right: -2px;
  border: 2px solid var(--lc-surface); border-radius: 999px;
  background: var(--lc-indigo); color: var(--lc-surface);
  font-size: 10px; font-weight: 700; padding: 2px 7px; cursor: pointer;
}
.pf-info { flex: 1; }
.pf-name { font-size: 20px; font-weight: 800; }
.pf-meta { font-size: 12px; color: var(--lc-subtle); margin-top: 4px; }

.pf-row {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 12px; padding: 13px 16px; border-bottom: 1px solid var(--lc-soft);
}
.pf-row.no-border { border-bottom: 0; }
.pf-label { font-size: 13px; color: var(--lc-muted-light); flex: 0 0 72px; }
.pf-value { font-size: 13px; color: var(--lc-text); text-align: right; flex: 1; }
.pf-bio { text-align: left; color: var(--lc-muted); }
.pf-verified { color: var(--lc-emerald); font-weight: 700; }
.pf-invite { display: flex; align-items: center; gap: 8px; justify-content: flex-end; flex-wrap: wrap; }
.pf-copy-btn {
  border: 1px solid var(--lc-blue-border); border-radius: 999px;
  background: var(--lc-surface); color: var(--lc-indigo);
  font-size: 11px; font-weight: 700; padding: 2px 10px; cursor: pointer;
}
.pf-invite em { font-style: normal; font-size: 11px; color: var(--lc-emerald); }
.pf-invite em.err { color: var(--lc-red); }

.sp-primary-btn {
  display: block; width: 100%;
  padding: 14px; border: 0; border-radius: 14px;
  background: linear-gradient(135deg, var(--lc-violet), var(--lc-indigo));
  color: var(--lc-surface); font-size: 15px; font-weight: 800; cursor: pointer;
  box-shadow: 0 8px 20px rgba(79,70,229,0.25);
}

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
  .sp-body { padding-top: 14px; }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
