<template>
  <section class="platform-page me-layout">
    <aside class="platform-card me-sidebar">
      <h2>平台个人中心</h2>
      <nav class="me-menu">
        <router-link to="/me" class="me-menu-item is-active">我的平台首页</router-link>
        <router-link to="/messages" class="me-menu-item">
          平台消息中心
          <span v-if="unreadCount > 0" class="me-dot">{{ unreadCount }}</span>
        </router-link>
        <router-link to="/articles" class="me-menu-item">我的内容</router-link>
        <router-link to="/events" class="me-menu-item">我的活动</router-link>
        <router-link to="/modules" class="me-menu-item">模块入口</router-link>
        <router-link to="/about" class="me-menu-item">平台设置</router-link>
      </nav>
    </aside>

    <main class="me-main">
      <div class="platform-card me-hero">
        <div class="me-user">
          <img v-if="user?.avatar" :src="user.avatar" class="me-avatar" alt="头像" />
          <div v-else class="me-avatar me-avatar-fallback">{{ avatarFallback }}</div>
          <div class="me-meta">
            <h1>{{ user?.username || '平台用户' }}</h1>
            <p>账号ID：{{ user?.id || '--' }}</p>
            <div class="me-tags">
              <span class="me-tag">{{ roleLabel }}</span>
              <span class="me-tag">{{ verifyLabel }}</span>
            </div>
          </div>
        </div>
        <div class="me-kpis">
          <article>
            <p>平台通知</p>
            <strong>{{ unreadCount }}</strong>
          </article>
          <article>
            <p>我的内容</p>
            <strong>{{ myContentCount }}</strong>
          </article>
          <article>
            <p>我的活动</p>
            <strong>{{ myEventCount }}</strong>
          </article>
          <article>
            <p>收藏记录</p>
            <strong>{{ myFavoriteCount }}</strong>
          </article>
        </div>
      </div>

      <div class="me-grid">
        <div class="platform-card">
          <h3>我的模块</h3>
          <div class="me-shortcuts">
            <router-link to="/modules">模块中心</router-link>
            <router-link to="/fellowship">联谊模块入口</router-link>
            <router-link to="/announcements">平台动态</router-link>
            <router-link to="/articles">精选内容</router-link>
          </div>
        </div>

        <div class="platform-card">
          <h3>账号与设置</h3>
          <div class="me-shortcuts">
            <router-link to="/me">平台个人中心</router-link>
            <router-link to="/messages">平台消息中心</router-link>
            <router-link to="/about">登录安全</router-link>
            <router-link to="/about">平台设置</router-link>
          </div>
        </div>
      </div>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { getNotifUnreadCount } from '@/api/notification.js'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const unreadCount = ref(0)

const myContentCount = ref(8)
const myEventCount = ref(2)
const myFavoriteCount = ref(12)

const avatarFallback = computed(() => {
  const name = user.value?.username || ''
  return name.slice(0, 1).toUpperCase() || 'U'
})

const roleLabel = computed(() => {
  const role = String(user.value?.role || '').toLowerCase()
  if (role === 'admin' || role === 'super_admin' || role === 'root') return '平台管理员'
  return '普通用户'
})

const verifyLabel = computed(() => {
  const status = String(user.value?.verificationStatus || 'none')
  if (status === 'approved') return '账号已认证'
  if (status === 'pending') return '认证审核中'
  if (status === 'rejected') return '认证未通过'
  return '账号未认证'
})

onMounted(async () => {
  if (!user.value) {
    await userStore.refreshCurrentUser().catch(() => {})
  }
  const res = await getNotifUnreadCount().catch(() => null)
  unreadCount.value = Number(res?.count ?? res?.unreadCount ?? 0)
})
</script>

<style scoped>
.me-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
}

.me-sidebar {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.me-sidebar h2 {
  margin: 0;
  font-size: 22px;
}

.me-menu {
  margin-top: 14px;
  display: grid;
  gap: 6px;
}

.me-menu-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--lc-muted);
  text-decoration: none;
  font-weight: 600;
}

.me-menu-item.is-active,
.me-menu-item:hover {
  background: #feeef3;
  color: #e11d48;
}

.me-dot {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.me-main {
  display: grid;
  gap: 14px;
}

.me-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
  background: linear-gradient(100deg, #f8a2ce, #5a8ff2);
}

.me-user {
  display: flex;
  gap: 12px;
  align-items: center;
}

.me-avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
}

.me-avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  font-size: 28px;
  font-weight: 800;
}

.me-meta h1 {
  margin: 0;
  color: #fff;
}

.me-meta p {
  margin: 6px 0;
  color: rgba(255, 255, 255, 0.88);
}

.me-tags {
  display: flex;
  gap: 8px;
}

.me-tag {
  padding: 3px 10px;
  border-radius: 999px;
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  font-size: 12px;
}

.me-kpis {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.me-kpis article {
  border-radius: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.92);
}

.me-kpis p {
  margin: 0;
  color: var(--lc-muted);
  font-size: 12px;
}

.me-kpis strong {
  font-size: 26px;
}

.me-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.me-shortcuts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.me-shortcuts a {
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 10px;
  color: var(--lc-text);
  text-decoration: none;
  text-align: center;
}

@media (max-width: 1023px) {
  .me-layout {
    grid-template-columns: 1fr;
  }

  .me-sidebar {
    position: static;
  }

  .me-hero,
  .me-grid {
    grid-template-columns: 1fr;
  }
}
</style>
