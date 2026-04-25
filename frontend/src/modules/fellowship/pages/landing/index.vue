<template>
  <div class="landing-page">
    <div class="content">
      <div class="logo">💕</div>
      <h1 class="title">真诚认识</h1>
      <p class="slogan">遇见你，是最好的事</p>
      <div class="actions">
        <router-link to="/fellowship/login" class="btn-primary">立即加入</router-link>
        <router-link to="/fellowship/discover" class="btn-ghost">浏览</router-link>
      </div>
    </div>
    <div class="footer-link">
      <router-link to="/">→ 返回平台官网</router-link>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { storage } from '@f/utils/storage.js'

const router = useRouter()
if (storage.get('token')) {
  router.replace('/fellowship/discover')
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #fff0f3 0%, #ffffff 60%);
  padding: 40px 24px;
  position: relative;
}

.content {
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.logo {
  font-size: 80px;
  line-height: 1;
  margin-bottom: 20px;
}

.title {
  font-size: 36px;
  font-weight: 800;
  color: #ff6b8a;
  letter-spacing: 4px;
  margin-bottom: 12px;
}

.slogan {
  font-size: 16px;
  color: #aaa;
  margin-bottom: 40px;
}

.actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.btn-primary {
  display: inline-block;
  padding: 14px 36px;
  background: linear-gradient(135deg, #ff6b8a, #e85c7b);
  color: #fff;
  text-decoration: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 4px 20px rgba(255, 107, 138, 0.35);
}

.btn-ghost {
  display: inline-block;
  padding: 14px 36px;
  border: 2px solid #ff6b8a;
  color: #ff6b8a;
  text-decoration: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
}

.footer-link {
  position: absolute;
  bottom: 32px;
  font-size: 13px;
}

.footer-link a {
  color: #bbb;
  text-decoration: none;
}

.footer-link a:hover {
  color: #ff6b8a;
}
</style>
