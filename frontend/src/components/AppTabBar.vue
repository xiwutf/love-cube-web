<template>
  <van-tabbar
    v-model="active"
    class="fellowship-tabbar"
    active-color="#FF6B8A"
    fixed
    placeholder
  >
    <van-tabbar-item icon="home-o" @click="go(fellowshipPath('/discover'))">首页</van-tabbar-item>
    <van-tabbar-item icon="like-o" @click="go(fellowshipPath('/match'))">认识</van-tabbar-item>
    <van-tabbar-item icon="fire-o" @click="go(fellowshipPath('/dynamic'))">动态</van-tabbar-item>
    <van-tabbar-item icon="plus" @click="go(fellowshipPath('/dynamic/publish'))">发布</van-tabbar-item>
    <van-tabbar-item icon="chat-o" :badge="msgStore.totalUnread || ''" @click="go(fellowshipPath('/messages'))">消息</van-tabbar-item>
    <van-tabbar-item icon="contact-o" @click="go(fellowshipPath('/me'))">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessageStore } from '@/stores/message.js'
import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'

const route = useRoute()
const router = useRouter()
const msgStore = useMessageStore()
const { fellowshipPath } = useFellowshipNavBase()

const tabMap = computed(() => [
  fellowshipPath('/discover'),
  fellowshipPath('/match'),
  fellowshipPath('/dynamic'),
  fellowshipPath('/dynamic/publish'),
  fellowshipPath('/messages'),
  fellowshipPath('/me')
])

const active = computed(() => {
  const paths = tabMap.value
  const idx = paths.findIndex((path) => route.path === path || route.path.startsWith(`${path}/`))
  return idx >= 0 ? idx : 0
})

function go(path) {
  if (route.path !== path) router.push(path)
}
</script>

<style scoped>
/* placeholder 才是根节点，fixed 底栏是它里面的 .van-tabbar--fixed，类名不在同一个元素上 */
.fellowship-tabbar :deep(.van-tabbar--fixed) {
  left: 50%;
  right: auto;
  width: min(100%, 480px);
  transform: translateX(-50%);
  z-index: 120;
}
</style>
