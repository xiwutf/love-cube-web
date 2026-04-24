<template>
  <van-tabbar v-model="active" active-color="#FF6B8A" fixed placeholder>
    <van-tabbar-item icon="home-o"    @click="go('/')">首页</van-tabbar-item>
    <van-tabbar-item icon="search"    @click="go('/search')">发现</van-tabbar-item>
    <van-tabbar-item icon="like-o"    @click="go('/match')">匹配</van-tabbar-item>
    <van-tabbar-item icon="chat-o"    :badge="msgStore.totalUnread || ''" @click="go('/message')">消息</van-tabbar-item>
    <van-tabbar-item icon="contact-o" @click="go('/personal')">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message.js'

const route    = useRoute()
const router   = useRouter()
const msgStore = useMessageStore()

const tabMap = ['/', '/search', '/match', '/message', '/personal']
const active = computed(() => {
  const idx = tabMap.indexOf(route.path)
  return idx >= 0 ? idx : 0
})

function go(path) {
  if (route.path !== path) router.push(path)
}
</script>
