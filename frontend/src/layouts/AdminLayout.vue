<template>
  <div class="admin-layout">
    <header class="admin-mobile-bar">
      <router-link to="/admin" class="admin-mobile-brand" @click="closeNavDrawer">
        <img :src="loveCubeIcon" alt="">
        <span class="admin-mobile-brand-text">LoveCube</span>
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
      <router-link to="/admin" class="admin-brand" @click="closeNavDrawer">
        <img :src="loveCubeIcon" alt="">
        <span class="admin-brand-copy">
          <strong>LoveCube</strong>
          <small>后台管理系统</small>
        </span>
      </router-link>

      <nav class="admin-nav" aria-label="后台功能导航">
        <template v-for="group in visibleNavGroups" :key="group.id">
          <router-link
            v-if="group.items.length === 1"
            v-slot="{ isActive, isExactActive, href, navigate }"
            :to="group.items[0].to"
            custom
          >
            <a
              :href="href"
              class="admin-nav-link admin-nav-link--top"
              :class="{ 'is-active': group.items[0].to === '/admin' ? isExactActive : isActive }"
              @click="
                (e) => {
                  navigate(e)
                  closeNavDrawer()
                }
              "
            >
              <span class="nav-icon" aria-hidden="true">{{ group.icon }}</span>
              <span class="nav-text">{{ group.label }}</span>
              <span class="nav-chevron" aria-hidden="true">›</span>
            </a>
          </router-link>

          <details
            v-else-if="group.items.length > 1"
            class="admin-nav-group"
            :open="isGroupActive(group)"
          >
            <summary
              class="admin-nav-link admin-nav-link--top"
              :class="{ 'is-active': isGroupActive(group) }"
            >
              <span class="nav-icon" aria-hidden="true">{{ group.icon }}</span>
              <span class="nav-text">{{ group.label }}</span>
              <span class="nav-chevron" aria-hidden="true">›</span>
            </summary>
            <div class="admin-subnav">
              <router-link
                v-for="item in group.items"
                :key="item.to"
                v-slot="{ isActive, isExactActive, href, navigate }"
                :to="item.to"
                custom
              >
                <a
                  :href="href"
                  class="admin-subnav-link"
                  :class="{ 'is-active': item.to === '/admin' ? isExactActive : isActive }"
                  @click="
                    (e) => {
                      navigate(e)
                      closeNavDrawer()
                    }
                  "
                >
                  <span class="subnav-dot" aria-hidden="true">{{ item.icon || group.icon }}</span>
                  <span>{{ item.label }}</span>
                </a>
              </router-link>
            </div>
          </details>

          <span v-else class="admin-nav-link admin-nav-link--top is-disabled">
            <span class="nav-icon" aria-hidden="true">{{ group.icon }}</span>
            <span class="nav-text">{{ group.label }}</span>
            <span class="nav-chevron" aria-hidden="true">›</span>
          </span>
        </template>
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

const visibleNavItems = computed(() => visibleNavGroups.value.flatMap(group => group.items))
const navHomePaths = computed(() => new Set(visibleNavItems.value.map(item => item.to)))
const showRouteBackButton = computed(() => !navHomePaths.value.has(route.path))
const currentSection = computed(() => resolveAdminSectionTitle(route.path))

function isGroupActive(group) {
  return group.items.some((item) => {
    if (item.to === '/admin') return route.path === '/admin'
    return route.path === item.to || route.path.startsWith(`${item.to}/`)
  })
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 248px minmax(0, 1fr);
  background: var(--lc-soft);
}

.admin-sidebar {
  --sidebar-bg: color-mix(in srgb, var(--lc-text) 88%, var(--lc-blue) 12%);
  --sidebar-bg-deep: color-mix(in srgb, var(--lc-text) 94%, var(--lc-blue) 6%);
  --sidebar-line: color-mix(in srgb, var(--lc-surface) 14%, transparent);
  --sidebar-text: color-mix(in srgb, var(--lc-surface) 88%, var(--lc-soft) 12%);
  --sidebar-muted: color-mix(in srgb, var(--lc-surface) 60%, transparent);
  --sidebar-hover: color-mix(in srgb, var(--lc-surface) 10%, transparent);
  --sidebar-active: color-mix(in srgb, var(--lc-rose) 56%, transparent);
  padding: 20px 12px 14px;
  border-right: 1px solid var(--sidebar-line);
  background:
    linear-gradient(180deg, var(--sidebar-bg) 0%, var(--sidebar-bg-deep) 100%);
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 22px;
  overflow: hidden auto;
}

.admin-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0 8px;
  text-decoration: none;
  color: var(--sidebar-text);
}

.admin-brand img {
  width: 38px;
  height: 38px;
  border-radius: 9px;
  display: block;
  box-shadow: 0 10px 20px color-mix(in srgb, var(--lc-rose) 28%, transparent);
}

.admin-brand-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.admin-brand-copy strong {
  font-size: 17px;
  line-height: 1.1;
  font-weight: 800;
  color: var(--lc-surface);
}

.admin-brand-copy small {
  font-size: 11px;
  line-height: 1.2;
  color: var(--sidebar-muted);
  font-weight: 600;
}

.admin-nav {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
  display: grid;
  gap: 7px;
  align-content: start;
}

.admin-nav::-webkit-scrollbar,
.admin-sidebar::-webkit-scrollbar {
  width: 6px;
}

.admin-nav::-webkit-scrollbar-thumb,
.admin-sidebar::-webkit-scrollbar-thumb {
  background: color-mix(in srgb, var(--lc-surface) 22%, transparent);
  border-radius: 999px;
}

.admin-nav::-webkit-scrollbar-track,
.admin-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.admin-nav-group {
  display: grid;
  gap: 6px;
}

.admin-nav-group[open] .nav-chevron {
  transform: rotate(90deg);
}

.admin-nav-group summary {
  list-style: none;
  cursor: pointer;
}

.admin-nav-group summary::-webkit-details-marker {
  display: none;
}

.admin-nav-link {
  min-height: 46px;
  text-decoration: none;
  color: var(--sidebar-text);
  padding: 0 12px;
  border-radius: 7px;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: background .18s ease, color .18s ease, box-shadow .18s ease, opacity .18s ease;
}

.admin-nav-link:hover {
  background: var(--sidebar-hover);
  color: var(--lc-surface);
}

.admin-nav-link.is-active {
  background: var(--sidebar-active);
  color: var(--lc-surface);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--lc-surface) 18%, transparent);
}

.admin-nav-link.is-disabled {
  color: color-mix(in srgb, var(--sidebar-muted) 72%, transparent);
  cursor: not-allowed;
  opacity: .62;
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
  display: inline-grid;
  place-items: center;
  border-radius: 6px;
  color: var(--sidebar-text);
  border: 1px solid color-mix(in srgb, var(--lc-surface) 20%, transparent);
  font-size: 12px;
  line-height: 1;
  font-weight: 800;
}

.admin-nav-link.is-active .nav-icon {
  background: color-mix(in srgb, var(--lc-surface) 18%, transparent);
  border-color: color-mix(in srgb, var(--lc-surface) 28%, transparent);
  color: var(--lc-surface);
}

.nav-text {
  flex: 1;
  min-width: 0;
}

.nav-chevron {
  color: var(--sidebar-muted);
  font-size: 18px;
  line-height: 1;
  transition: transform .18s ease;
}

.admin-subnav {
  display: grid;
  gap: 3px;
  padding: 0 0 3px 39px;
}

.admin-subnav-link {
  min-height: 32px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 7px;
  padding: 0 10px;
  color: var(--sidebar-muted);
  text-decoration: none;
  font-size: 12px;
  font-weight: 700;
  transition: background .18s ease, color .18s ease;
}

.admin-subnav-link:hover,
.admin-subnav-link.is-active {
  background: var(--sidebar-hover);
  color: var(--lc-surface);
}

.subnav-dot {
  width: 18px;
  height: 18px;
  display: inline-grid;
  place-items: center;
  border-radius: 6px;
  background: color-mix(in srgb, var(--lc-surface) 9%, transparent);
  color: inherit;
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
}

.back-home {
  text-decoration: none;
  color: var(--sidebar-text);
  border: 1px solid color-mix(in srgb, var(--lc-surface) 22%, transparent);
  background: color-mix(in srgb, var(--lc-surface) 8%, transparent);
  border-radius: 8px;
  min-height: 38px;
  justify-content: center;
  font-weight: 700;
  padding: 8px 10px;
  font-size: 13px;
  display: flex;
  align-items: center;
  transition: background .18s ease, border-color .18s ease, color .18s ease;
}

.back-home:hover {
  background: color-mix(in srgb, var(--lc-surface) 14%, transparent);
  border-color: color-mix(in srgb, var(--lc-surface) 32%, transparent);
  color: var(--lc-surface);
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
    letter-spacing: 0;
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
    width: min(292px, 88vw);
    max-width: 100%;
    height: 100vh;
    height: 100dvh;
    z-index: 100;
    transform: translateX(-100%);
    transition: transform 0.22s ease, box-shadow 0.22s ease;
    pointer-events: none;
    padding: 18px 12px 14px;
    padding-top: max(18px, env(safe-area-inset-top, 0px));
    box-shadow: none;
  }

  .admin-sidebar.is-drawer-open {
    transform: translateX(0);
    pointer-events: auto;
    box-shadow: 8px 0 24px color-mix(in srgb, var(--lc-text) 18%, transparent);
  }

  .admin-main {
    min-height: 0;
    padding: 12px 14px max(20px, env(safe-area-inset-bottom, 0px));
    gap: 12px;
  }
}
</style>
