<template>
  <div class="fellowship-layout fellowship-shell">
    <router-view />
    <RouteBackButton v-if="showLayoutBack" class="fellowship-route-back" />
    <router-link to="/fellowship-intro" class="back-platform">返回平台官网</router-link>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import RouteBackButton from '@/components/RouteBackButton.vue'

const route = useRoute()

const pagesWithOwnBack = new Set([
  '/fellowship/settings',
  '/fellowship/verify',
  '/fellowship/blacklist',
  '/fellowship/invite',
  '/fellowship/my-likes',
  '/fellowship/following',
  '/fellowship/privacy',
  '/fellowship/help',
  '/fellowship/newcomers',
  '/fellowship/profile',
  '/fellowship/profile/edit',
  '/fellowship/profile-edit',
  '/fellowship/vip'
])

const publicEntryPages = new Set([
  '/fellowship',
  '/fellowship/login',
  '/fellowship/welcome'
])

const showLayoutBack = computed(() => {
  if (publicEntryPages.has(route.path)) return false
  if (pagesWithOwnBack.has(route.path)) return false
  if (route.path.startsWith('/fellowship/user-profile/')) return false
  if (route.path.startsWith('/fellowship/chat/')) return false
  return true
})
</script>

<style scoped>
.fellowship-layout {
  position: relative;
}

.fellowship-route-back {
  position: fixed;
  left: max(12px, calc(50% - 228px));
  top: calc(12px + env(safe-area-inset-top));
  z-index: var(--lc-z-sticky);
}
</style>
