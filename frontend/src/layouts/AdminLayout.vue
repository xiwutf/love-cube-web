<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <router-link to="/admin" class="admin-brand">
        <img :src="loveCubeIcon" alt="">
        <span>Love Cube Console</span>
      </router-link>
      <p class="admin-sub">运营后台</p>

      <section class="admin-sidebar-card">
        <p class="admin-sidebar-card-label">当前页面</p>
        <p class="admin-sidebar-card-value">{{ currentSection }}</p>
        <p class="admin-sidebar-card-meta">{{ todayText }}</p>
      </section>

      <div class="admin-sidebar-guide">
        <p class="admin-sidebar-guide-title">怎么用</p>
        <p class="admin-sidebar-guide-text">
          左侧菜单与总览里的「功能地图」一致。点进模块即可办事；子页用顶栏「返回」。
        </p>
      </div>

      <nav class="admin-nav" aria-label="后台功能导航">
        <div v-for="entry in visibleNavEntries" :key="entry.key" class="admin-nav-row">
          <p v-if="entry.kind === 'section'" class="admin-nav-section">{{ entry.label }}</p>
          <router-link
            v-else
            v-slot="{ isActive, isExactActive, href, navigate }"
            :to="entry.to"
            custom
          >
            <a
              :href="href"
              class="admin-nav-link"
              :class="{ 'is-active': entry.to === '/admin' ? isExactActive : isActive }"
              @click="navigate"
            >
              <span class="nav-icon" aria-hidden="true">{{ entry.icon }}</span>
              <span>{{ entry.label }}</span>
            </a>
          </router-link>
        </div>
      </nav>

      <router-link to="/" class="back-home">返回平台官网</router-link>
    </aside>

    <main class="admin-main">
      <header class="admin-main-header">
        <div class="admin-main-heading">
          <p class="admin-main-kicker">管理后台</p>
          <h1 class="admin-main-title">{{ currentSection }}</h1>
          <p v-if="pageSubtitle" class="admin-main-subtitle">{{ pageSubtitle }}</p>
        </div>
        <div class="admin-main-actions">
          <RouteBackButton v-if="showRouteBackButton" class="admin-route-back" />
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
import loveCubeIcon from '@/assets/brand/love-cube-icon.svg'
import { useUserStore } from '@/stores/user.js'
import {
  ADMIN_NAV_GROUPS,
  filterAdminNavGroups,
  getAdminPageSubtitle,
  resolveAdminSectionTitle
} from '@/constants/adminNavigation.js'

const route = useRoute()
const userStore = useUserStore()

const visibleNavGroups = computed(() =>
  filterAdminNavGroups(ADMIN_NAV_GROUPS, userStore.hasPermission)
)

/** 扁平列表：便于单 v-for + 合法 key，避免 template 片段与 key 规则冲突 */
const visibleNavEntries = computed(() => {
  const rows = []
  for (const group of visibleNavGroups.value) {
    rows.push({ kind: 'section', key: `s-${group.id}`, label: group.label })
    for (const item of group.items) {
      rows.push({ kind: 'link', key: item.to, ...item })
    }
  }
  return rows
})

const visibleNavItems = computed(() => visibleNavGroups.value.flatMap(g => g.items))

const navHomePaths = computed(() => new Set(visibleNavItems.value.map(item => item.to)))
const showRouteBackButton = computed(() => !navHomePaths.value.has(route.path))
const currentSection = computed(() => resolveAdminSectionTitle(route.path))
const pageSubtitle = computed(() => getAdminPageSubtitle(route.path))
const todayText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
)
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  background: var(--lc-soft);
}

.admin-sidebar {
  padding: 16px 12px 12px;
  border-right: 1px solid var(--lc-border);
  background: var(--lc-surface);
  position: sticky;
  top: 0;
  height: 100vh;
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr) auto;
  align-content: stretch;
  gap: 10px;
  overflow: hidden;
}

.admin-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 800;
  text-decoration: none;
  color: var(--lc-text);
  letter-spacing: .01em;
}

.admin-brand img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: block;
}

.admin-sub {
  font-size: 12px;
  color: var(--lc-subtle);
  font-weight: 600;
}

.admin-sidebar-card {
  margin-top: 2px;
  border-radius: 12px;
  padding: 10px 12px;
  border: 1px solid var(--lc-border);
  background: var(--lc-bg);
}

.admin-sidebar-card-label {
  margin: 0;
  font-size: 11px;
  color: var(--lc-subtle);
  text-transform: uppercase;
  letter-spacing: .08em;
  font-weight: 700;
}

.admin-sidebar-card-value {
  margin: 8px 0 2px;
  font-size: 17px;
  color: var(--lc-text);
  font-weight: 700;
}

.admin-sidebar-card-meta {
  margin: 0;
  font-size: 11px;
  color: var(--lc-subtle);
}

.admin-sidebar-guide {
  border-radius: 10px;
  padding: 10px 11px;
  border: 1px dashed var(--lc-blue-border);
  background: var(--lc-blue-light);
}

.admin-sidebar-guide-title {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 800;
  color: var(--lc-blue-dark);
  letter-spacing: 0.04em;
}

.admin-sidebar-guide-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--lc-muted);
}

.admin-nav-section {
  margin: 0;
  padding: 12px 10px 4px;
  font-size: 11px;
  font-weight: 700;
  color: var(--lc-subtle);
  letter-spacing: .06em;
}

.admin-nav-section:first-of-type {
  padding-top: 4px;
}

.admin-nav {
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
  display: grid;
  gap: 2px;
  align-content: start;
}

.admin-nav-row {
  display: contents;
}

.admin-nav::-webkit-scrollbar {
  width: 6px;
}

.admin-nav::-webkit-scrollbar-thumb {
  background: var(--lc-border);
  border-radius: 999px;
}

.admin-nav::-webkit-scrollbar-track {
  background: transparent;
}

.admin-nav-link {
  text-decoration: none;
  color: var(--lc-muted);
  padding: 8px 10px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background .18s ease, color .18s ease, box-shadow .18s ease;
}

.admin-nav-link:hover {
  background: var(--lc-soft);
  color: var(--lc-text);
}

.nav-icon {
  color: var(--lc-subtle);
  font-size: 12px;
  flex-shrink: 0;
}

.admin-nav-link.is-active {
  background: var(--lc-blue-light);
  color: var(--lc-blue-mid);
  font-weight: 700;
  box-shadow: inset 0 0 0 1px var(--lc-blue-border);
}

.admin-nav-link.is-active .nav-icon {
  color: var(--lc-blue);
}

.back-home {
  margin-top: 6px;
  text-decoration: none;
  color: var(--lc-blue);
  border: 1px solid var(--lc-blue-border);
  background: var(--lc-blue-light);
  border-radius: 12px;
  min-height: 44px;
  justify-content: center;
  font-weight: 700;
  padding: 8px 10px;
  font-size: 13px;
  display: flex;
  align-items: center;
  transition: background .18s ease, border-color .18s ease, color .18s ease;
}

.back-home:hover {
  background: #dbeafe;
  border-color: #93c5fd;
  color: var(--lc-blue-dark);
}

.admin-main {
  min-height: 100vh;
  padding: 20px 24px 28px;
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 16px;
  min-width: 0;
}

.admin-main-header {
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  background: var(--lc-surface);
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  box-shadow: var(--lc-shadow-sm);
}

.admin-main-kicker {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: .12em;
  font-weight: 700;
}

.admin-main-heading {
  min-width: 0;
}

.admin-main-title {
  margin: 6px 0 0;
  font-size: 26px;
  line-height: 1.15;
  color: var(--lc-text);
  font-weight: 800;
}

.admin-main-subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.55;
  color: var(--lc-muted);
  font-weight: 500;
  max-width: 40rem;
}

.admin-home-link {
  text-decoration: none;
  color: var(--lc-blue);
  font-weight: 700;
  font-size: 13px;
  border: 1px solid var(--lc-blue-border);
  background: var(--lc-blue-light);
  border-radius: 8px;
  padding: 8px 14px;
  transition: background .18s ease, border-color .18s ease, color .18s ease;
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
  border-color: var(--lc-blue-border);
  color: var(--lc-blue);
}

.admin-home-link:hover {
  background: #dbeafe;
  border-color: #93c5fd;
  color: var(--lc-blue-dark);
}

.admin-main-content {
  min-height: 0;
  min-width: 0;
  max-width: min(1480px, 100%);
  width: 100%;
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
    border-bottom: 1px solid var(--lc-border);
    grid-template-rows: auto auto minmax(0, 1fr) auto;
    gap: 6px;
    padding: 10px 12px;
    overflow: visible;
  }

  .admin-sidebar-card {
    display: none;
  }

  .admin-sidebar-guide {
    display: none;
  }

  .admin-brand {
    font-size: 15px;
  }

  .admin-sub {
    display: none;
  }

  .admin-nav {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 4px;
    overflow: visible;
    padding-right: 0;
  }

  .admin-nav-section {
    grid-column: 1 / -1;
    padding: 10px 10px 4px;
  }

  .admin-nav-link {
    justify-content: center;
    font-size: 12px;
    padding: 7px 6px;
  }

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
