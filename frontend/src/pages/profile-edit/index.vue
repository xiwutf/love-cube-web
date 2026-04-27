<template>
  <div class="profile-edit-redirect">
    <van-loading size="20" vertical>正在跳转到资料编辑页...</van-loading>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace('/fellowship/profile/edit')
})
</script>

<style scoped>
.profile-edit-redirect {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f8f8f8;
}
</style>
