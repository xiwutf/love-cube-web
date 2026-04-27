<template>
  <div class="platform-layout">
    <header class="platform-header">
      <div class="header-main">
        <div class="nav-wrap">
          <router-link to="/" class="brand" @click="menuOpen = false">
            <span class="brand-logo" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="white" width="18" height="18">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
              </svg>
            </span>
            <span class="brand-copy">
              <span class="brand-text">Love Cube</span>
              <span class="brand-tag">多元连接平台</span>
            </span>
          </router-link>

          <button class="menu-toggle" type="button" @click="menuOpen = !menuOpen" aria-label="打开菜单">
            {{ menuOpen ? '关闭' : '菜单' }}
          </button>

          <nav class="nav-links nav-links-desktop">
            <router-link to="/" :class="{ 'is-active': isActive('/') }">首页</router-link>
            <router-link to="/modules" :class="{ 'is-active': isActive('/modules') }">模块中心</router-link>
            <router-link to="/announcements" :class="{ 'is-active': isActive('/announcements') }">平台动态</router-link>
            <router-link to="/articles" :class="{ 'is-active': isActive('/articles') }">精选内容</router-link>
            <router-link to="/events" :class="{ 'is-active': isActive('/events') }">活动</router-link>
            <router-link to="/fellowship-intro" :class="{ 'is-active': isActive('/fellowship-intro') }">联谊介绍</router-link>
          </nav>

          <div class="account-slot">
            <template v-if="userStore.isLoggedIn">
              <router-link to="/account" class="account-entry">{{ userStore.userInfo?.username || '我的账号' }}</router-link>
              <router-link v-if="userStore.isAdmin" to="/admin" class="admin-entry">后台</router-link>
              <button class="logout-btn" type="button" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <router-link to="/login" class="login-entry">登录</router-link>
              <router-link to="/login" class="register-entry">免费注册</router-link>
            </template>
          </div>
        </div>
      </div>
    </header>

    <RouteBackButton v-if="route.path !== '/'" class="platform-route-back" />

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
            <router-link to="/articles">资讯中心</router-link>
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
        <router-link to="/" :class="{ 'is-active': isActive('/') }" @click="menuOpen = false">首页</router-link>
        <router-link to="/modules" :class="{ 'is-active': isActive('/modules') }" @click="menuOpen = false">模块中心</router-link>
        <router-link to="/announcements" :class="{ 'is-active': isActive('/announcements') }" @click="menuOpen = false">平台动态</router-link>
        <router-link to="/articles" :class="{ 'is-active': isActive('/articles') }" @click="menuOpen = false">精选内容</router-link>
        <router-link to="/events" :class="{ 'is-active': isActive('/events') }" @click="menuOpen = false">活动</router-link>
        <router-link to="/about" :class="{ 'is-active': isActive('/about') }" @click="menuOpen = false">关于我们</router-link>
        <router-link to="/fellowship-intro" :class="{ 'is-active': isActive('/fellowship-intro') }" @click="menuOpen = false">联谊介绍</router-link>
        <router-link v-if="userStore.isLoggedIn" to="/account" @click="menuOpen = false">我的账号</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin" @click="menuOpen = false">管理后台</router-link>
        <router-link v-if="!userStore.isLoggedIn" to="/login" @click="menuOpen = false">登录</router-link>
        <router-link v-if="!userStore.isLoggedIn" to="/login" @click="menuOpen = false">免费注册</router-link>
        <button v-if="userStore.isLoggedIn" type="button" class="mobile-logout" @click="handleLogout">退出登录</button>
      </nav>
    </transition>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import RouteBackButton from '@/components/RouteBackButton.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuOpen = ref(false)

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
</script>

<style scoped>
.platform-layout {
  min-height: 100vh;
  background: #f3f6fb;
  color: #111827;
}

.platform-header {
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.nav-wrap,
.footer-inner {
  width: calc(100% - 48px);
  margin: 0 auto;
}

.header-main {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid #d6e0ed;
}

.nav-wrap {
  min-height: 68px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #ff6f92, #e84f73);
  box-shadow: 0 10px 24px rgba(232, 79, 115, 0.24);
}

.brand-copy {
  display: inline-flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand-text {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.01em;
}

.brand-tag {
  margin-top: 2px;
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
}

.nav-links {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 22px;
}

.nav-links a {
  color: #334155;
  text-decoration: none;
  font-weight: 700;
  font-size: 15px;
  line-height: 68px;
  border-bottom: 2px solid transparent;
}

.nav-links a.router-link-exact-active,
.nav-links a.is-active {
  color: #e84f73;
  border-bottom-color: #e84f73;
}

.account-slot {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.account-entry,
.admin-entry,
.login-entry,
.logout-btn {
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  padding: 7px 12px;
  text-decoration: none;
  border: 1px solid #d6e0ed;
  background: #fff;
  color: #334155;
  transition: all 0.2s ease;
}

.account-entry:hover,
.admin-entry:hover,
.login-entry:hover,
.logout-btn:hover {
  border-color: #ffd0db;
  color: #e84f73;
}

.login-entry,
.logout-btn {
  border-color: #ffd2dc;
  color: #e84f73;
}

.register-entry {
  text-decoration: none;
  background: linear-gradient(135deg, #ff6f92, #e84f73);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 999px;
  padding: 9px 14px;
  box-shadow: 0 10px 22px rgba(232, 79, 115, 0.24);
}

.menu-toggle {
  display: none;
  border: 1px solid #ffd0db;
  background: #fff;
  color: #e84f73;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  align-items: center;
  justify-content: center;
}

.platform-main {
  min-height: calc(100vh - 68px - 212px);
}

.platform-route-back {
  position: fixed;
  left: 24px;
  top: 84px;
  z-index: 95;
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
  padding: 36px 0 24px;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 2fr);
  gap: 30px;
  color: #64748b;
}

.footer-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
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
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  text-align: left;
  padding: 12px 10px;
}

@media (min-width: 768px) and (max-width: 1199px) {
  .nav-wrap,
  .footer-inner {
    width: calc(100% - 32px);
  }

  .nav-wrap {
    grid-template-columns: auto 1fr;
    gap: 14px;
    padding: 12px 0;
  }

  .account-slot {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }

  .footer-inner {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .nav-wrap,
  .footer-inner {
    width: calc(100% - 24px);
  }

  .nav-wrap {
    grid-template-columns: 1fr auto;
    min-height: 64px;
    gap: 10px;
  }

  .brand-logo {
    width: 30px;
    height: 30px;
    border-radius: 10px;
  }

  .brand-text {
    font-size: 18px;
  }

  .brand-tag {
    display: none;
  }

  .menu-toggle {
    display: inline-flex;
  }

  .nav-links-desktop,
  .account-slot {
    display: none;
  }

  .platform-main {
    min-height: calc(100vh - 64px - 58px);
    padding-bottom: calc(70px + env(safe-area-inset-bottom));
  }

  .platform-route-back {
    left: 12px;
    top: 76px;
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
    text-decoration: none;
    color: #64748b;
    font-size: 12px;
    font-weight: 700;
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
    padding: 28px 0 18px;
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
    top: calc(64px + env(safe-area-inset-top));
    left: 0;
    right: 0;
    z-index: 101;
    background: #fff;
    border-bottom: 1px solid #d6e0ed;
    display: grid;
    padding: 12px;
    gap: 6px;
    box-shadow: 0 16px 32px rgba(15, 23, 42, 0.16);
  }

  .mobile-menu-panel a {
    text-decoration: none;
    color: #334155;
    font-size: 15px;
    font-weight: 700;
    padding: 11px 9px;
    border-radius: 10px;
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
