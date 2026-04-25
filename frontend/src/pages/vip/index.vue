<template>
  <div class="vip-page">
    <NavBar title="VIP 会员" />

    <!-- 会员卡 -->
    <div class="vip-card">
      <van-image
        round
        width="60"
        height="60"
        :src="userAvatar"
        fit="cover"
      >
        <template #error>
          <div class="avatar-fallback">{{ userInitial }}</div>
        </template>
      </van-image>
      <div class="vip-card-info">
        <span class="vip-card-name">{{ userName }}</span>
        <span class="vip-card-status">{{ statusText }}</span>
      </div>
    </div>

    <!-- 会员特权 -->
    <div class="section">
      <p class="section-title">VIP 会员特权</p>
      <div class="benefits-grid">
        <div v-for="b in benefits" :key="b.text" class="benefit-item">
          <van-icon :name="b.icon" size="28" color="#FF6B8A" />
          <span class="benefit-text">{{ b.text }}</span>
        </div>
      </div>
    </div>

    <!-- 选择套餐 -->
    <div class="section">
      <p class="section-title">选择套餐</p>
      <div class="package-list">
        <div
          v-for="pkg in packages"
          :key="pkg.id"
          class="package-item"
          :class="{ selected: selected === pkg.id }"
          @click="selected = pkg.id"
        >
          <div class="package-name">{{ pkg.name }}</div>
          <div class="package-price">¥{{ pkg.price }}</div>
        </div>
      </div>
    </div>

    <!-- 开通按钮 -->
    <div class="vip-action">
      <van-button
        type="primary"
        block
        round
        :loading="paying"
        color="linear-gradient(135deg, #ff9a3c, #ff6123)"
        @click="handlePay"
      >
        立即开通 VIP
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { showConfirmDialog, showToast, showLoadingToast, closeToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { buyVip } from '@/api/vip.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()
const paying    = ref(false)
const selected  = ref('month')

const userAvatar  = computed(() => userStore.userInfo?.avatar || '')
const userName    = computed(() => userStore.userInfo?.nickname || '用户')
const userInitial = computed(() => (userName.value || '?')[0])

const benefits = [
  { icon: 'fire-o',      text: '优先推荐匹配' },
  { icon: 'chat-o',      text: '无限留言互动' },
  { icon: 'eye-o',       text: '查看访客记录' },
  { icon: 'gold-coin-o', text: '尊贵身份标识' },
]

const packages = [
  { id: 'month',  name: '月卡', price: 30  },
  { id: 'season', name: '季卡', price: 80  },
  { id: 'year',   name: '年卡', price: 280 },
]

const statusText = computed(() => {
  const pkg = packages.find(p => p.id === selected.value)
  return pkg ? `开通${pkg.name}后立即生效` : '未开通 VIP'
})

async function handlePay() {
  const pkg = packages.find(p => p.id === selected.value)
  try {
    await showConfirmDialog({
      title: '确认支付',
      message: `确认支付 ¥${pkg.price} 购买${pkg.name} VIP 吗？`,
    })
  } catch {
    return
  }

  paying.value = true
  showLoadingToast({ message: '支付中…', forbidClick: true })
  try {
    await buyVip(pkg.id, pkg.name, pkg.price)
    closeToast()
    showToast({ type: 'success', message: 'VIP 开通成功！' })
  } catch (e) {
    closeToast()
    showToast({ type: 'fail', message: e?.message || '支付失败，请重试' })
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.vip-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 32px; }

.vip-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 12px 16px;
  padding: 20px;
  background: linear-gradient(135deg, #ff9a3c, #ff6123);
  border-radius: 16px;
  color: #fff;
}
.avatar-fallback {
  width: 60px; height: 60px; border-radius: 50%;
  background: rgba(255,255,255,.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700;
}
.vip-card-info { display: flex; flex-direction: column; gap: 4px; }
.vip-card-name  { font-size: 17px; font-weight: 700; }
.vip-card-status { font-size: 12px; opacity: .85; }

.section { background: #fff; margin: 8px 0; padding: 16px; }
.section-title { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 12px; }

.benefits-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.benefit-item {
  display: flex; align-items: center; gap: 8px;
  padding: 12px; background: #fff5f5; border-radius: 10px;
}
.benefit-text { font-size: 13px; color: #555; }

.package-list { display: flex; gap: 10px; }
.package-item {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  padding: 14px 8px; border: 2px solid #eee; border-radius: 12px;
  cursor: pointer; transition: border-color .2s, background .2s;
}
.package-item.selected { border-color: #ff6123; background: #fff5f0; }
.package-name  { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 4px; }
.package-price { font-size: 18px; font-weight: 700; color: #ff6123; }

.vip-action { padding: 16px; }
</style>
