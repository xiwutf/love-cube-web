<template>
  <section class="social-page">
    <header class="social-head">
      <button type="button" class="social-back" aria-label="返回" @click="goBack('/me')">‹</button>
      <h1 class="social-title">{{ title }}</h1>
      <p v-if="subtitle" class="social-sub">{{ subtitle }}</p>
    </header>

    <div v-if="lockedBanner" class="social-lock">
      <p>{{ lockedBanner.title }}</p>
      <router-link to="/fellowship/vip">{{ lockedBanner.action }}</router-link>
    </div>

    <p v-if="loading" class="social-status">加载中...</p>
    <p v-else-if="error" class="social-status is-error">{{ error }}</p>
    <p v-else-if="!displayList.length" class="social-status">{{ emptyText }}</p>

    <ul v-else class="social-list">
      <li v-for="user in displayList" :key="user.userId || user.createdAt" class="social-item">
        <button type="button" class="social-user" @click="openProfile(user)">
          <span class="social-avatar" aria-hidden="true">
            <img v-if="user.avatar" :src="user.avatar" alt="">
            <span v-else class="social-avatar-fb">{{ avatarInitial(user) }}</span>
          </span>
          <span class="social-copy">
            <strong>{{ user.nickname || '平台用户' }}</strong>
            <small>{{ userMeta(user) }}</small>
          </span>
        </button>
        <button
          v-if="resolveAction(user)"
          type="button"
          class="social-action"
          :disabled="actionBusyId === user.userId"
          @click="$emit('action', user)"
        >
          {{ actionBusyId === user.userId ? '处理中' : resolveAction(user) }}
        </button>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useBackNavigation } from '@/composables/useBackNavigation.js'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  emptyText: { type: String, default: '暂无数据' },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  users: { type: Array, default: () => [] },
  locked: { type: Boolean, default: false },
  lockedCount: { type: Number, default: 0 },
  actionLabel: { type: String, default: '' },
  actionResolver: { type: Function, default: null },
  actionBusyId: { type: [String, Number], default: '' }
})

defineEmits(['action'])

const router = useRouter()
const { goBack } = useBackNavigation('/me')

const displayList = computed(() => (Array.isArray(props.users) ? props.users : []))

function resolveAction(user) {
  if (!user?.userId || user.locked) return ''
  if (typeof props.actionResolver === 'function') {
    return props.actionResolver(user) || ''
  }
  return props.actionLabel || ''
}

const lockedBanner = computed(() => {
  if (!props.locked) return null
  return {
    title: `有 ${props.lockedCount} 人向你表达了喜欢，开通 VIP 可查看完整名单`,
    action: '了解 VIP 权益'
  }
})

function avatarInitial(user) {
  return String(user?.nickname || '?').trim().charAt(0) || '?'
}

function userMeta(user) {
  const parts = []
  if (user?.age) parts.push(`${user.age}岁`)
  if (user?.location) parts.push(user.location)
  if (user?.occupation) parts.push(user.occupation)
  return parts.length ? parts.join(' · ') : '暂无更多资料'
}

function openProfile(user) {
  if (user?.locked || !user?.userId) {
    router.push('/fellowship/vip')
    return
  }
  router.push(`/fellowship/user-profile/${user.userId}`)
}
</script>

<style scoped>
.social-page {
  min-height: 100vh;
  padding-bottom: calc(72px + env(safe-area-inset-bottom, 0px));
  background: var(--lc-bg);
  color: var(--lc-text);
}

.social-head {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: calc(8px + env(safe-area-inset-top, 0px)) 16px 12px 8px;
  background: var(--lc-surface);
  border-bottom: 1px solid var(--lc-soft-alt);
}

.social-back {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--lc-indigo);
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
}

.social-title {
  margin: 0 0 0 8px;
  font-size: 18px;
  font-weight: 800;
}

.social-sub {
  margin: 4px 0 0 8px;
  font-size: 12px;
  color: var(--lc-subtle);
  line-height: 1.45;
}

.social-lock {
  margin: 12px 14px 0;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border: 1px solid #fdba74;
}

.social-lock p {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #9a3412;
  line-height: 1.45;
}

.social-lock a {
  display: inline-block;
  margin-top: 6px;
  font-size: 12px;
  color: #c2410c;
  text-decoration: none;
  font-weight: 700;
}

.social-status {
  margin: 24px 16px;
  text-align: center;
  font-size: 14px;
  color: var(--lc-subtle);
}

.social-status.is-error {
  color: var(--lc-red);
}

.social-list {
  list-style: none;
  margin: 10px 0 0;
  padding: 0 14px;
  display: grid;
  gap: 10px;
}

.social-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-soft-alt);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.social-user {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0;
  border: none;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.social-avatar {
  flex: 0 0 auto;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--lc-soft);
}

.social-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.social-avatar-fb {
  display: grid;
  place-items: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-violet));
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}

.social-copy {
  min-width: 0;
  display: grid;
  gap: 3px;
}

.social-copy strong {
  font-size: 15px;
  font-weight: 700;
  color: var(--lc-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.social-copy small {
  font-size: 12px;
  color: var(--lc-subtle);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.social-action {
  flex: 0 0 auto;
  border: 1px solid var(--lc-blue-border);
  border-radius: 999px;
  padding: 6px 12px;
  background: var(--lc-blue-light);
  color: var(--lc-blue);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.social-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
