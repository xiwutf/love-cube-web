<template>
  <div class="user-profile-redirect">
    <van-loading size="20" vertical>正在跳转到用户资料...</van-loading>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const id = route.params.id || route.query.id
  if (id) {
    router.replace(`/fellowship/user-profile/${id}`)
    return
  }
  router.replace('/fellowship/discover')
})
</script>

<style scoped>
.user-profile-redirect {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f8f8f8;
}
</style>
