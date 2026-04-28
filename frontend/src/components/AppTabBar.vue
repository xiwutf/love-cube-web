<template>
  <van-tabbar
    v-model="active"
    class="fellowship-tabbar"
    active-color="#FF6B8A"
    fixed
    placeholder
  >
    <van-tabbar-item icon="home-o" @click="go('/fellowship/discover')">首页</van-tabbar-item>
    <van-tabbar-item icon="search" @click="go('/fellowship/search')">发现</van-tabbar-item>
    <van-tabbar-item icon="like-o" @click="go('/fellowship/match')">认识</van-tabbar-item>
    <van-tabbar-item icon="chat-o" :badge="msgStore.totalUnread || ''" @click="go('/fellowship/messages')">消息</van-tabbar-item>
    <van-tabbar-item icon="contact-o" @click="go('/fellowship/me')">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message.js'

const route = useRoute()
const router = useRouter()
const msgStore = useMessageStore()

const tabMap = [
  '/fellowship/discover',
  '/fellowship/search',
  '/fellowship/match',
  '/fellowship/messages',
  '/fellowship/me'
]

const active = computed(() => {
  const idx = tabMap.findIndex((path) => route.path === path || route.path.startsWith(`${path}/`))
  return idx >= 0 ? idx : 0
})

function go(path) {
  if (route.path !== path) router.push(path)
}
</script>

<style scoped>
:deep(.fellowship-tabbar.van-tabbar--fixed) {
  position: fixed;
  left: 50%;
  right: auto;
  transform: translateX(-50%);
  width: min(100%, 480px);
  bottom: 0;
  z-index: 120;
}
</style>
