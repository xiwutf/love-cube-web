<template>
  <div class="member-m">
    <header class="head">
      <router-link to="/m/platform" class="back" aria-label="返回玩法中心">‹</router-link>
      <h1>平台会员</h1>
    </header>

    <section class="hero">
      <p class="kicker">Love Cube Plus</p>
      <h2>{{ status.memberActive ? '会员生效中' : '升级平台会员' }}</h2>
      <p v-if="status.expiresAt" class="exp">到期 {{ String(status.expiresAt).slice(0, 10) }}</p>
      <ul>
        <li v-for="p in status.perks || []" :key="p">{{ p }}</li>
      </ul>
    </section>

    <section class="price-card">
      <p class="price">¥{{ status.monthlyPrice || 19 }}<small>/月</small></p>
      <van-button round block type="primary" color="#6366f1" :loading="paying" @click="subscribe">
        {{ status.memberActive ? '续费 1 个月（演示）' : '立即开通（演示）' }}
      </van-button>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { fetchPlatformMemberStatus, subscribePlatformMember } from '@/api/platformMember.js'

const status = ref({ perks: [], monthlyPrice: 19 })
const paying = ref(false)

async function load() {
  try {
    status.value = await fetchPlatformMemberStatus()
  } catch {
    status.value = { perks: [], monthlyPrice: 19, memberActive: false }
  }
}

async function subscribe() {
  paying.value = true
  try {
    await subscribePlatformMember()
    showToast({ type: 'success', message: '开通成功' })
    await load()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '开通失败' })
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.member-m {
  min-height: 100vh;
  padding: 12px 14px 24px;
  background: #f4f5fb;
}

.head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.back {
  border: none;
  background: #fff;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  font-size: 22px;
}

.head h1 {
  margin: 0;
  font-size: 18px;
}

.hero {
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, #312e81, #4338ca);
  color: #e0e7ff;
}

.kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  opacity: 0.85;
}

.hero h2 {
  margin: 8px 0 0;
  color: #fff;
  font-size: 22px;
}

.exp {
  margin: 6px 0 0;
  font-size: 12px;
  opacity: 0.9;
}

.hero ul {
  margin: 14px 0 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.6;
}

.price-card {
  margin-top: 14px;
  padding: 16px;
  background: #fff;
  border-radius: 14px;
  text-align: center;
}

.price {
  margin: 0 0 12px;
  font-size: 32px;
  font-weight: 800;
  color: #4338ca;
}

.price small {
  font-size: 14px;
  font-weight: 600;
}
</style>
