<template>
  <div class="landing-page">
    <div class="content">
      <p class="eyebrow">Love Cube Fellowship</p>
      <h1 class="title">更可靠的联谊交友体验</h1>
      <p class="slogan">{{ sloganText }}</p>
      <div class="actions">
        <button
          v-if="isLoggedIn && !isFellowshipEnabled"
          type="button"
          class="btn-primary"
          :disabled="activating"
          @click="handleActivate"
        >
          {{ activating ? '开通中...' : '立即开通联谊模块' }}
        </button>
        <router-link v-else to="/fellowship/discover" class="btn-primary">立即开始匹配</router-link>
        <router-link v-if="isLoggedIn && isFellowshipEnabled" to="/fellowship/profile/edit" class="btn-ghost">完善我的资料</router-link>
        <router-link v-else to="/fellowship/login" class="btn-ghost">登录后开通</router-link>
      </div>
      <p class="hint">{{ hintText }}</p>

      <div class="quick-grid">
        <router-link v-for="item in quickItems" :key="item.label" :to="item.to" class="quick-item">{{ item.label }}</router-link>
      </div>
    </div>
    <div class="footer-link">
      <router-link to="/">返回平台首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activating = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isFellowshipEnabled = computed(() => userStore.isFellowshipEnabled)

const sloganText = computed(() => {
  if (isLoggedIn.value && !isFellowshipEnabled.value) {
    return '联谊模块默认关闭，需你本人手动开通后才可进入'
  }
  return '从完善资料到开始匹配，3 分钟即可进入互动状态'
})

const hintText = computed(() => {
  if (isLoggedIn.value && !isFellowshipEnabled.value) {
    return '你的平台账号不受影响，开通后才会启用联谊能力'
  }
  return '新用户建议先完善资料并完成真人认证，推荐效果会更好'
})

const quickItems = computed(() => {
  if (isLoggedIn.value && !isFellowshipEnabled.value) {
    return [
      { label: '联谊是什么', to: '/fellowship-intro' },
      { label: '平台首页', to: '/' },
      { label: '内容广场', to: '/articles' },
      { label: '活动中心', to: '/events' }
    ]
  }
  return [
    { label: '推荐对象', to: '/fellowship/discover' },
    { label: '打招呼消息', to: '/fellowship/messages?tab=interact' },
    { label: '谁看过我', to: '/fellowship/messages?tab=visitor' },
    { label: '我的喜欢', to: '/fellowship/my-likes' },
    { label: '择偶条件', to: '/fellowship/preferences' },
    { label: '真人/实名认证', to: '/fellowship/verify' },
    { label: '黑名单', to: '/fellowship/blacklist' },
    { label: '生活照管理', to: '/fellowship/profile/edit' }
  ]
})

async function handleActivate() {
  activating.value = true
  try {
    await userStore.activateFellowship()
    showToast({ type: 'success', message: '联谊模块已开通' })
    const target = typeof route.query.redirect === 'string' ? decodeURIComponent(route.query.redirect) : '/fellowship/discover'
    router.replace(target || '/fellowship/discover')
  } catch (error) {
    showToast({ type: 'fail', message: error?.message || '开通失败，请稍后重试' })
  } finally {
    activating.value = false
  }
}

if (isLoggedIn.value && isFellowshipEnabled.value) {
  const target = typeof route.query.redirect === 'string' ? decodeURIComponent(route.query.redirect) : '/fellowship/discover'
  router.replace(target || '/fellowship/discover')
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #fff0f3 0%, #ffffff 60%);
  padding: calc(24px + env(safe-area-inset-top)) 20px calc(24px + env(safe-area-inset-bottom));
  position: relative;
}

.content {
  width: min(100%, 520px);
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #e84f73;
  text-transform: uppercase;
}

.title {
  margin: 14px 0 10px;
  font-size: clamp(32px, 8vw, 44px);
  line-height: 1.2;
  font-weight: 900;
  color: #1f2a44;
}

.slogan {
  font-size: 15px;
  color: #64748b;
  margin: 0;
  line-height: 1.7;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 28px;
}

.btn-primary,
.btn-ghost {
  min-height: 46px;
  padding: 0 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: #ff5f84;
  color: #fff;
  box-shadow: 0 10px 20px rgba(255, 95, 132, 0.25);
}

.btn-ghost {
  border: 1px solid #ffc6d4;
  color: #d94870;
  background: #fff;
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.hint {
  margin: 16px 0 0;
  font-size: 13px;
  color: #64748b;
}

.quick-grid {
  width: 100%;
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-item {
  min-height: 40px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #ffd5df;
  background: #fff;
  color: #d94870;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  line-height: 1.35;
  word-break: keep-all;
}

.footer-link {
  position: absolute;
  bottom: 24px;
  font-size: 13px;
}

.footer-link a {
  color: #94a3b8;
  text-decoration: none;
}

@media (max-width: 767px) {
  .landing-page {
    justify-content: flex-start;
    padding: calc(16px + env(safe-area-inset-top)) 16px calc(20px + env(safe-area-inset-bottom));
  }

  .content {
    width: 100%;
    justify-content: flex-start;
    padding-bottom: 12px;
  }

  .actions {
    width: 100%;
    flex-direction: column;
  }

  .btn-primary,
  .btn-ghost {
    width: 100%;
  }

  .footer-link {
    position: static;
    margin-top: 10px;
  }
}

@media (max-width: 375px) {
  .landing-page {
    padding: calc(12px + env(safe-area-inset-top)) 12px calc(16px + env(safe-area-inset-bottom));
  }

  .title {
    margin-top: 10px;
    font-size: 28px;
    line-height: 1.25;
  }

  .slogan {
    font-size: 14px;
    line-height: 1.55;
  }

  .actions {
    margin-top: 16px;
    gap: 8px;
  }

  .btn-primary,
  .btn-ghost {
    min-height: 42px;
    font-size: 14px;
  }

  .quick-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .quick-item {
    min-height: 38px;
    font-size: 12px;
  }
}

@media (max-width: 360px) {
  .landing-page {
    padding: calc(10px + env(safe-area-inset-top)) 10px calc(12px + env(safe-area-inset-bottom));
  }

  .eyebrow {
    font-size: 11px;
    letter-spacing: 0.08em;
  }

  .title {
    margin: 8px 0;
    font-size: 24px;
    line-height: 1.3;
  }

  .hint {
    margin-top: 12px;
    font-size: 12px;
  }

  .btn-primary,
  .btn-ghost {
    min-height: 40px;
    font-size: 13px;
  }
}
</style>

