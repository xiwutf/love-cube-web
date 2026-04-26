<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link to="/admin" class="admin-brand">Love Cube Console</router-link>
      <p class="admin-sub">运营后台</p>

      <section class="admin-sidebar-card">
        <p class="admin-sidebar-card-label">当前模块</p>
        <p class="admin-sidebar-card-value">{{ currentSection }}</p>
        <p class="admin-sidebar-card-meta">{{ todayText }}</p>
      </section>

      <nav class="admin-nav">
        <router-link v-for="item in navItems" :key="item.to" :to="item.to">
          <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <router-link to="/" class="back-home">返回平台官网</router-link>
    </aside>

    <main class="admin-main">
      <header class="admin-main-header">
        <div>
          <p class="admin-main-kicker">Admin Workspace</p>
          <h1 class="admin-main-title">{{ currentSection }}</h1>
        </div>
        <router-link to="/" class="admin-home-link">打开平台官网</router-link>
      </header>

      <section class="admin-main-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { to: '/admin', label: '总览面板', icon: '◉' },
  { to: '/admin/announcements', label: '公告管理', icon: '◎' },
  { to: '/admin/articles', label: '资讯管理', icon: '◈' },
  { to: '/admin/events', label: '活动管理', icon: '◍' },
  { to: '/admin/users', label: '用户管理', icon: '◌' },
  { to: '/admin/invites', label: '邀请记录', icon: '◐' },
  { to: '/admin/verifications', label: '认证审核', icon: '◑' },
  { to: '/admin/reports', label: '举报处理', icon: '◒' },
  { to: '/admin/feedbacks', label: '用户反馈', icon: '◓' }
]

const sectionMap = {
  '/admin': '总览面板',
  '/admin/announcements': '公告管理',
  '/admin/articles': '资讯管理',
  '/admin/events': '活动管理',
  '/admin/users': '用户管理',
  '/admin/invites': '邀请记录',
  '/admin/verifications': '认证审核',
  '/admin/reports': '举报处理',
  '/admin/feedbacks': '用户反馈'
}

const currentSection = computed(() => sectionMap[route.path] || '管理中心')
const todayText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
)
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 276px 1fr;
  background:
    radial-gradient(circle at 100% 0, rgba(255, 89, 125, 0.08), transparent 42%),
    linear-gradient(180deg, #f7f9fd 0%, #f1f5f9 100%);
}

.admin-sidebar {
  padding: 22px 16px;
  border-right: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  position: sticky;
  top: 0;
  height: 100vh;
  display: grid;
  align-content: start;
  gap: 12px;
}

.admin-brand {
  font-size: 20px;
  font-weight: 800;
  text-decoration: none;
  color: #131f35;
  letter-spacing: 0.01em;
}

.admin-sub {
  font-size: 13px;
  color: #66758f;
}

.admin-sidebar-card {
  margin-top: 8px;
  border-radius: 14px;
  padding: 12px;
  border: 1px solid #e9eef7;
  background:
    linear-gradient(150deg, rgba(255, 92, 129, 0.1), rgba(255, 255, 255, 0.98) 45%),
    #fff;
}

.admin-sidebar-card-label {
  margin: 0;
  font-size: 11px;
  color: #9aa6ba;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.admin-sidebar-card-value {
  margin: 8px 0 2px;
  font-size: 18px;
  color: #1a2a44;
  font-weight: 700;
}

.admin-sidebar-card-meta {
  margin: 0;
  font-size: 12px;
  color: #7f8ca3;
}

.admin-nav {
  margin-top: 2px;
  display: grid;
  gap: 6px;
}

.admin-nav a,
.back-home {
  text-decoration: none;
  color: #394963;
  padding: 10px 12px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: transform 0.18s ease, background-color 0.2s ease, color 0.2s ease;
}

.admin-nav a:hover,
.back-home:hover {
  background: #fff5f8;
  transform: translateX(2px);
}

.nav-icon {
  color: #ff5f84;
  font-size: 12px;
}

.admin-nav a.router-link-exact-active {
  background: linear-gradient(135deg, #ffe8ee, #fff4f7);
  color: #ce3f64;
  box-shadow: inset 0 0 0 1px #ffd4df;
}

.back-home {
  margin-top: 10px;
  color: #d9486d;
  background: #fff6f9;
}

.admin-main {
  min-height: 100vh;
  padding: 20px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 14px;
}

.admin-main-header {
  border: 1px solid #e4eaf4;
  border-radius: 16px;
  background:
    radial-gradient(circle at 100% 0, rgba(255, 95, 132, 0.16), transparent 48%),
    #fff;
  padding: 16px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.admin-main-kicker {
  margin: 0;
  color: #8895a9;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.11em;
  font-weight: 700;
}

.admin-main-title {
  margin: 6px 0 0;
  font-size: 28px;
  line-height: 1.15;
  color: #122039;
  letter-spacing: 0.01em;
}

.admin-home-link {
  text-decoration: none;
  color: #d9486d;
  font-weight: 700;
  border: 1px solid #ffd3de;
  background: #fff6f9;
  border-radius: 999px;
  padding: 10px 14px;
}

.admin-main-content {
  min-height: 0;
}

@media (max-width: 1023px) {
  .admin-layout {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    position: sticky;
    top: 0;
    z-index: 50;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid #e2e8f0;
    gap: 10px;
    padding: 12px;
  }

  .admin-sidebar-card {
    display: none;
  }

  .admin-brand {
    font-size: 17px;
  }

  .admin-sub {
    display: none;
  }

  .admin-nav {
    margin-top: 0;
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 6px;
  }

  .admin-nav a,
  .back-home {
    justify-content: center;
    font-size: 12px;
    padding: 8px 6px;
  }

  .nav-icon {
    display: none;
  }

  .admin-main {
    padding: 12px 10px 16px;
    gap: 10px;
  }

  .admin-main-header {
    padding: 12px;
    border-radius: 12px;
  }

  .admin-main-title {
    font-size: 20px;
  }

  .admin-home-link {
    font-size: 12px;
    padding: 8px 10px;
  }
}

@media (max-width: 767px) {
  .admin-nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-main-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
