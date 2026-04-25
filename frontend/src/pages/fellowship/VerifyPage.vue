<template>
  <div class="verify-page">
    <NavBar title="认证中心" />
    <section class="card">
      <h3>实名认证</h3>
      <p>完成认证后可提升可信度并获得更多曝光机会。</p>
      <van-button round type="primary" block @click="router.push('/account')">去认证</van-button>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
</script>

<style scoped>
.verify-page {
  min-height: 100vh;
  background: #f7f9fc;
}

.card {
  margin: 12px;
  padding: 16px;
  border-radius: 16px;
  background: #fff;
}

.card h3 {
  margin: 0;
  font-size: 16px;
}

.card p {
  margin: 10px 0 16px;
  font-size: 13px;
  color: #6b7280;
}
</style>

