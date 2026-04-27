<template>
  <div class="platform-layout">
    <header class="platform-header" :class="{ 'is-scrolled': isScrolled }">
      <div class="header-main">
        <div class="nav-wrap">
          <router-link to="/" class="brand" @click="menuOpen = false">
            <span class="brand-logo" aria-hidden="true">
              <img :src="loveCubeIcon" alt="">
            </span>
            <span class="brand-copy">
              <span class="brand-text">Love Cube</span>
              <span class="brand-tag">多元连接平台</span>
            </span>
          </router-link>

          <nav class="nav-links nav-links-desktop">
            <router-link
              v-for="item in navItems"
              :key="item.to"
              :to="item.to"
              :class="{ 'is-active': isActive(item.to) }"
            >
              {{ item.label }}
            </router-link>
          </nav>

          <form class="nav-search" role="search" @submit.prevent="handleSearch">
            <span class="nav-search-icon" aria-hidden="true">🔍</span>
            <input v-model.trim="searchKeyword" type="search" placeholder="搜索内容、用户、活动..." aria-label="搜索内容">
          </form>

          <div class="account-slot">
            <router-link to="/events" class="nav-action">
              <span aria-hidden="true">✎</span>
              签到
            </router-link>
            <router-link to="/messages" class="nav-action nav-action-message">
              <span aria-hidden="true">💬</span>
              平台消息
              <i v-if="messageBadge" class="message-badge">{{ messageBadge }}</i>
            </router-link>
            <template v-if="userStore.isLoggedIn">
              <router-link to="/me" class="login-entry">{{ userStore.userInfo?.username || '平台个人中心' }}</router-link>
              <router-link v-if="userStore.isAdmin" to="/admin" class="admin-entry">后台</router-link>
            </template>
            <template v-else>
              <router-link to="/login" class="register-entry">登录 / 注册</router-link>
            </template>
          </div>

          <button class="menu-toggle" type="button" @click="menuOpen = !menuOpen" aria-label="打开菜单">
            <span></span>
            <span></span>
            <span></span>
          </button>
        </div>
      </div>
    </header>

    <RouteBackButton v-if="showRouteBackButton" class="platform-route-back" />

    <main class="platform-main">
      <router-view />
    </main>

    <nav class="mobile-quick-nav">
      <router-link to="/" :class="{ 'is-active': isActive('/') }">首页</router-link>
      <router-link to="/modules" :class="{ 'is-active': isActive('/modules') }">模块</router-link>
      <router-link to="/articles" :class="{ 'is-active': isActive('/articles') }">内容</router-link>
      <router-link to="/events" :class="{ 'is-active': isActive('/events') }">活动</router-link>
      <router-link to="/fellowship-intro" :class="{ 'is-active': isActive('/fellowship-intro') }">联谊</router-link>
    </nav>

    <footer class="platform-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <p class="footer-title">Love Cube Platform</p>
          <p class="footer-desc">连接真实的人、内容与服务，打造可持续增长的多模块平台。</p>
        </div>
        <div class="footer-cols">
          <section class="footer-col">
            <h4>平台</h4>
            <router-link to="/modules">模块中心</router-link>
            <router-link to="/announcements">平台动态</router-link>
            <router-link to="/fellowship-intro">联谊介绍</router-link>
          </section>
          <section class="footer-col">
            <h4>内容</h4>
            <router-link to="/articles">精选内容</router-link>
            <router-link to="/events">活动中心</router-link>
            <router-link to="/about">关于我们</router-link>
          </section>
          <section class="footer-col">
            <h4>合规</h4>
            <router-link to="/policies/terms">用户协议</router-link>
            <router-link to="/policies/privacy">隐私政策</router-link>
            <router-link to="/policies/content-policy">内容规范</router-link>
          </section>
        </div>
      </div>
      <div class="footer-bottom">© {{ new Date().getFullYear() }} Love Cube. All rights reserved.</div>
    </footer>

    <transition name="menu-fade">
      <div v-if="menuOpen" class="mobile-menu-mask" @click="menuOpen = false" />
    </transition>
    <transition name="menu-slide">
      <nav v-if="menuOpen" class="mobile-menu-panel">
        <form class="mobile-search" role="search" @submit.prevent="handleSearch">
          <span aria-hidden="true">⌕</span>
          <input v-model.trim="searchKeyword" type="search" placeholder="搜索内容、用户、活动...">
        </form>
        <router-link
          v-for="item in mobileNavItems"
          :key="item.to"
          :to="item.to"
          :class="{ 'is-active': isActive(item.to) }"
          @click="menuOpen = false"
        >
          {{ item.label }}
        </router-link>
        <router-link to="/events" @click="menuOpen = false">签到</router-link>
        <router-link to="/messages" @click="menuOpen = false">平台消息中心</router-link>
        <router-link v-if="userStore.isLoggedIn" to="/me" @click="menuOpen = false">平台个人中心</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin" @click="menuOpen = false">管理后台</router-link>
        <router-link v-if="!userStore.isLoggedIn" to="/login" @click="menuOpen = false">登录 / 注册</router-link>
        <button v-if="userStore.isLoggedIn" type="button" class="mobile-logout" @click="handleLogout">退出登录</button>
      </nav>
    </transition>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import RouteBackButton from '@/components/RouteBackButton.vue'
import loveCubeIcon from '@/assets/brand/love-cube-icon.svg'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuOpen = ref(false)
const isScrolled = ref(false)
const searchKeyword = ref('')

const navItems = [
  { to: '/', label: '首页' },
  { to: '/modules', label: '缘说中心' },
  { to: '/announcements', label: '平台动态' },
  { to: '/articles', label: '精选内容' },
  { to: '/events', label: '活动' },
  { to: '/fellowship-intro', label: '联谊介绍' }
]

const mobileNavItems = [
  ...navItems,
  { to: '/about', label: '关于我们' }
]

const messageBadge = computed(() => '')
const navHomePaths = computed(() => new Set([...navItems.map(item => item.to), '/about']))
const showRouteBackButton = computed(() => !navHomePaths.value.has(route.path))

watch(() => route.fullPath, () => {
  menuOpen.value = false
  userStore.syncCurrentUser()
})

function isActive(basePath) {
  if (basePath === '/') return route.path === '/'
  return route.path === basePath || route.path.startsWith(`${basePath}/`)
}

function handleLogout() {
  userStore.logout()
  menuOpen.value = false
  router.push('/')
}

function handleSearch() {
  const keyword = searchKeyword.value.trim()
  menuOpen.value = false
  if (!keyword) {
    router.push('/articles')
    return
  }
  router.push({ path: '/articles', query: { keyword } })
}

function handleScroll() {
  isScrolled.value = window.scrollY > 8
}

onMounted(() => {
  handleScroll()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.platform-layout {
  min-height: 100vh;
  background: #f7f9fd;
  color: #111827;
}

.platform-header {
  position: sticky;
  top: 10px;
  z-index: 100;
  background: transparent;
  border-bottom: 0;
  backdrop-filter: none;
  transition: filter 0.22s ease;
}

.platform-header.is-scrolled {
  filter: drop-shadow(0 10px 24px rgba(58, 51, 118, 0.22));
}

.header-main,
.nav-wrap,
.footer-inner {
  width: 100%;
  margin: 0 auto;
}

.nav-wrap {
  min-height: 58px;
  padding: 0 18px;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) minmax(180px, 320px) auto auto;
  align-items: center;
  gap: 12px;
  margin: 0 auto;
  width: calc(100% - 30px);
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  background: linear-gradient(90deg, #ef5ca7 0%, #8f96f8 56%, #6f8de7 100%);
  box-shadow: 0 12px 28px rgba(94, 73, 178, 0.2);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 152px;
  color: inherit;
  text-decoration: none;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 18px rgba(89, 26, 82, 0.25);
}

.brand-logo img {
  width: 100%;
  height: 100%;
  display: block;
}

.brand-copy {
  display: inline-flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand-text {
  font-size: 18px;
  font-weight: 800;
  color: #ffffff;
}

.brand-tag {
  margin-top: 1px;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.78);
  font-weight: 600;
}

.nav-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  min-width: 0;
  overflow: hidden;
}

.nav-links a {
  position: relative;
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
  font-weight: 700;
  font-size: 15px;
  line-height: 1;
  white-space: nowrap;
  padding: 20px 2px 18px;
  transition: color 0.2s ease, opacity 0.2s ease;
  opacity: 0.9;
}

.nav-links a:hover,
.nav-links a.router-link-exact-active,
.nav-links a.is-active {
  color: #ffffff;
  opacity: 1;
}

.nav-links a.router-link-exact-active::after,
.nav-links a.is-active::after {
  content: '';
  position: absolute;
  left: 6px;
  right: 6px;
  bottom: 6px;
  height: 2px;
  border-radius: 999px;
  background: #ffffff;
}

.nav-search,
.mobile-search {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid #edf1f7;
  background: #f8fafc;
  color: #7c8a9d;
}

.nav-search {
  height: 34px;
  border-radius: 999px;
  padding: 0 12px;
  justify-self: stretch;
  width: 100%;
  min-width: 0;
  border: 1px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.24);
  color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

.account-slot {
  min-width: 0;
}

.nav-search-icon {
  color: rgba(255, 255, 255, 0.84);
  font-size: 14px;
  line-height: 1;
}

.nav-search input,
.mobile-search input {
  min-width: 0;
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
}

.nav-search input::placeholder,
.mobile-search input::placeholder {
  color: rgba(255, 255, 255, 0.75);
}

.account-slot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

.nav-action,
.admin-entry,
.login-entry,
.register-entry {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 30px;
  color: rgba(255, 255, 255, 0.95);
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
  white-space: nowrap;
}

.nav-action:hover,
.admin-entry:hover,
.login-entry:hover,
.register-entry:hover {
  color: #ffffff;
}

.login-entry,
.register-entry {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-badge {
  display: grid;
  place-items: center;
  min-width: 16px;
  height: 16px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ff4d7f;
  color: #fff;
  font-size: 11px;
  font-style: normal;
}

.register-entry {
  min-height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.52);
  background: rgba(255, 255, 255, 0.18);
}

.menu-toggle {
  display: none;
  border: 1px solid #e1e8f2;
  background: #fff;
  width: 44px;
  height: 44px;
  border-radius: 8px;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 5px;
}

.menu-toggle span {
  width: 18px;
  height: 2px;
  border-radius: 999px;
  background: #243449;
}

.platform-main {
  min-height: calc(100vh - 74px - 212px);
}

.platform-route-back {
  position: sticky;
  top: 86px;
  z-index: 60;
  margin: 8px 0 0 12px;
  width: fit-content;
}

.platform-route-back:hover {
  border-color: var(--lc-blue-border);
  color: var(--lc-blue);
}

.mobile-quick-nav {
  display: none;
}

.platform-footer {
  margin-top: 36px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.footer-inner {
  padding: 36px 24px 24px;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 2fr);
  gap: 30px;
  color: #64748b;
}

.footer-title {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.footer-desc {
  margin: 12px 0 0;
  line-height: 1.8;
  font-size: 14px;
}

.footer-cols {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.footer-col h4 {
  margin: 0 0 12px;
  color: #334155;
  font-size: 14px;
  font-weight: 800;
}

.footer-col a {
  display: block;
  margin-top: 8px;
  color: #64748b;
  text-decoration: none;
  font-size: 13px;
}

.footer-col a:hover {
  color: #e84f73;
}

.footer-bottom {
  border-top: 1px solid #e2e8f0;
  color: #94a3b8;
  font-size: 12px;
  padding: 14px 24px calc(14px + env(safe-area-inset-bottom));
  text-align: center;
}

.mobile-logout {
  border: 1px solid #ffd8e3;
  color: #e84f73;
  background: #fff8fa;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  text-align: left;
  padding: 12px 10px;
}

@media (min-width: 768px) and (max-width: 1199px) {
  .platform-header {
    top: 0;
  }

  .nav-wrap {
    grid-template-columns: auto 1fr auto;
    gap: 14px;
    padding: 0 18px;
    width: calc(100% - 24px);
    border-radius: 16px;
  }

  .nav-links-desktop,
  .account-slot {
    display: none;
  }

  .nav-search {
    width: min(100%, 360px);
    justify-self: stretch;
  }

  .menu-toggle {
    display: inline-flex;
  }

  .footer-inner {
    grid-template-columns: 1fr;
  }

  .mobile-menu-mask {
    position: fixed;
    inset: 0;
    z-index: 100;
    background: rgba(15, 23, 42, 0.35);
  }

  .mobile-menu-panel {
    position: fixed;
    top: 94px;
    left: 32px;
    right: 32px;
    z-index: 101;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
    padding: 14px;
    border: 1px solid #dbeafe;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 20px 46px rgba(15, 23, 42, 0.18);
    backdrop-filter: blur(18px);
  }

  .mobile-search {
    grid-column: 1 / -1;
    min-height: 44px;
    border-radius: 8px;
    padding: 0 12px;
  }

  .mobile-menu-panel a {
    color: #334155;
    font-size: 15px;
    font-weight: 700;
    text-decoration: none;
    padding: 12px;
    border-radius: 8px;
  }

  .mobile-menu-panel a.router-link-exact-active,
  .mobile-menu-panel a.is-active {
    background: #fff2f6;
    color: #e84f73;
  }
}

@media (max-width: 767px) {
  .platform-header {
    top: 0;
    padding: 0;
  }

  .nav-wrap {
    grid-template-columns: 1fr auto;
    min-height: 64px;
    padding: 0 12px;
    gap: 10px;
    width: calc(100% - 16px);
    border-radius: 12px;
  }

  .brand-logo {
    width: 34px;
    height: 34px;
  }

  .brand-text {
    font-size: 19px;
  }

  .brand-tag {
    display: none;
  }

  .menu-toggle {
    display: inline-flex;
  }

  .nav-links-desktop,
  .nav-search,
  .account-slot {
    display: none;
  }

  .platform-main {
    min-height: calc(100vh - 74px - 58px);
    padding-bottom: calc(70px + env(safe-area-inset-bottom));
  }

  .platform-route-back {
    top: 78px;
    margin: 6px 0 0 10px;
  }

  .mobile-quick-nav {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 90;
    height: calc(58px + env(safe-area-inset-bottom));
    padding-bottom: env(safe-area-inset-bottom);
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    background: rgba(255, 255, 255, 0.96);
    border-top: 1px solid #d6e0ed;
    backdrop-filter: blur(8px);
  }

  .mobile-quick-nav a {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
    text-decoration: none;
  }

  .mobile-quick-nav a.router-link-exact-active,
  .mobile-quick-nav a.is-active {
    color: #e84f73;
  }

  .platform-footer {
    padding-bottom: calc(68px + env(safe-area-inset-bottom));
  }

  .footer-inner {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 28px 16px 18px;
  }

  .footer-cols {
    grid-template-columns: 1fr 1fr;
  }

  .mobile-menu-mask {
    position: fixed;
    inset: 0;
    z-index: 100;
    background: rgba(15, 23, 42, 0.35);
  }

  .mobile-menu-panel {
    position: fixed;
    top: calc(82px + env(safe-area-inset-top));
    left: 12px;
    right: 12px;
    z-index: 101;
    display: grid;
    gap: 6px;
    padding: 12px;
    border: 1px solid #dbeafe;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 16px 32px rgba(15, 23, 42, 0.16);
    backdrop-filter: blur(18px);
  }

  .mobile-search {
    min-height: 44px;
    margin-bottom: 4px;
    padding: 0 12px;
    border-radius: 8px;
  }

  .mobile-menu-panel a {
    color: #334155;
    font-size: 15px;
    font-weight: 700;
    text-decoration: none;
    padding: 11px 9px;
    border-radius: 8px;
  }

  .mobile-menu-panel a.router-link-exact-active,
  .mobile-menu-panel a.is-active {
    background: #fff2f6;
    color: #e84f73;
  }
}

.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: opacity 0.18s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
}

.menu-slide-enter-active,
.menu-slide-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.menu-slide-enter-from,
.menu-slide-leave-to {
  transform: translateY(-8px);
  opacity: 0;
}
</style>
