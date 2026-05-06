<template>
  <div class="admin-layout">
    <header class="admin-mobile-bar">
      <router-link to="/admin" class="admin-mobile-brand" @click="closeNavDrawer">
        <img :src="loveCubeIcon" alt="">
        <span class="admin-mobile-brand-text">Love Cube</span>
      </router-link>
      <p class="admin-mobile-section u-truncate">{{ currentSection }}</p>
      <button
        id="admin-nav-drawer-trigger"
        type="button"
        class="admin-menu-toggle"
        :aria-expanded="navDrawerOpen"
        aria-controls="admin-nav-drawer"
        aria-label="打开或关闭后台菜单"
        @click="toggleNavDrawer"
      >
        <span class="admin-menu-burger" aria-hidden="true" />
      </button>
    </header>

    <div
      v-show="navDrawerOpen"
      class="admin-drawer-backdrop"
      aria-hidden="true"
      @click="closeNavDrawer"
    />

    <aside id="admin-nav-drawer" class="admin-sidebar" :class="{ 'is-drawer-open': navDrawerOpen }">
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
              @click="
                (e) => {
                  navigate(e)
                  closeNavDrawer()
                }
              "
            >
              <span class="nav-icon" aria-hidden="true">{{ entry.icon }}</span>
              <span>{{ entry.label }}</span>
            </a>
          </router-link>
        </div>
      </nav>

      <router-link to="/" class="back-home" @click="closeNavDrawer">返回平台官网</router-link>
    </aside>

    <main class="admin-main">
      <div v-if="showRouteBackButton" class="admin-main-back-strip">
        <RouteBackButton class="admin-route-back" />
      </div>

      <section class="admin-main-content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import RouteBackButton from '@/components/RouteBackButton.vue'
import loveCubeIcon from '@/assets/brand/love-cube-icon.svg'
import { useUserStore } from '@/stores/user.js'
import { ADMIN_NAV_GROUPS, filterAdminNavGroups, resolveAdminSectionTitle } from '@/constants/adminNavigation.js'

const route = useRoute()
const userStore = useUserStore()

const navDrawerOpen = ref(false)

function closeNavDrawer() {
  navDrawerOpen.value = false
}

function toggleNavDrawer() {
  navDrawerOpen.value = !navDrawerOpen.value
}

watch(navDrawerOpen, (open) => {
  if (typeof document === 'undefined') return
  document.body.style.overflow = open ? 'hidden' : ''
})

watch(
  () => route.fullPath,
  () => {
    closeNavDrawer()
  }
)

function onGlobalKeydown(e) {
  if (e.key === 'Escape') {
    closeNavDrawer()
  }
}

onMounted(() => {
  if (typeof document !== 'undefined') {
    document.addEventListener('keydown', onGlobalKeydown)
  }
})

onUnmounted(() => {
  if (typeof document !== 'undefined') {
    document.removeEventListener('keydown', onGlobalKeydown)
    document.body.style.overflow = ''
  }
})

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
const todayText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
)
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 288px minmax(0, 1fr);
  background: var(--lc-soft);
}

.admin-sidebar {
  padding: 14px 10px 10px;
  border-right: 1px solid var(--lc-border);
  background: var(--lc-surface);
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
  overflow: hidden auto;
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
  position: relative;
  z-index: 1;
  isolation: isolate;
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
  flex: 1 1 auto;
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
  padding: 9px 10px;
  border-radius: 9px;
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
  margin-top: 4px;
  text-decoration: none;
  color: var(--lc-blue);
  border: 1px solid var(--lc-blue-border);
  background: var(--lc-blue-light);
  border-radius: 10px;
  min-height: 40px;
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
  padding: 16px 18px 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.admin-main-back-strip {
  flex-shrink: 0;
}

.admin-route-back {
  box-shadow: none;
}

.admin-route-back:hover {
  border-color: var(--lc-blue-border);
  color: var(--lc-blue);
}

.admin-main-content {
  flex: 1;
  min-height: 0;
  min-width: 0;
  max-width: 100%;
  width: 100%;
}

.admin-mobile-bar,
.admin-drawer-backdrop {
  display: none;
}

@media (max-width: 1023px) {
  .admin-layout {
    display: block;
    grid-template-columns: unset;
  }

  .admin-mobile-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    padding-top: max(10px, env(safe-area-inset-top, 0px));
    border-bottom: 1px solid var(--lc-border);
    background: var(--lc-surface);
    position: sticky;
    top: 0;
    z-index: 80;
  }

  .admin-mobile-brand {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    text-decoration: none;
    color: var(--lc-text);
  }

  .admin-mobile-brand img {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: block;
  }

  .admin-mobile-brand-text {
    font-size: 15px;
    font-weight: 800;
    letter-spacing: 0.01em;
  }

  .admin-mobile-section {
    margin: 0;
    flex: 1;
    min-width: 0;
    text-align: center;
    font-size: 13px;
    font-weight: 700;
    color: var(--lc-text);
  }

  .admin-menu-toggle {
    flex-shrink: 0;
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid var(--lc-border);
    border-radius: 10px;
    background: var(--lc-bg);
    cursor: pointer;
    padding: 0;
    transition: background 0.18s ease, border-color 0.18s ease;
  }

  .admin-menu-toggle:hover {
    background: var(--lc-soft);
    border-color: var(--lc-blue-border);
  }

  .admin-menu-burger {
    display: block;
    width: 20px;
    height: 2px;
    border-radius: 1px;
    background: var(--lc-text);
    box-shadow:
      0 -6px 0 var(--lc-text),
      0 6px 0 var(--lc-text);
  }

  .admin-drawer-backdrop {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 90;
    background: color-mix(in srgb, var(--lc-text) 38%, transparent);
    border: 0;
    padding: 0;
    margin: 0;
  }

  .admin-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    bottom: 0;
    width: min(304px, 88vw);
    max-width: 100%;
    height: 100vh;
    height: 100dvh;
    z-index: 100;
    transform: translateX(-100%);
    transition: transform 0.22s ease, box-shadow 0.22s ease;
    pointer-events: none;
    border-right: 1px solid var(--lc-border);
    border-bottom: none;
    padding: 14px 10px 10px;
    padding-top: max(14px, env(safe-area-inset-top, 0px));
    gap: 8px;
    overflow: hidden auto;
    box-shadow: none;
  }

  .admin-sidebar.is-drawer-open {
    transform: translateX(0);
    pointer-events: auto;
    box-shadow: 8px 0 24px color-mix(in srgb, var(--lc-text) 12%, transparent);
  }

  .admin-sidebar-card,
  .admin-sidebar-guide {
    display: none;
  }

  .admin-nav {
    display: grid;
    grid-template-columns: 1fr;
    gap: 2px;
    overflow-y: auto;
    padding-right: 2px;
  }

  .admin-nav-section {
    grid-column: auto;
    padding: 12px 10px 4px;
  }

  .admin-nav-link {
    justify-content: flex-start;
    font-size: 13px;
    padding: 9px 10px;
  }

  .back-home {
    font-size: 13px;
    padding: 8px 10px;
  }

  .admin-main {
    min-height: 0;
    padding: 12px 14px max(20px, env(safe-area-inset-bottom, 0px));
    gap: 12px;
  }

}
</style>
