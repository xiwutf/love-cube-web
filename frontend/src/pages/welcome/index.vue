<template>
  <div class="welcome-page" @click="goHome">
    <div class="content">
      <img class="logo" :src="loveCubeIcon" alt="Love Cube">
      <h1 class="title">Love Cube</h1>
      <p class="slogan">遇见你，是最好的</p>
      <p class="countdown">{{ count }} 秒后自动进入</p>
    </div>
    <p class="hint">点击任意处立即进</p>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import loveCubeIcon from '@/assets/brand/love-cube-icon.svg'

const router = useRouter()
const count = ref(3)
let timer = null

function goHome() {
  clearInterval(timer)
  router.replace('/fellowship/discover')
}

onMounted(() => {
  timer = setInterval(() => {
    count.value -= 1
    if (count.value <= 0) goHome()
  }, 1000)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.welcome-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #fff0f3 0%, #ffffff 60%);
  cursor: pointer;
  user-select: none;
  padding: calc(20px + env(safe-area-inset-top)) 12px calc(20px + env(safe-area-inset-bottom));
}

.content { text-align: center; }
.logo { width: 72px; height: 72px; border-radius: 18px; margin-bottom: 16px; box-shadow: 0 18px 36px rgba(232, 79, 115, 0.28); }
.title { font-size: 32px; font-weight: 700; color: #ff6b8a; letter-spacing: 3px; }
.slogan { font-size: 15px; color: #aaa; margin-top: 10px; }
.countdown { font-size: 13px; color: #ccc; margin-top: 32px; }
.hint { position: absolute; bottom: calc(24px + env(safe-area-inset-bottom)); font-size: 12px; color: #ddd; }

@media (max-width: 360px) {
  .welcome-page {
    padding: 20px 12px calc(20px + env(safe-area-inset-bottom));
  }

  .title {
    font-size: 28px;
    letter-spacing: 2px;
  }

  .slogan {
    font-size: 14px;
  }

  .countdown {
    margin-top: 24px;
  }

  .hint {
    position: static;
    margin-top: 16px;
  }
}
</style>

