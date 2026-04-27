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
        <div class="admin-main-actions">
          <RouteBackButton v-if="route.path !== '/admin'" class="admin-route-back" />
          <router-link to="/" class="admin-home-link">打开平台官网</router-link>
        </div>
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
import RouteBackButton from '@/components/RouteBackButton.vue'

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
  { to: '/admin/feedbacks', label: '用户反馈', icon: '◓' },
  { to: '/admin/modules', label: '模块管理', icon: '◇' },
  { to: '/admin/home-config', label: '首页配置', icon: '◆' }
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
  '/admin/feedbacks': '用户反馈',
  '/admin/modules': '模块管理',
  '/admin/home-config': '首页配置'
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
  grid-template-columns: 264px 1fr;
  background: #f1f5f9;
}

.admin-sidebar {
  padding: 20px 14px;
  border-right: 1px solid #e2e8f0;
  background: #fff;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  display: grid;
  align-content: start;
  gap: 10px;
}

.admin-brand {
  font-size: 18px;
  font-weight: 800;
  text-decoration: none;
  color: #0f172a;
  letter-spacing: .01em;
}

.admin-sub {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
}

.admin-sidebar-card {
  margin-top: 4px;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.admin-sidebar-card-label {
  margin: 0;
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: .08em;
  font-weight: 700;
}

.admin-sidebar-card-value {
  margin: 8px 0 2px;
  font-size: 17px;
  color: #0f172a;
  font-weight: 700;
}

.admin-sidebar-card-meta {
  margin: 0;
  font-size: 11px;
  color: #94a3b8;
}

.admin-nav {
  margin-top: 4px;
  display: grid;
  gap: 3px;
}

.admin-nav a,
.back-home {
  text-decoration: none;
  color: #475569;
  padding: 9px 12px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all .18s ease;
}

.admin-nav a:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.nav-icon {
  color: #94a3b8;
  font-size: 12px;
  flex-shrink: 0;
}

.admin-nav a.router-link-exact-active {
  background: #eff6ff;
  color: #1d4ed8;
  font-weight: 700;
  box-shadow: inset 0 0 0 1px #bfdbfe;
}

.admin-nav a.router-link-exact-active .nav-icon {
  color: #2563eb;
}

.back-home {
  margin-top: 12px;
  color: #2563eb;
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  border-radius: 10px;
  justify-content: center;
  font-weight: 700;
}

.back-home:hover {
  background: #dbeafe;
  border-color: #93c5fd;
  color: #1e3a8a;
}

.admin-main {
  min-height: 100vh;
  padding: 20px 24px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 16px;
}

.admin-main-header {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: #fff;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(15,23,42,.04);
}

.admin-main-kicker {
  margin: 0;
  color: #94a3b8;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: .12em;
  font-weight: 700;
}

.admin-main-title {
  margin: 6px 0 0;
  font-size: 26px;
  line-height: 1.15;
  color: #0f172a;
  font-weight: 800;
}

.admin-home-link {
  text-decoration: none;
  color: #2563eb;
  font-weight: 700;
  font-size: 13px;
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  border-radius: 8px;
  padding: 8px 14px;
  transition: all .18s ease;
}

.admin-main-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-route-back {
  box-shadow: none;
}

.admin-route-back:hover {
  border-color: #bfdbfe;
  color: #2563eb;
}

.admin-home-link:hover {
  background: #dbeafe;
  border-color: #93c5fd;
  color: #1e3a8a;
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
    gap: 8px;
    padding: 10px 12px;
  }

  .admin-sidebar-card {
    display: none;
  }

  .admin-brand {
    font-size: 16px;
  }

  .admin-sub {
    display: none;
  }

  .admin-nav {
    margin-top: 0;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 4px;
  }

  .admin-nav a,
  .back-home {
    justify-content: center;
    font-size: 12px;
    padding: 7px 6px;
  }

  .nav-icon {
    display: none;
  }

  .admin-main {
    padding: 12px 14px 20px;
    gap: 12px;
  }

  .admin-main-header {
    padding: 12px 14px;
    border-radius: 12px;
  }

  .admin-main-title {
    font-size: 20px;
  }

  .admin-home-link {
    font-size: 12px;
    padding: 7px 10px;
  }
}

@media (max-width: 767px) {
  .admin-nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-main-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .admin-main-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
