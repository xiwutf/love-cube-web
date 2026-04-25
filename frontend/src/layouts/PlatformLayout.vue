<template>
  <div class="platform-layout">
    <header class="platform-header">
      <div class="platform-container nav-wrap">
        <router-link to="/" class="brand" @click="menuOpen = false">
          <span class="brand-logo">LC</span>
          <span class="brand-text">Love Cube 平台</span>
        </router-link>

        <button class="menu-toggle" type="button" @click="menuOpen = !menuOpen" aria-label="打开菜单">
          {{ menuOpen ? '关闭' : '菜单' }}
        </button>

        <nav class="nav-links nav-links-desktop">
          <router-link to="/" :class="{ 'is-active': isActive('/') }">首页</router-link>
          <router-link to="/announcements" :class="{ 'is-active': isActive('/announcements') }">公告</router-link>
          <router-link to="/articles" :class="{ 'is-active': isActive('/articles') }">资讯</router-link>
          <router-link to="/events" :class="{ 'is-active': isActive('/events') }">活动</router-link>
          <router-link to="/about" :class="{ 'is-active': isActive('/about') }">关于我们</router-link>
          <router-link to="/fellowship-intro" :class="{ 'is-active': isActive('/fellowship-intro') }">联谊介绍</router-link>
        </nav>

        <div class="account-slot">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/account" class="account-entry">{{ userStore.userInfo?.username || '我的账号' }}</router-link>
            <router-link v-if="userStore.isAdmin" to="/admin" class="admin-entry">后台</router-link>
            <button class="logout-btn" type="button" @click="handleLogout">退出</button>
          </template>
          <template v-else>
            <router-link to="/login" class="login-entry">登录 / 注册</router-link>
          </template>
          <router-link to="/fellowship-intro" class="fellowship-entry">进入 联谊</router-link>
        </div>
      </div>
    </header>

    <main class="platform-main">
      <router-view />
    </main>

    <nav class="mobile-quick-nav">
      <router-link to="/" :class="{ 'is-active': isActive('/') }">首页</router-link>
      <router-link to="/announcements" :class="{ 'is-active': isActive('/announcements') }">公告</router-link>
      <router-link to="/articles" :class="{ 'is-active': isActive('/articles') }">资讯</router-link>
      <router-link to="/events" :class="{ 'is-active': isActive('/events') }">活动</router-link>
      <router-link to="/fellowship-intro" :class="{ 'is-active': isActive('/fellowship-intro') }">联谊</router-link>
    </nav>

    <footer class="platform-footer">
      <div class="platform-container footer-inner">
        <div>
          <p class="footer-title">Love Cube Platform</p>
          <p>平台官网用于公告、资讯与活动发布；联谊模块用于移动端互动社交。</p>
        </div>
        <div class="footer-links">
          <router-link to="/policies/terms">用户协议</router-link>
          <router-link to="/policies/privacy">隐私政策</router-link>
          <router-link to="/policies/content-policy">内容规范</router-link>
          <router-link to="/policies/safety">安全说明</router-link>
        </div>
      </div>
    </footer>

    <transition name="menu-fade">
      <div v-if="menuOpen" class="mobile-menu-mask" @click="menuOpen = false" />
    </transition>
    <transition name="menu-slide">
      <nav v-if="menuOpen" class="mobile-menu-panel">
        <router-link to="/" :class="{ 'is-active': isActive('/') }" @click="menuOpen = false">首页</router-link>
        <router-link to="/announcements" :class="{ 'is-active': isActive('/announcements') }" @click="menuOpen = false">公告</router-link>
        <router-link to="/articles" :class="{ 'is-active': isActive('/articles') }" @click="menuOpen = false">资讯</router-link>
        <router-link to="/events" :class="{ 'is-active': isActive('/events') }" @click="menuOpen = false">活动</router-link>
        <router-link to="/about" :class="{ 'is-active': isActive('/about') }" @click="menuOpen = false">关于我们</router-link>
        <router-link to="/fellowship-intro" :class="{ 'is-active': isActive('/fellowship-intro') }" @click="menuOpen = false">进入 联谊</router-link>
        <router-link v-if="userStore.isLoggedIn" to="/account" @click="menuOpen = false">我的账号</router-link>
        <router-link v-if="userStore.isAdmin" to="/admin" @click="menuOpen = false">管理后台</router-link>
        <router-link v-if="!userStore.isLoggedIn" to="/login" @click="menuOpen = false">登录 / 注册</router-link>
        <button v-if="userStore.isLoggedIn" type="button" class="mobile-logout" @click="handleLogout">退出登录</button>
      </nav>
    </transition>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

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
  background: linear-gradient(180deg, #f4f7fb 0%, #ffffff 420px);
  color: #1f2937;
}

.platform-header {
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid #e2e8f0;
}

.nav-wrap {
  min-height: 72px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 16px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.brand-logo {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff5f84, #e84f73);
  color: #fff;
  font-size: 10px;
  letter-spacing: 0.06em;
}

.nav-links {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
}

.nav-links a {
  color: #475569;
  text-decoration: none;
  font-weight: 600;
}

.nav-links a.router-link-exact-active,
.nav-links a.is-active {
  color: #ff5f84;
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
  padding: 6px 12px;
  text-decoration: none;
}

.account-entry,
.admin-entry {
  border: 1px solid #e2e8f0;
  color: #334155;
  background: #fff;
}

.admin-entry {
  border-color: #cbd5e1;
}

.login-entry {
  border: 1px solid #ffd2dc;
  color: #e84f73;
  background: #fff;
}

.logout-btn {
  border: 1px solid #ffe4ec;
  color: #e84f73;
  background: #fff7fa;
}

.fellowship-entry {
  text-decoration: none;
  background: linear-gradient(135deg, #ff5f84, #e84f73);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 999px;
  padding: 8px 14px;
  box-shadow: 0 10px 22px rgba(255, 95, 132, 0.28);
}

.menu-toggle {
  display: none;
  border: 1px solid #ffd2dc;
  background: #fff;
  color: #e84f73;
  font-size: 13px;
  font-weight: 700;
  border-radius: 999px;
  padding: 8px 14px;
}

.platform-main {
  min-height: calc(100vh - 72px - 112px);
}

.mobile-quick-nav {
  display: none;
}

.platform-footer {
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.footer-inner {
  min-height: 112px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  color: #64748b;
}

.footer-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.footer-links {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.footer-links a {
  text-decoration: none;
  color: #475569;
}

.footer-links a:hover {
  color: #ff5f84;
}

.mobile-logout {
  border: 1px solid #ffd8e3;
  color: #e84f73;
  background: #fff8fa;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  text-align: left;
  padding: 10px 8px;
}

@media (min-width: 768px) and (max-width: 1023px) {
  .nav-wrap {
    grid-template-columns: auto 1fr;
    gap: 14px;
    padding: 10px 0;
  }

  .account-slot {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }

  .fellowship-entry {
    justify-self: start;
  }
}

@media (max-width: 767px) {
  .nav-wrap {
    grid-template-columns: 1fr auto;
    min-height: 60px;
    gap: 10px;
  }

  .brand {
    font-size: 17px;
  }

  .brand-logo {
    width: 24px;
    height: 24px;
  }

  .menu-toggle {
    display: inline-flex;
    align-items: center;
  }

  .nav-links-desktop,
  .account-slot {
    display: none;
  }

  .platform-main {
    min-height: calc(100vh - 60px - 58px);
    padding-bottom: calc(68px + env(safe-area-inset-bottom));
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
    border-top: 1px solid #e2e8f0;
    backdrop-filter: blur(8px);
  }

  .mobile-quick-nav a {
    display: flex;
    align-items: center;
    justify-content: center;
    text-decoration: none;
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
  }

  .mobile-quick-nav a.router-link-exact-active,
  .mobile-quick-nav a.is-active {
    color: #ff5f84;
  }

  .platform-footer {
    padding-bottom: calc(66px + env(safe-area-inset-bottom));
  }

  .footer-inner {
    min-height: 88px;
    font-size: 12px;
  }

  .mobile-menu-mask {
    position: fixed;
    inset: 0;
    z-index: 100;
    background: rgba(15, 23, 42, 0.35);
  }

  .mobile-menu-panel {
    position: fixed;
    top: calc(60px + env(safe-area-inset-top));
    left: 0;
    right: 0;
    z-index: 101;
    background: #fff;
    border-bottom: 1px solid #e2e8f0;
    display: grid;
    padding: 10px 12px;
    gap: 6px;
  }

  .mobile-menu-panel a {
    text-decoration: none;
    color: #334155;
    font-size: 15px;
    font-weight: 600;
    padding: 10px 8px;
    border-radius: 10px;
  }

  .mobile-menu-panel a.router-link-exact-active,
  .mobile-menu-panel a.is-active {
    background: #fff2f6;
    color: #ff5f84;
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
