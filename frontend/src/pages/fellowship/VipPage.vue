<template>
  <div class="vip-page">
    <NavBar title="VIP 会员" />

    <section class="vip-hero">
      <div class="hero-orbit" />
      <div class="hero-top">
        <div class="avatar-frame">
          <van-image round width="58" height="58" :src="userAvatar" fit="cover">
            <template #error>
              <div class="avatar-fallback">{{ userInitial }}</div>
            </template>
          </van-image>
        </div>
        <div class="vip-card-info">
          <span class="vip-card-name">{{ userName }}</span>
          <span class="vip-card-status">{{ statusText }}</span>
        </div>
        <span class="premium-chip">PREMIUM</span>
      </div>
      <div class="hero-title">
        <p>让优质身份被优先看见</p>
        <h1>Love Cube VIP</h1>
      </div>
      <div class="hero-privileges">
        <span>金色身份标识</span>
        <span>推荐曝光加权</span>
        <span>高意向用户优先触达</span>
      </div>
    </section>

    <section class="section benefit-section">
      <div class="section-head">
        <p class="section-title">VIP 会员特权</p>
        <span>开通后立即生效</span>
      </div>
      <div class="benefits-grid">
        <div v-for="b in benefits" :key="b.title" class="benefit-item">
          <div class="benefit-icon">
            <van-icon :name="b.icon" size="22" />
          </div>
          <div>
            <span class="benefit-title">{{ b.title }}</span>
            <small>{{ b.desc }}</small>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-head">
        <p class="section-title">选择套餐</p>
        <span>越长期越划算</span>
      </div>
      <div class="package-list">
        <div
          v-for="pkg in packages"
          :key="pkg.id"
          class="package-item"
          :class="{ selected: selected === pkg.id }"
          @click="selected = pkg.id"
        >
          <span v-if="pkg.badge" class="package-badge">{{ pkg.badge }}</span>
          <div class="package-name">{{ pkg.name }}</div>
          <div class="package-price"><small>¥</small>{{ pkg.price }}</div>
          <div class="package-desc">{{ pkg.desc }}</div>
        </div>
      </div>
    </section>

    <section class="trust-panel">
      <div>
        <p>认证 + VIP 双身份展示</p>
        <span>资料页推荐卡片互动入口都会强化可信与优质感</span>
      </div>
      <van-icon name="certificate" size="30" />
    </section>

    <div class="vip-action">
      <van-button
        type="primary"
        block
        round
        :loading="paying"
        color="linear-gradient(135deg, #ff9a3c, #ff6123)"
        @click="handlePay"
      >
        立即升级高级身份
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
  { icon: 'fire-o', title: '曝光加权', desc: '优先进入推荐' },
  { icon: 'eye-o', title: '访客解锁', desc: '看见谁关注了你' },
  { icon: 'chat-o', title: '高频互动', desc: '更多聊天机会' },
  { icon: 'gold-coin-o', title: '尊贵金标', desc: '身份更有辨识度' },
]

const packages = [
  { id: 'month', name: '月卡', price: 30, desc: '轻量体验', badge: '' },
  { id: 'season', name: '季卡', price: 80, desc: '连续曝光', badge: '推荐' },
  { id: 'year', name: '年卡', price: 280, desc: '长期优惠', badge: '省更多' },
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
  showLoadingToast({ message: '支付中...', forbidClick: true })
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
.vip-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at 50% 0, rgba(255, 225, 164, 0.2), rgba(255, 225, 164, 0) 32%),
    linear-gradient(180deg, #17121c 0, #292032 180px, #f7f3ec 181px, #f7f3ec 100%);
  padding-bottom: 96px;
  color: #2a2130;
}

.vip-hero {
  position: relative;
  overflow: hidden;
  margin: 12px 14px;
  padding: 18px;
  min-height: 210px;
  border-radius: 24px;
  color: #fff5d7;
  background:
    radial-gradient(circle at 78% 18%, rgba(255, 232, 174, 0.4), rgba(255, 232, 174, 0) 27%),
    linear-gradient(135deg, #211728 0%, #3d283a 45%, #976324 100%);
  border: 1px solid rgba(255, 225, 168, 0.34);
  box-shadow: 0 18px 42px rgba(34, 20, 11, 0.26);
}

.hero-orbit {
  position: absolute;
  right: -42px;
  bottom: -56px;
  width: 170px;
  height: 170px;
  border-radius: 50%;
  border: 1px solid rgba(255, 232, 178, 0.24);
  box-shadow: inset 0 0 0 22px rgba(255, 226, 169, 0.06);
}

.hero-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-frame {
  width: 66px;
  height: 66px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff6d2, #c98a31);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.22);
}

.avatar-fallback {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
}

.vip-card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.vip-card-name {
  font-size: 18px;
  font-weight: 800;
  color: #fff8e5;
}

.vip-card-status {
  font-size: 12px;
  color: rgba(255, 242, 208, 0.78);
}

.premium-chip {
  border-radius: 999px;
  background: rgba(255, 232, 173, 0.14);
  border: 1px solid rgba(255, 232, 173, 0.34);
  color: #ffe7a6;
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 1px;
  padding: 4px 8px;
}

.hero-title {
  position: relative;
  z-index: 1;
  margin-top: 30px;
}

.hero-title p {
  margin: 0 0 5px;
  color: rgba(255, 235, 192, 0.78);
  font-size: 13px;
}

.hero-title h1 {
  margin: 0;
  font-size: 36px;
  line-height: 1;
  letter-spacing: 0.6px;
}

.hero-privileges {
  position: relative;
  z-index: 1;
  margin-top: 22px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.hero-privileges span {
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #ffe8b3;
  font-size: 11px;
  font-weight: 700;
  padding: 5px 8px;
}

.section {
  background: rgba(255, 255, 255, 0.94);
  margin: 10px 14px 0;
  padding: 16px;
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(68, 51, 35, 0.07);
  border: 1px solid rgba(236, 224, 207, 0.82);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 13px;
}

.section-title {
  font-size: 17px;
  font-weight: 800;
  color: #2b2430;
  margin: 0;
}

.section-head span {
  font-size: 11px;
  color: #9b7a46;
}

.benefits-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 12px;
  background: linear-gradient(135deg, #fffaf1, #fff);
  border: 1px solid #f4e3c3;
  border-radius: 14px;
}

.benefit-icon {
  flex: 0 0 auto;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  color: #7a4c14;
  background: linear-gradient(135deg, #fff1c6, #e5b45b);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.62);
}

.benefit-title {
  display: block;
  font-size: 14px;
  font-weight: 800;
  color: #2d2630;
}

.benefit-item small {
  display: block;
  margin-top: 2px;
  font-size: 11px;
  color: #94754b;
}

.package-list {
  display: flex;
  gap: 10px;
}

.package-item {
  position: relative;
  flex: 1;
  min-height: 112px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 18px 8px 12px;
  border: 1.5px solid #e7d8c0;
  border-radius: 16px;
  background: linear-gradient(180deg, #fff 0%, #fff9ee 100%);
  cursor: pointer;
  transition: var(--lc-transition);
}

.package-item.selected {
  border-color: #c88428;
  background: linear-gradient(180deg, #fff8df 0%, #fff 100%);
  box-shadow: 0 10px 24px rgba(184, 118, 36, 0.18);
  transform: translateY(-2px);
}

.package-badge {
  position: absolute;
  top: -9px;
  border-radius: 999px;
  background: linear-gradient(135deg, #3a250d, #c3842d);
  color: #fff2cc;
  font-size: 10px;
  font-weight: 800;
  padding: 3px 8px;
}

.package-name {
  font-size: 15px;
  font-weight: 800;
  color: #2b2430;
  margin-bottom: 6px;
}

.package-price {
  font-size: 26px;
  line-height: 1;
  font-weight: 900;
  color: #b36d19;
}

.package-price small {
  font-size: 12px;
  margin-right: 1px;
}

.package-desc {
  margin-top: 7px;
  font-size: 11px;
  color: #96764a;
}

.trust-panel {
  margin: 10px 14px 0;
  border-radius: 18px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #221a29, #523524);
  color: #ffe6ae;
  box-shadow: 0 10px 24px rgba(49, 31, 24, 0.16);
}

.trust-panel p {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
}

.trust-panel span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 232, 184, 0.72);
  line-height: 1.4;
}

.vip-action {
  position: fixed;
  left: 50%;
  bottom: 0;
  width: min(480px, 100%);
  transform: translateX(-50%);
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, rgba(247, 243, 236, 0), rgba(247, 243, 236, 0.96) 26%, #f7f3ec 100%);
}
</style>

