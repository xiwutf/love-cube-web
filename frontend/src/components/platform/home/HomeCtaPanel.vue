<template>
  <section class="cta-panel">
    <div>
      <h2>加入 Love Cube，开启美好生活之旅</h2>
      <p>在这里，遇见有趣的人，发现精彩的生活。</p>
    </div>
    <div class="cta-actions">
      <router-link to="/login" class="home-btn home-btn-white">立即注册</router-link>
      <router-link to="/modules" class="home-btn home-btn-outline">了解更多</router-link>
    </div>
    <img :src="ctaBackgroundImage" alt="" loading="lazy" decoding="async" @error="$emit('media-error', $event)">
  </section>
</template>

<script setup>
defineProps({
  ctaBackgroundImage: {
    type: String,
    required: true
  }
})

defineEmits(['media-error'])
</script>

<style scoped>
.cta-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 142px;
  margin-top: 22px;
  padding: 30px 38px;
  border-radius: 8px;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-blue), #6d5dfc);
  overflow: hidden;
}

.cta-panel h2 {
  margin: 0;
  font-size: clamp(26px, 2.3vw, 34px);
}

.cta-panel p {
  margin: 12px 0 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 16px;
}

.cta-panel img {
  position: absolute;
  right: 70px;
  bottom: -46px;
  width: 190px;
  opacity: 0.3;
  pointer-events: none;
}

.cta-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 0;
}

.home-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 132px;
  height: 44px;
  padding: 0 22px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
}

.home-btn-white {
  color: var(--lc-blue);
  background: var(--lc-surface);
}

.home-btn-outline {
  color: var(--lc-surface);
  border-color: rgba(255, 255, 255, 0.62);
}

@media (max-width: 760px) {
  .cta-panel {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px 16px;
  }

  .cta-actions,
  .home-btn {
    width: 100%;
  }
}
</style>
